/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Phase 1 of the project view concept (CONCEPT_ProjectView.md): the project document
 * ({@code .wood/project.json}), the bundle descriptor ({@code .woodbundle.json}), their round
 * trip through the existing JSON machinery and the binding validation rules of sections 2 and 4.
 *
 * @author Janusch Rentenatus
 */
public class WoodProjectNGTest {

    private static Path tempDir;
    private static ProjectFileService service;

    @BeforeClass
    public static void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("woodproject-model");
        service = new ProjectFileService();
    }

    @AfterClass
    public static void removeTempDir() throws IOException {
        if (tempDir != null) {
            deleteRecursively(tempDir.toFile());
        }
    }

    private static void deleteRecursively(File folder) throws IOException {
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        Files.deleteIfExists(folder.toPath());
    }

    private File newProjectRoot(String name) throws IOException {
        File root = new File(tempDir.toFile(), name);
        assertTrue(root.mkdirs() || root.isDirectory());
        return root;
    }

    @Test
    public void defaultProjectRoundTrip() throws Exception {
        File projectRoot = newProjectRoot("roundtrip");
        ProjectDocument document = new ProjectDocument("roundtrip-project");
        document.getRoots().add(new Partition("src", PartitionType.SRC, "./src", null));
        document.getRoots().add(new Partition("generated", PartitionType.GEN, "D:/wood/generated", true));

        assertFalse(service.isProjectRoot(projectRoot));
        service.saveProject(projectRoot, document);
        assertTrue(service.isProjectRoot(projectRoot));

        ProjectDocument loaded = service.loadProject(projectRoot);
        assertEquals(loaded.getName(), "roundtrip-project");
        assertEquals(loaded.getRoots().size(), 2);

        Partition src = loaded.findPartition("src");
        assertNotNull(src);
        assertEquals(src.getType(), PartitionType.SRC);
        assertFalse(src.isReadonly());
        assertEquals(src.resolve(projectRoot), new File(projectRoot, "src"));

        Partition gen = loaded.findPartition("generated");
        assertNotNull(gen);
        assertEquals(gen.getType(), PartitionType.GEN);
        assertTrue(gen.isReadonly());
        assertEquals(gen.resolve(projectRoot), new File("D:/wood/generated"));

        assertTrue(service.validateProject(projectRoot, loaded).isEmpty());
    }

    @Test
    public void absoluteMountStaysAbsolute() throws IOException {
        File projectRoot = newProjectRoot("absolute");
        Partition external = new Partition("ext", PartitionType.ASSETS, "D:/elsewhere/assets", null);
        assertEquals(external.resolve(projectRoot), new File("D:/elsewhere/assets"));
        assertEquals(external.mountKey(projectRoot), new File("D:/elsewhere/assets").getCanonicalPath());
    }

    @Test
    public void duplicateMountIsRejected() throws IOException {
        File projectRoot = newProjectRoot("duplicates");
        ProjectDocument document = new ProjectDocument("duplicates");
        document.getRoots().add(new Partition("one", PartitionType.SRC, "./same", null));
        document.getRoots().add(new Partition("two", PartitionType.TEST, "./same", null));

        assertEquals(service.validateProject(projectRoot, document).size(), 1);
    }

    @Test
    public void projectProblemsAreReported() throws IOException {
        File projectRoot = newProjectRoot("problems");
        ProjectDocument document = new ProjectDocument(null);
        document.getRoots().add(new Partition(null, null, null, null));

        assertEquals(service.validateProject(projectRoot, document).size(), 4);
    }

    @Test
    public void bundleDescriptorRoundTrip() throws Exception {
        File projectRoot = newProjectRoot("bundle");
        File bundleFolder = new File(projectRoot, "config1");
        assertTrue(bundleFolder.mkdirs());

        BundleDescriptor descriptor = new BundleDescriptor("config1", "config1.json");
        descriptor.getFiles().add("configDesc.json");
        descriptor.getImports().add(new BundleImport("profile.base", "../profile-base", null));
        descriptor.getImports().add(new BundleImport("base.logging", null, "1.2"));

        service.saveBundleDescriptor(bundleFolder, descriptor);
        BundleDescriptor loaded = service.loadBundleDescriptor(bundleFolder);

        assertEquals(loaded.getName(), "config1");
        assertEquals(loaded.getMain(), "config1.json");
        assertEquals(loaded.getFiles().size(), 2);
        assertTrue(loaded.hasFile("configDesc.json"));
        assertEquals(loaded.getImports().size(), 2);

        BundleImport reactor = loaded.getImports().get(0);
        assertTrue(reactor.isReactor());
        assertEquals(reactor.getPath(), "../profile-base");

        BundleImport dependency = loaded.getImports().get(1);
        assertTrue(dependency.isDependency());
        assertEquals(dependency.getVersion(), "1.2");

        assertTrue(service.validateBundle(bundleFolder, loaded).isEmpty());
    }

    @Test
    public void bundleProblemsAreReported() throws IOException {
        File projectRoot = newProjectRoot("bundle-problems");
        File bundleFolder = new File(projectRoot, "broken");
        assertTrue(bundleFolder.mkdirs());

        BundleDescriptor descriptor = new BundleDescriptor("broken", "main.json");
        descriptor.setMain("other.json");
        descriptor.setFiles(java.util.Collections.emptyList());
        descriptor.getImports().add(new BundleImport("both", "../path", "1.2"));
        descriptor.getImports().add(new BundleImport(null, null, null));

        assertEquals(service.validateBundle(bundleFolder, descriptor).size(), 5);
    }

    @Test
    public void partitionTypeEnumParsesTheSixRoles() {
        assertEquals(PartitionType.getByName("src"), PartitionType.SRC);
        assertEquals(PartitionType.getByName("test"), PartitionType.TEST);
        assertEquals(PartitionType.getByName("assets"), PartitionType.ASSETS);
        assertEquals(PartitionType.getByName("json"), PartitionType.JSON);
        assertEquals(PartitionType.getByName("gen"), PartitionType.GEN);
        assertEquals(PartitionType.getByName("target"), PartitionType.TARGET);
        assertNull(PartitionType.getByName("unknown"));
    }

    /**
     * The role defaults of concept section 2: gen is output meant to be read and target is
     * build output - both are readonly without an explicit flag; the editable roles stay
     * editable; an explicit flag wins over any default (gen mounted writable on purpose).
     */
    @Test
    public void readonlyDefaultsFollowTheRole() {
        assertFalse(new Partition("src", PartitionType.SRC, "./src", null).isReadonly());
        assertFalse(new Partition("json", PartitionType.JSON, "./json", null).isReadonly());
        assertTrue(new Partition("gen", PartitionType.GEN, "./gen", null).isReadonly());
        assertTrue(new Partition("target", PartitionType.TARGET, "./target", null).isReadonly());

        assertFalse(new Partition("genwritable", PartitionType.GEN, "./gen", false).isReadonly());
        assertTrue(new Partition("srclocked", PartitionType.SRC, "./src", true).isReadonly());
    }

    @Test
    public void writtenProjectDocumentIsReadableJson() throws Exception {
        File projectRoot = newProjectRoot("readable");
        ProjectDocument document = new ProjectDocument("readable");
        document.getRoots().add(new Partition("profiles", PartitionType.JSON, "./json/profiles", null));
        service.saveProject(projectRoot, document);

        File projectFile = service.getProjectDocumentFile(projectRoot);
        assertTrue(projectFile.isFile());
        String content = Files.readString(projectFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("\"name\""));
        assertTrue(content.contains("\"profiles\""));
        assertTrue(content.contains("\"json\""));
    }
}
