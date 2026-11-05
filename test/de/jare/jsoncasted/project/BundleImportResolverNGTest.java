package de.jare.jsoncasted.project;

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
 * Regression tests of the import resolution (concept WoodJsonJack CONCEPT_ProjectView.md section
 * 6 - phase 7): the binding resolution order (path reactor, project-local convenience,
 * repository dependency with the materialize-once cache), the strict name check of a wired
 * path import, the guidance of unresolved imports and the conflict rule (two versions of one
 * imported bundle in one project are refused, never silently resolved).
 *
 * @author Janusch Rentenatus
 */
public class BundleImportResolverNGTest {

    private static final ProjectFileService SERVICE = new ProjectFileService();

    private File tempRoot;
    private File projectRoot;
    private File repositoryRoot;
    private BundleImportResolver resolver;

    @BeforeMethod
    public void setUp() throws IOException {
        tempRoot = Files.createTempDirectory("wjj-imports").toFile();
        projectRoot = new File(tempRoot, "myproject");
        repositoryRoot = new File(tempRoot, "repository");
        assertTrue(projectRoot.mkdir());
        assertTrue(repositoryRoot.mkdir());
        resolver = new BundleImportResolver(repositoryRoot);
        buildProject();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        deleteRecursively(tempRoot);
    }

    /**
     * Channel 1 (reactor): a path import resolves direct and project-relative, the resolution
     * carries live reference semantics - it names the wired folder itself, no copy, no cache.
     */
    @Test
    public void testPathImportResolvesProjectRelative() throws Exception {
        File importer = newBundle("myproject/json/profiles/app", "app", importWithPath("helper", "./helpers/h"));
        newBundle("myproject/helpers/h", "helper", null);

        BundleImportResolver.ImportResolution resolution = only(resolver.resolveImports(projectRoot, importer));

        assertEquals(resolution.getChannel(), BundleImportResolver.Channel.PATH);
        assertEquals(resolution.getBundleFolder().getCanonicalPath(),
                new File(projectRoot, "helpers/h").getCanonicalPath());
        assertNull(resolution.getProblem());
        // live reference: nothing materialized anywhere
        assertFalse(new File(projectRoot, ".wood/cache").exists());
    }

    /**
     * The strictness of the wiring: a path import whose found bundle carries a different
     * descriptor name is refused - the wiring would lie.
     */
    @Test
    public void testPathImportRefusesNameMismatch() throws Exception {
        File importer = newBundle("myproject/json/profiles/app", "app", importWithPath("helper", "./helpers/h"));
        newBundle("myproject/helpers/h", "somethingElse", null);

        BundleImportResolver.ImportResolution resolution = only(resolver.resolveImports(projectRoot, importer));

        assertEquals(resolution.getChannel(), BundleImportResolver.Channel.UNRESOLVED);
        assertTrue(resolution.getProblem().contains("somethingElse"),
                "the refusal names the mismatch: " + resolution.getProblem());
    }

    /**
     * Channel 2 (project-local convenience): a version import finds the bundle of that name in
     * one of the project's own partitions - live reference, no cache stand.
     */
    @Test
    public void testVersionImportFindsProjectLocalFirst() throws Exception {
        File importer = newBundle("myproject/json/profiles/app", "app", importWithVersion("base.lib", "1.0"));
        newBundle("myproject/json/profiles/lib/base", "base.lib", null);

        BundleImportResolver.ImportResolution resolution = only(resolver.resolveImports(projectRoot, importer));

        assertEquals(resolution.getChannel(), BundleImportResolver.Channel.PROJECT_LOCAL);
        assertEquals(resolution.getBundleFolder().getCanonicalPath(),
                new File(projectRoot, "json/profiles/lib/base").getCanonicalPath());
        assertFalse(cacheOf("base.lib", "1.0").exists(), "live reference - no cache materialized");
    }

    /**
     * Channel 3 (dependency): a version import materializes the repository entry deep-copy
     * into the project's cache exactly once - the second resolve serves the existing stand
     * without touching the repository (version pinning is the immutability promise).
     */
    @Test
    public void testRepositoryImportMaterializesOnce() throws Exception {
        File importer = newBundle("myproject/json/profiles/app", "app", importWithVersion("base.logging", "1.2"));
        newBundle("repository/base/logging/1.2", "base.logging", null);

        BundleImportResolver.ImportResolution first = only(resolver.resolveImports(projectRoot, importer));
        assertEquals(first.getChannel(), BundleImportResolver.Channel.REPOSITORY);
        File stand = first.getBundleFolder();
        assertEquals(stand, cacheOf("base.logging", "1.2"));
        assertTrue(new File(stand, "base.logging.json").isFile(), "the deep copy carries the files");

        // materialize once: a marker in the stand survives a repo wipe - no refresh happens
        Files.write(new File(stand, "marker.txt").toPath(), "touched".getBytes(StandardCharsets.UTF_8));
        deleteRecursively(new File(repositoryRoot, "base"));
        BundleImportResolver.ImportResolution second = only(resolver.resolveImports(projectRoot, importer));
        assertEquals(second.getChannel(), BundleImportResolver.Channel.REPOSITORY);
        assertEquals(second.getBundleFolder(), stand, "the same stand serves again");
        assertTrue(new File(stand, "marker.txt").isFile(), "never refreshed within a version");
        assertTrue(new File(stand, "base.logging.json").isFile(), "the stand stays complete");
    }

