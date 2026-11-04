/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.iotest;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.convertservice.RootConverter;
import de.jare.jsoncasted.io.convertservice.WoodProxyResolver;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.lang.JsonProject;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.impltest.lib.ImplTestDefinition;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * The project-aware io layer of doc/JSON_PROJECT.md (doc/PLAN_JsonProject.md phase 2): the provider
 * resolution asks the registry first, the convert overload builds the system from the project - the shared
 * instance B flows into the resolution instead of a fresh parse. The legacy signatures keep their per-session
 * behavior untouched (project == null) and still demo the old divergence.
 *
 * @author Janusch Rentenatus
 */
public class WoodProxyResolverProjectNGTest {

    private static final JsonDebugLevel DEBUG = JsonDebugLevel.SIMPLE;

    private static Path tempDir;

    @BeforeClass
    public static void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("jsonproject-io");
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

    /**
     * A references B (synonym dep) with a wood link; the project main P references A as subA.
     */
    private String buildFixture() throws IOException {
        final String b = write("B.json", "{\"shared\": {\"_woodObjectId\": \"5\", \"text\": \"B\"}}");
        final String a = write("A.json", "{\"_woodProviders\": [{\"synonym\": \"dep\", \"filename\": \""
                + escaped(b) + "\"}], \"one\": {\"_woodLink\": \"dep::5\"}}");
        return write("P.json", "{\"_woodProviders\": [{\"synonym\": \"subA\", \"filename\": \""
                + escaped(a) + "\"}], \"anything\": 1}");
    }

    @Test
    public void projectAwareResolutionSharesTheProvidersInstance() throws IOException, JsonParseException {
        buildFixture();
        final JsonProject project = JsonProject.open(tempDir.resolve("P.json").toFile(), DEBUG);
        final JsonResource registryB = project.findBySynonym("dep");

        // a per-resource system around the shared A - exactly what a sub-resource convert builds
        final JsonResource sharedA = project.findBySynonym("subA");
        final JsonSystem sys = JsonSystem.of(sharedA);
        WoodProxyResolver.resolveProviders(sys, project, DEBUG);

        assertSame(sys.findResourcesBySynonym("dep"), registryB,
                "B == B' - the io seam serves the registry instance, no second parse");
        assertTrue(registryB.isConsumed(), "the resolution fed with B flips it to CONSUMED");
        assertTrue(sharedA.isConsumed(), "the resolved main is consumed as well");
        project.close();
    }

    @Test(dependsOnMethods = "projectAwareResolutionSharesTheProvidersInstance")
    public void legacyResolutionStillLoadsItsOwnInstance() throws IOException, JsonParseException {
        buildFixture();
        final JsonProject project = JsonProject.open(tempDir.resolve("P.json").toFile(), DEBUG);
        final JsonResource registryB = project.findBySynonym("dep");

        final JsonResource sharedA = project.findBySynonym("subA");
        final JsonSystem sys = JsonSystem.of(sharedA);
        WoodProxyResolver.resolveProviders(sys, DEBUG);

        assertNotSame(sys.findResourcesBySynonym("dep"), registryB,
                "the legacy path keeps its per-session behavior: its own fresh parse (the old divergence)");
        assertFalse(registryB.isConsumed(), "the legacy session knows no project - no consumption flips");
        project.close();
    }

    @Test(dependsOnMethods = "legacyResolutionStillLoadsItsOwnInstance")
    public void convertThroughTheProjectResolvesWithoutSecondParses() throws IOException, JsonParseException {
        buildFixture();
        final JsonProject project = JsonProject.open(tempDir.resolve("P.json").toFile(), DEBUG);
        final JsonResource registryB = project.findBySynonym("dep");

        final WoodResolution resolution = RootConverter.convert(project, project.getMainResource(),
                ImplTestDefinition.getInstance().getRootClass().getcName(),
                ImplTestDefinition.getInstance().getDescriptor(), DEBUG);

        assertNotNull(resolution, "the convert must resolve the main resource");
        assertNotNull(resolution.getAnswer(), "the main resource answers");
        assertTrue(registryB.isConsumed(), "the project-aware resolve consumed the shared B");
        assertTrue(tempDir.resolve("B.json").toFile().exists(), "fixture intact");

        // a second convert of the SAME resource reuses everything: B stays the install instance
        final JsonResource before = project.findBySynonym("dep");
        RootConverter.convert(project, project.getMainResource(),
                ImplTestDefinition.getInstance().getRootClass().getcName(),
                ImplTestDefinition.getInstance().getDescriptor(), DEBUG);
        assertSame(project.findBySynonym("dep"), before, "no reload between two project-aware converts");
        project.close();
    }

    @Test(dependsOnMethods = "convertThroughTheProjectResolvesWithoutSecondParses")
    public void toSystemClosureCarriesOnlyTheReachableProviders() throws IOException, JsonParseException {
        buildFixture();
        // an unrelated E: part of the project graph (referenced by P) but NOT of A's provider closure
        final String e = write("E.json", "{\"e\": true}");
        Files.write(tempDir.resolve("P.json"), ("{\"_woodProviders\": [{\"synonym\": \"subA\", \"filename\": \""
                + escaped(tempDir.resolve("A.json").toAbsolutePath().toString()) + "\"},"
                + "{\"synonym\": \"subE\", \"filename\": \"" + escaped(e) + "\"}], \"anything\": 1}")
                .getBytes(StandardCharsets.UTF_8));
        final JsonProject project = JsonProject.open(tempDir.resolve("P.json").toFile(), DEBUG);

        final JsonResource sharedA = project.findBySynonym("subA");
        final JsonResource projectE = project.findBySynonym("subE");
        assertNotNull(projectE, "E belongs to the project");

        final JsonSystem aSystem = project.toSystem(sharedA);
        assertSame(aSystem.getMainResource(), sharedA);
        assertSame(aSystem.findResourcesBySynonym("dep"), project.findBySynonym("dep"),
                "A's closure contains its provider B");
        assertNull(aSystem.findResourcesBySynonym("subE"),
                "A's closure contains ONLY its providers - E stays outside");
        assertFalse(aSystem.getResources().contains(projectE), "E never leaks into A's resolution");
        project.close();
    }
}
