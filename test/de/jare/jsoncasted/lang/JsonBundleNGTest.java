/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * The identity core of doc/JSON_BUNDLE.md (step 1, second half): the bundle registry guarantees
 * {@code B == B'} - every file of the bundle is parsed exactly once and shared by all consumers. Fixtures build
 * the A/E/B constellation with temp files: the main references the sub providers A and E, both reference B.
 *
 * <p>
 * Two reference styles are covered: the classic share (A and E reference B under the SAME synonym) and the
 * alias (a later reference under a DIFFERENT synonym - first synonym wins, the linking set is rebuilt under
 * the requesting namespace, a warning is collected, CONSUMED refuses).
 * </p>
 *
 * @author Janusch Rentenatus
 */
public class JsonBundleNGTest {

    private static final JsonDebugLevel DEBUG = JsonDebugLevel.SIMPLE;

    private static Path tempDir;

    @BeforeClass
    public static void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("jsonbundle-registry");
    }

    @AfterClass
    public static void removeTempDir() throws IOException {
        if (tempDir != null) {
            for (Path child : Files.list(tempDir).toList()) {
                Files.deleteIfExists(child);
            }
            Files.deleteIfExists(tempDir);
        }
    }

    private static String write(String name, String content) throws IOException {
        final Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file.toAbsolutePath().toString();
    }

    private static String escaped(String path) {
        return path.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String box(String... lines) {
        return "\"_woodProviders\": [" + String.join(", ", lines) + "]";
    }

    private static String provider(String synonym, String path) {
        return "{\"synonym\": \"" + synonym + "\", \"filename\": \"" + escaped(path) + "\"}";
    }

    /**
     * The classic constellation: A and E both reference B under the SAME synonym - one load, one instance.
     */
    private void buildSharedFixture(String synonym) throws IOException {
        final String b = write("B.json", "{\"shared\": {\"_woodObjectId\": \"5\", \"text\": \"B\"}}");
        final String a = write("A.json", "{" + box(provider(synonym, b))
                + ", \"ref\": {\"_woodLink\": \"" + synonym + "::5\"}}");
        final String e = write("E.json", "{" + box(provider(synonym, b))
                + ", \"ref\": {\"_woodLink\": \"" + synonym + "::5\"}}");
        write("main.json", "{" + box(provider("subA", a), provider("subE", e)) + ", \"one\": 1}");
    }

    /**
     * The alias constellation: A references B as dep, E references the SAME file as base.
     */
    private void buildAliasFixture() throws IOException {
        final String b = write("B.json", "{\"shared\": {\"_woodObjectId\": \"5\", \"text\": \"B\"}}");
        final String a = write("A.json", "{" + box(provider("dep", b))
                + ", \"ref\": {\"_woodLink\": \"dep::5\"}}");
        final String e = write("E.json", "{" + box(provider("base", b))
                + ", \"ref\": {\"_woodLink\": \"base::5\"}}");
        write("main.json", "{" + box(provider("subA", a), provider("subE", e)) + ", \"one\": 1}");
    }

    @Test
    public void sharedShapeOpensWithFingerprintPerResource() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        final JsonResource viaSynonym = bundle.findBySynonym("dep");
        assertNotNull(viaSynonym, "B must be findable under the shared synonym");
        assertTrue(viaSynonym.isFresh(), "the fingerprint captured at load matches the file");
        assertTrue(viaSynonym.getFingerprint() != null, "captured at load");
        assertFalse(viaSynonym.isConsumed(), "no resolution ran - REGISTERED");
        bundle.close();
    }

    @Test(dependsOnMethods = "sharedShapeOpensWithFingerprintPerResource")
    public void bIsOneInstanceForBothConsumers() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        final JsonResource b = bundle.findBySynonym("dep");
        final String canonicalB = new File(b.getResourceFile()).getCanonicalPath();
        assertSame(bundle.findByPath(canonicalB), b,
                "B == B' - the synonym lookup and the path lookup share ONE instance");
        assertEquals(bundle.getSortedSynonyms().size(), 4, "self, subA, subE and dep");
        assertTrue(bundle.getWarnings().isEmpty(), "the same synonym twice is the normal share case");
        bundle.close();
    }

    @Test(dependsOnMethods = "bIsOneInstanceForBothConsumers")
    public void aliasSharesTheInstanceAndRebuildsTheSet() throws IOException, JsonParseException {
        buildAliasFixture();
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        final JsonResource asDep = bundle.findBySynonym("dep");
        final JsonResource asBase = bundle.findBySynonym("base");
        assertNotNull(asDep, "B must be findable under its first synonym");
        assertNotNull(asBase, "B must be findable under the reconciled alias");
        assertSame(asDep, asBase, "the alias shares the instance - first synonym wins");
        assertFalse(bundle.getWarnings().isEmpty(), "the alias is collected as a warning");
        assertTrue(bundle.getWarnings().get(0).contains("Alias"), "warning: " + bundle.getWarnings().get(0));
        bundle.close();
    }

    @Test(dependsOnMethods = "aliasSharesTheInstanceAndRebuildsTheSet")
    public void aliasForConsumedInstanceIsRefused() throws IOException, JsonParseException {
        buildAliasFixture();
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        final JsonResource b = bundle.findBySynonym("dep");
        b.markConsumed();

        final JsonResource aliasRequest = JsonResource.forFile(b.getResourceFile());
        aliasRequest.setProviderName("lateAlias");
        try {
            bundle.register(aliasRequest);
            fail("an alias for a CONSUMED instance must be refused - the rebuild would invalidate resolutions");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Alias refused"), "message: " + expected.getMessage());
        }
        bundle.close();
    }

    @Test(dependsOnMethods = "aliasForConsumedInstanceIsRefused")
    public void synonymConflictOfTwoFilesThrows() throws IOException {
        buildSharedFixture("dep");
        final String c = write("C.json", "{\"c\": true}");
        // E-conflict references C under the synonym that A already uses for B - two files, one namespace
        final String eConflict = write("E-conflict.json", "{" + box(provider("dep", c))
                + ", \"ref\": {\"_woodLink\": \"dep::9\"}}");
        write("main-conflict.json", "{" + box(provider("subA", path("A.json")),
                provider("subE", eConflict)) + ", \"one\": 1}");

        try {
            JsonBundle.open(conflictMainFile(), DEBUG);
            fail("the same synonym owned by two different files must throw");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Synonym"), "message: " + expected.getMessage());
        }
    }

    @Test(dependsOnMethods = "synonymConflictOfTwoFilesThrows")
    public void dependentsOfTheGraphNodes() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        assertTrue(bundle.getDependents("subA").contains("self"), "the main box references subA");
        assertTrue(bundle.getDependents("dep").contains("subA"), "A references dep");
        assertTrue(bundle.getDependents("dep").contains("subE"), "E references dep");
        assertTrue(bundle.getDependents("self").isEmpty(), "nobody references the main");
        bundle.close();
    }

    @Test(dependsOnMethods = "dependentsOfTheGraphNodes")
    public void reloadReplacesTheSharedInstance() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        final JsonResource b = bundle.findBySynonym("dep");
        final String canonicalB = new File(b.getResourceFile()).getCanonicalPath();

        // the file changes on disk under the cache
        write("B.json", "{\"shared\": {\"_woodObjectId\": \"5\", \"text\": \"B changed\"}}");
        assertTrue(b.isStale(), "the change under the cache is detected");

        bundle.reload(canonicalB, DEBUG);
        final JsonResource fresh = bundle.findBySynonym("dep");
        assertNotSame(fresh, b, "reload replaces the shared instance");
        assertTrue(fresh.isFresh(), "the fresh parse is FRESH again");
        assertFalse(fresh.isConsumed(), "and REGISTERED again");
        bundle.close();
    }

    @Test(dependsOnMethods = "reloadReplacesTheSharedInstance")
    public void toSystemCarriesTheRegistryInstances() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);

        final JsonSystem sys = bundle.toSystem(bundle.getMainResource());
        assertSame(sys.getMainResource(), bundle.getMainResource());
        assertSame(sys.findResourcesBySynonym("dep"), bundle.findBySynonym("dep"),
                "the system carries the shared bundle instances - no second parse");
        assertEquals(sys.getSortedSynonyms().size(), 4, "the sorted synonyms come from the bundle graph");
        bundle.close();
    }

    @Test(dependsOnMethods = "toSystemCarriesTheRegistryInstances")
    public void closedBundleRefusesRegistryAccess() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);
        bundle.close();

        try {
            bundle.findBySynonym("dep");
            fail("a closed bundle must refuse registry access");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
    }

    @Test(dependsOnMethods = "closedBundleRefusesRegistryAccess")
    public void missingProviderFileIsToleratedWithWarning() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        write("main-missing.json", "{" + box(provider("ghost", path("ghost.json")))
                + ", \"one\": 1}");

        // the same tolerance the convert layer lives by: a missing provider file warns and continues instead
        // of failing the whole bundle (a legacy Open of the same file works too)
        final JsonBundle bundle = JsonBundle.open(new File(tempDir.resolve("main-missing.json").toString()), DEBUG);
        assertNotNull(bundle.getMainResource(), "the bundle must open despite the dangling reference");
        assertFalse(bundle.getWarnings().isEmpty(), "the missing file is collected as a warning");
        assertTrue(bundle.getWarnings().get(0).contains("not found"),
                "warning: " + bundle.getWarnings().get(0));
        assertNull(bundle.findBySynonym("ghost"), "the ghost stays unregistered");
        bundle.close();
    }

    @Test(dependsOnMethods = "missingProviderFileIsToleratedWithWarning")
    public void registeredStaleAcquireReloadsSilently() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);
        final JsonResource b = bundle.findBySynonym("dep");
        assertNotNull(b);
        assertFalse(b.isConsumed(), "sanity: no resolution ran, REGISTERED");

        // the file changes under the cache before any resolution consumed the instance
        write("B.json", "{\"shared\": {\"_woodObjectId\": \"5\", \"text\": \"changed\"}}");
        assertTrue(b.isStale(), "sanity: STALE detected");
        final int warningsBefore = bundle.getWarnings().size();

        final JsonResource acquired = bundle.acquire("dep", new File(path("B.json")), DEBUG);
        assertNotSame(acquired, b, "REGISTERED+STALE silently reloads - nobody consumed the old state");
        assertTrue(acquired.isFresh(), "the fresh parse is FRESH");
        assertEquals(bundle.getWarnings().size(), warningsBefore, "the reload is silent");
        bundle.close();
    }

    @Test(dependsOnMethods = "registeredStaleAcquireReloadsSilently")
    public void consumedStaleAcquireWarnsAndKeeps() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);
        final JsonResource b = bundle.findBySynonym("dep");
        b.markConsumed();

        write("B.json", "{\"shared\": {\"_woodObjectId\": \"5\", \"text\": \"changed again\"}}");
        assertTrue(b.isStale(), "sanity: STALE detected");
        final int warningsBefore = bundle.getWarnings().size();

        final JsonResource acquired = bundle.acquire("dep", new File(path("B.json")), DEBUG);
        assertSame(acquired, b, "CONSUMED+STALE warns and KEEPS - a reload would invalidate resolutions");
        assertEquals(bundle.getWarnings().size(), warningsBefore + 1, "the conflict is reported");
        assertTrue(bundle.getWarnings().get(warningsBefore).contains("CONSUMED + STALE"),
                "warning: " + bundle.getWarnings().get(warningsBefore));
        bundle.close();
    }

    @Test(dependsOnMethods = "consumedStaleAcquireWarnsAndKeeps")
    public void vanishedFileKeepsTheCache() throws IOException, JsonParseException {
        buildSharedFixture("dep");
        final JsonBundle bundle = JsonBundle.open(mainFile(), DEBUG);
        final JsonResource b = bundle.findBySynonym("dep");
        Files.delete(tempDir.resolve("B.json"));
        assertTrue(b.isStale(), "a vanished file counts as STALE");

        final JsonResource acquired = bundle.acquire("dep", new File(path("B.json")), DEBUG);
        assertSame(acquired, b, "nothing to reload - the registered instance stays for this session");
        assertFalse(bundle.getWarnings().isEmpty());
        assertTrue(bundle.getWarnings().get(bundle.getWarnings().size() - 1).contains("vanished"),
                "warning: " + bundle.getWarnings().get(bundle.getWarnings().size() - 1));
        bundle.close();
    }

    private static File mainFile() {
        return tempDir.resolve("main.json").toFile();
    }

    private static File conflictMainFile() {
        return tempDir.resolve("main-conflict.json").toFile();
    }

    private static String path(String name) {
        return tempDir.resolve(name).toAbsolutePath().toString();
    }
}