    /**
     * No remote fetch in v1: an import nothing resolves carries the guidance with the expected
     * repository path - an error message with help, not a silent drop.
     */
    @Test
    public void testUnresolvedImportCarriesGuidance() throws Exception {
        File importer = newBundle("myproject/json/profiles/app", "app", importWithVersion("ghost.lib", "9.9"));

        BundleImportResolver.ImportResolution resolution = only(resolver.resolveImports(projectRoot, importer));

        assertEquals(resolution.getChannel(), BundleImportResolver.Channel.UNRESOLVED);
        String problem = resolution.getProblem();
        assertTrue(problem.contains("ghost.lib"), "the guidance names the bundle: " + problem);
        assertTrue(problem.contains("9.9"), "the guidance names the version: " + problem);
        assertTrue(problem.contains(new File(repositoryRoot, "ghost" + File.separator + "9.9").getName()),
                "the guidance names the checked repository place: " + problem);
        assertFalse(resolution.getBundleFolder() != null && resolution.getBundleFolder().isDirectory(),
                "nothing materialized");
    }

    /**
     * The conflict rule (binding): the same imported bundle with two different versions in one
     * project is refused with a clear message; the same version twice is the normal share case.
     */
    @Test
    public void testVersionConflictRefusedSameVersionFine() throws Exception {
        newBundle("myproject/json/profiles/one", "one", importWithVersion("base.logging", "1.1"));
        newBundle("myproject/json/profiles/two", "two", importWithVersion("base.logging", "1.2"));
        newBundle("myproject/json/profiles/three", "three", importWithVersion("base.logging", "1.1"));

        java.util.List<String> conflicts = resolver.findVersionConflicts(projectRoot);
        assertEquals(conflicts.size(), 1, "one conflicted bundle name, not one per importer");
        assertTrue(conflicts.get(0).contains("base.logging"), conflicts.get(0));
        assertTrue(conflicts.get(0).contains("1.1"), conflicts.get(0));
        assertTrue(conflicts.get(0).contains("1.2"), conflicts.get(0));
        assertTrue(conflicts.get(0).contains("refused"), conflicts.get(0));
    }

    //
    // helpers
    //

    /**
     * Fixture: the project carries one json partition (./json) and its .wood/project.json.
     */
    private void buildProject() throws IOException {
        assertTrue(new File(projectRoot, "json/profiles").mkdirs());
        write("myproject/.wood/project.json", "{\n  \"name\": \"myproject\",\n  \"roots\": [\n"
                + "    { \"name\": \"profiles\", \"type\": \"json\", \"path\": \"./json/profiles\" }\n"
                + "  ]\n}\n");
    }

    /**
     * A bundle folder with a descriptor: the main file carries the id/link content when given.
     */
    private File newBundle(String relativePath, String name, BundleImport importEntry)
            throws IOException {
        final File folder = new File(tempRoot, relativePath);
        assertTrue(folder.mkdirs(), "fixture folder: " + relativePath);
        write(relativePath + "/" + name + ".json", "{\"name\": \"" + name + "\"}");
        final BundleDescriptor descriptor = new BundleDescriptor(name, name + ".json");
        if (importEntry != null) {
            descriptor.getImports().add(importEntry);
        }
        try {
            SERVICE.saveBundleDescriptor(folder, descriptor);
        } catch (Exception e) {
            throw new IOException("fixture failed: " + e.getMessage(), e);
        }
        return folder;
    }

    private BundleImport importWithPath(String bundle, String path) {
        return new BundleImport(bundle, path, null);
    }

    private BundleImport importWithVersion(String bundle, String version) {
        return new BundleImport(bundle, null, version);
    }

    private File cacheOf(String bundleName, String version) {
        return BundleImportResolver.cacheFolderFor(projectRoot, bundleName, version);
    }

    private BundleImportResolver.ImportResolution only(java.util.List<BundleImportResolver.ImportResolution> list) {
        assertEquals(list.size(), 1, "the fixture declares exactly one import");
        return list.get(0);
    }

    private void write(String relativePath, String content) throws IOException {
        Path file = tempRoot.toPath().resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    private static void deleteRecursively(File folder) {
        if (!folder.exists()) {
            return;
        }
        File[] entries = folder.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                deleteRecursively(entry);
            }
        }
        folder.delete();
    }

}
