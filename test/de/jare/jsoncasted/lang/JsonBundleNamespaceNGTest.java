package de.jare.jsoncasted.lang;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Regression tests of the bundle-scoped IDs (concept WoodJsonJack CONCEPT_ProjectView.md section 7,
 * strategy B - phase 5 engine prerequisite): the same synonym may be owned by different files as long
 * as they belong to different bundles; within one namespace the identity matrix holds unchanged; the
 * supervised merge refuses colliding Wood ID keys at the join of two namespaces - never a silent
 * overwrite; and the explicitly wired import opens the cross-bundle resolution scope (own namespace
 * first, then the wired ones).
 *
 * @author Janusch Rentenatus
 */
public class JsonBundleNamespaceNGTest {

    private static final JsonDebugLevel DEBUG = JsonDebugLevel.SIMPLE;

    private Path tempDir;
    private JsonBundle bundle;

    @BeforeMethod
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("jsonbundle-namespace");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        if (bundle != null) {
            bundle.close();
        }
        for (Path child : Files.list(tempDir).toList()) {
            Files.deleteIfExists(child);
        }
        Files.deleteIfExists(tempDir);
    }

    /**
     * Strategy B core: two files of two bundles may own the SAME synonym - {@code save::…} of
     * bundle one and {@code save::…} of bundle two are different worlds. Both register, both stay
     * findable through their own namespace.
     */
    @Test
    public void testSameSynonymInTwoNamespacesCoexists() throws IOException, JsonParseException {
        bundle = openPlain();
        final JsonResource one = resource("one.json", "save", "config1");
        final JsonResource two = resource("two.json", "save", "config1_copy");

        final JsonResource registeredOne = bundle.register(one);
        final JsonResource registeredTwo = bundle.register(two);
        assertSame(registeredOne, bundle.findBySynonym("config1", "save"),
                "config1 owns its own save file");
        assertSame(registeredTwo, bundle.findBySynonym("config1_copy", "save"),
                "config1_copy owns its own save file - no exception");
        assertEquals(bundle.findBySynonym("config1", "save").getResourceFile(), one.getResourceFile());
        assertEquals(bundle.findBySynonym("config1_copy", "save").getResourceFile(), two.getResourceFile());
    }

    /**
     * The identity matrix holds unchanged WITHIN a namespace (concept section 7, consequence 2):
     * the same synonym on a different path of the SAME bundle still throws.
     */
    @Test
    public void testSameSynonymSameNamespaceStillThrows() throws IOException, JsonParseException {
        bundle = openPlain();
        bundle.register(resource("one.json", "save", "config1"));
        try {
            bundle.register(resource("two.json", "save", "config1"));
            fail("Expected a JsonParseException - one namespace, two files cannot both own it");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("already taken"),
                    "the matrix error names the conflict: " + expected.getMessage());
            assertTrue(expected.getMessage().contains("config1"),
                    "the matrix error names the namespace: " + expected.getMessage());
        }
    }

    /**
     * The legacy lane (no namespace labels - the session default) behaves exactly as before
     * bundle scoping: the same synonym on a different path throws.
     */
    @Test
    public void testDefaultNamespaceKeepsTheLegacyMatrix() throws IOException, JsonParseException {
        bundle = openPlain();
        bundle.register(resource("one.json", "save", null));
        try {
            bundle.register(resource("two.json", "save", null));
            fail("Expected a JsonParseException in the session default namespace");
        } catch (JsonParseException expected) {
            // the legacy message and rule, unchanged
        }
    }

    /**
     * The supervised merge (strategy B): joining two namespaces in one resolution scope refuses
     * on colliding Wood ID keys. No silent overwrite - the explicit RerollID preparation is the
     * remedy (concept section 7.1).
     */
    @Test
    public void testSupervisedMergeRefusesCrossNamespaceCollision() throws IOException, JsonParseException {
        final JsonResource targetOwner = resource("one.json", "self", "config1");
        final JsonResource sourceOwner = resource("two.json", "save", "config1_copy");
        final LinkingSet target = new LinkingSet("self");
        target.getObjectIdMap().put("save::78", null);
        final LinkingSet source = new LinkingSet("save");
        source.getObjectIdMap().put("save::78", null);

        try {
            JsonBundle.mergeSupervised(targetOwner, target, sourceOwner, source);
            fail("Expected a JsonParseException for the colliding ID keys");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("save::78"),
                    "the refusal names the colliding keys: " + expected.getMessage());
            assertTrue(expected.getMessage().contains("RerollID"),
                    "the refusal names the remedy: " + expected.getMessage());
            assertFalse(target.getObjectIdMap().containsKey("added"),
                    "nothing merged - refused means refused");
        }
    }

    /**
     * Within one namespace the merge behaves as before: keys flow into the receiver's set, no
     * supervision between resources of the same world.
     */
    @Test
    public void testSupervisedMergeWithinNamespaceMergesAsBefore() throws IOException, JsonParseException {
        final JsonResource targetOwner = resource("one.json", "self", "config1");
        final JsonResource sourceOwner = resource("two.json", "save", "config1");
        final LinkingSet target = new LinkingSet("self");
        target.getObjectIdMap().put("save::78", null);
        final LinkingSet source = new LinkingSet("save");
        source.getObjectIdMap().put("save::90", null);

        JsonBundle.mergeSupervised(targetOwner, target, sourceOwner, source);
        assertTrue(target.getObjectIdMap().containsKey("save::90"), "the source keys merged in");
        assertEquals(target.getObjectIdMap().size(), 2);
    }

    /**
     * The wire (the descriptor imports, concept sections 6 and 7): wiring two namespaces refuses
     * when their registered ID key sets collide - the reroll is the explicit preparation. Disjoint
     * keys wire cleanly.
     */
    @Test
    public void testWireRefusesCollidingRegisteredIds() throws IOException, JsonParseException {
        bundle = openPlain();
        final JsonResource one = resource("one.json", "save", "config1");
        one.setLinkingSet(idSet("save", "save::78"));
        bundle.register(one);
        final JsonResource two = resource("two.json", "save", "config1_copy");
        two.setLinkingSet(idSet("save", "save::78"));
        bundle.register(two);

        try {
            bundle.wire("config1", "config1_copy");
            fail("Expected a JsonParseException - the namespaces collide in one resolution scope");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("collision"), expected.getMessage());
        }
    }

    /**
     * The wired import opens the resolution scope (strategy B + concept section 6): a link of the
     * asking bundle resolves into the wired bundle - cross-bundle resolution exists only through the
     * declared wiring, never implicitly.
     */
    @Test
    public void testWiredNamespaceResolvesInScope() throws IOException, JsonParseException {
        final String imp = write("imp.json", "{\"shared\": {\"_woodObjectId\": \"7\", \"text\": \"B\"}}");
        final String subA = write("A.json", "{\"data\": {\"_woodObjectId\": \"1\", \"text\": \"A\"}}");
        final String main = write("main_a.json",
                "{\"" + "_woodProviders" + "\": [{\"synonym\": \"imp\", \"filename\": \""
                        + escaped(imp) + "\"}, {\"synonym\": \"subA\", \"filename\": \"" + escaped(subA)
                        + "\"}], \"ref\": {\"_woodLink\": \"imp::7\"}}");

        // the wiring layer decides the owning bundle of every provider file: b/imp.json belongs to b
        bundle = JsonBundle.open(new File(main), "config1",
                file -> file.getName().equals("imp.json") ? "base.logging" : null, DEBUG);
        bundle.wire("config1", "base.logging");

        final JsonResource mainResource = bundle.getMainResource();
        assertNotNull(bundle.findBySynonym("base.logging", "imp"),
                "the imported file registered under its own namespace");
        assertNotNull(bundle.findBySynonym("config1", "subA"), "the own file registered under config1");

        final JsonSystem sys = bundle.toSystem(mainResource);
        assertTrue(sys.getResources().contains(bundle.findBySynonym("base.logging", "imp")),
                "the system of the asking bundle resolves the wired provider");
        assertTrue(mainResource.getLinkingSet().getObjectIdMap().containsKey("imp::7"),
                "the supervised merge brought the imported ID into the asking resolution scope");
    }

    //
    // helpers
    //

    /**
     * A plain bundle on a dummy main file without provider references - the registration lane under
     * test only.
     */
    private JsonBundle openPlain() throws IOException, JsonParseException {
        final String main = write("main_plain.json", "{}");
        return JsonBundle.open(new File(main), DEBUG);
    }

    private JsonResource resource(String name, String synonym, String namespace) throws IOException {
        final JsonResource resource = JsonResource.forFile(tempDir.resolve(name).toAbsolutePath().toString());
        Files.write(tempDir.resolve(name), "{}".getBytes(StandardCharsets.UTF_8));
        resource.setProviderName(synonym);
        resource.setNamespace(namespace);
        return resource;
    }

    private LinkingSet idSet(String synonym, String... keys) {
        final LinkingSet setOfIds = new LinkingSet(synonym);
        for (String key : keys) {
            setOfIds.getObjectIdMap().put(key, null);
        }
        return setOfIds;
    }

    private String write(String name, String content) throws IOException {
        final Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file.toAbsolutePath().toString();
    }

    private static String escaped(String path) {
        return path.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
