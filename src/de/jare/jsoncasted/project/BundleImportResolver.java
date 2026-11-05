/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The import resolution of the descriptor input structure (concept WoodJsonJack
 * CONCEPT_ProjectView.md section 6 - the Maven analogy): the resolution order is binding -
 * path imports resolve direct and project-relative (reactor, live reference); a bundle of the
 * wanted name found in one of the project's own partitions is the project-local convenience
 * hit; a version import materializes the local bundle repository deep-copy into the project's
 * import cache, exactly once (version pinning is the immutability promise - the cache is never
 * refreshed within a version); everything else stays unresolved with guidance - there is no
 * remote fetch in v1.
 *
 * <p>
 * Strict by decision (binding): no transitive resolution - the imported bundle's own imports
 * are shown, each importer wires what it wants explicitly. No conflict magic - two different
 * versions of the same imported bundle in one project are refused with a clear message, never
 * a silent nearest-wins.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public class BundleImportResolver {

    /**
     * The channel an import resolved through (concept section 6): the reactor path import, the
     * project-local convenience hit, the materialized repository dependency - or unresolved.
     */
    public enum Channel {
        PATH, PROJECT_LOCAL, REPOSITORY, UNRESOLVED
    }

    /**
     * One resolved import: the entry from the descriptor, the channel it resolved through, the
     * bundle folder (repository imports resolve to their per-project cache stand) or the
     * guidance problem when nothing matched.
     */
    public static final class ImportResolution {

        private final BundleImport entry;
        private final Channel channel;
        private final File bundleFolder;
        private final String problem;

        private ImportResolution(BundleImport entry, Channel channel, File bundleFolder, String problem) {
            this.entry = entry;
            this.channel = channel;
            this.bundleFolder = bundleFolder;
            this.problem = problem;
        }

        public BundleImport getEntry() {
            return entry;
        }

        public Channel getChannel() {
            return channel;
        }

        public File getBundleFolder() {
            return bundleFolder;
        }

        /**
         * @return the guidance line when {@link #getChannel()} is UNRESOLVED, or null
         */
        public String getProblem() {
            return problem;
        }
    }

    /** The repository root of a default resolver: {@code ~/.wood/repository}. */
    public static final String DEFAULT_REPOSITORY = ".wood" + File.separator + "repository";

    private final File repositoryRoot;
    private final ProjectFileService service = new ProjectFileService();

    /**
     * The default resolver over the user's local bundle repository ({@code ~/.wood/repository},
     * analogous to {@code ~/.m2}).
     */
    public BundleImportResolver() {
        this(new File(System.getProperty("user.home"), DEFAULT_REPOSITORY));
    }

    /**
     * A resolver over an explicit repository root (tests, tools).
     *
     * @param repositoryRoot the repository root, not null.
     */
    public BundleImportResolver(File repositoryRoot) {
        this.repositoryRoot = Objects.requireNonNull(repositoryRoot, "repositoryRoot must not be null");
    }

    /**
     * Resolves every import of the given bundle's descriptor through the binding resolution
     * order. Transitive imports stay unresolved by design - the imported bundle's own imports
     * are the next dialog's content, never silently wired.
     *
     * @param projectRoot the folder that contains .wood/project.json, not null.
     * @param bundleFolder the importing bundle folder (with .woodbundle.json), not null.
     * @return the resolution per import, in descriptor order, not null.
     */
    public List<ImportResolution> resolveImports(File projectRoot, File bundleFolder) {
        Objects.requireNonNull(projectRoot, "projectRoot must not be null");
        Objects.requireNonNull(bundleFolder, "bundleFolder must not be null");
        final List<ImportResolution> resolutions = new ArrayList<>();
        try {
            final List<BundleImport> imports = service.loadBundleDescriptor(bundleFolder).getImports();
            for (BundleImport entry : imports) {
                resolutions.add(resolveOne(projectRoot, entry));
            }
        } catch (Exception unreadable) {
            final ImportResolution failed = new ImportResolution(null, Channel.UNRESOLVED, null,
                    "The bundle descriptor of " + bundleFolder.getName()
                    + " cannot be read: " + unreadable.getMessage());
            resolutions.add(failed);
        }
        return resolutions;
    }

    //
    // the binding resolution order (concept section 6)
    //

    private ImportResolution resolveOne(File projectRoot, BundleImport entry) {
        if (entry == null || entry.getBundle() == null || entry.getBundle().isBlank()) {
            return new ImportResolution(entry, Channel.UNRESOLVED, null,
                    "The import declares no bundle name.");
        }
        if (entry.getPath() != null && !entry.getPath().isBlank()) {
            return resolvePathImport(projectRoot, entry);
        }
        return resolveVersionImport(projectRoot, entry);
    }

    /**
     * Channel 1 (reactor): direct and project-relative - the path carries the wiring, live
     * reference. The name strictness is deliberate: the declared bundle name must match the
     * found descriptor's name, otherwise the wiring lies and is refused with guidance.
     */
    private ImportResolution resolvePathImport(File projectRoot, BundleImport entry) {
        final File folder = normalized(new File(projectRoot, entry.getPath()));
        final String problem = pathProblem(folder, entry);
        if (problem != null) {
            return new ImportResolution(entry, Channel.UNRESOLVED, null, problem);
        }
        return new ImportResolution(entry, Channel.PATH, folder, null);
    }

    /**
     * Kills the {@code .} segments of a project-relative path - a reactor import resolves to
     * the same folder whether it is spelled {@code ./helpers/h} or {@code helpers/h}.
     */
    private static File normalized(File folder) {
        try {
            return folder.getCanonicalFile();
        } catch (IOException e) {
            return folder.getAbsoluteFile();
        }
    }

    /**
     * Channels 2 and 3: the project-local convenience hit comes before the repository - a bundle
     * of the wanted name in one of the project's own partitions wins (it is right there, ready
     * to touch); a version pin materializes the repository entry exactly once into the
     * project's import cache; nothing else exists in v1.
     */
    private ImportResolution resolveVersionImport(File projectRoot, BundleImport entry) {
        final File local = findProjectLocalBundle(projectRoot, entry.getBundle());
        if (local != null) {
            return new ImportResolution(entry, Channel.PROJECT_LOCAL, local, null);
        }
        if (entry.getVersion() == null || entry.getVersion().isBlank()) {
            return new ImportResolution(entry, Channel.UNRESOLVED, null,
                    "The import of '" + entry.getBundle() + "' declares neither path nor version -"
                    + " nothing to resolve.");
        }
        final File cached = materializeFromRepository(projectRoot, entry.getBundle(), entry.getVersion());
        if (cached != null) {
            return new ImportResolution(entry, Channel.REPOSITORY, cached, null);
        }
        return new ImportResolution(entry, Channel.UNRESOLVED, null,
                "No bundle '" + entry.getBundle() + "' " + entry.getVersion()
                + " in the local repository (" + repositoryFolderFor(entry.getBundle(),
                entry.getVersion()).getAbsolutePath() + ") - install it by hand (v1) or check"
                + " the path.");
    }

    /**
     * The strictness of a path hit: the folder must exist, be a bundle and carry the EXACT
     * declared name in its descriptor - a wired import whose name mismatches the found bundle
     * lies about the wiring.
     */
    private String pathProblem(File folder, BundleImport entry) {
        if (!folder.isDirectory()) {
            return "The path import of '" + entry.getBundle() + "' points at a missing folder: "
                    + folder.getAbsolutePath();
        }
        if (!service.getBundleDescriptorFile(folder).isFile()) {
            return "The path import of '" + entry.getBundle() + "' points at a folder without a"
                    + " bundle descriptor: " + folder.getAbsolutePath();
        }
        try {
            final String name = service.loadBundleDescriptor(folder).getName();
            if (!Objects.equals(name, entry.getBundle())) {
                return "The path import of '" + entry.getBundle() + "' resolves to a bundle named '"
                        + name + "' - the wiring would lie; refused.";
            }
        } catch (Exception unreadable) {
            return "The bundle descriptor of the path import of '" + entry.getBundle()
                    + "' cannot be read: " + folder.getAbsolutePath();
        }
        return null;
    }

    /**
     * Channel 2 (project-local convenience): the bundle of that name in one of the project's
     * own partitions - recursively searched, the live partitions first and the isolated sources
     * LAST (a same-named live bundle always wins - an old isolated version never shadows the
     * current one; the isolated hit happens only when nothing live carries the name). Two
     * hits of the same name in one project are ambiguous and refused by the conflict check of
     * the project scan.
     *
     * @param projectRoot the folder that contains .wood/project.json
     * @param bundleName the wanted bundle name
     * @return the bundle folder of that name, or null when no partition carries it
     */
    private File findProjectLocalBundle(File projectRoot, String bundleName) {
        final ProjectDocument document = loadProjectOrNull(projectRoot);
        if (document == null) {
            return null;
        }
        final List<Partition> isolatedLate = new ArrayList<>();
        for (Partition partition : document.getRoots()) {
            if (partition.getType() == PartitionType.ISOLATED) {
                isolatedLate.add(partition); // isolated sources serve only as the last resort
                continue;
            }
            final File hit = searchBundleIn(partition, projectRoot, bundleName);
            if (hit != null) {
                return hit;
            }
        }
        for (Partition partition : isolatedLate) {
            final File hit = searchBundleIn(partition, projectRoot, bundleName);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    /**
     * One partition's recursive search for a bundle folder of the wanted name.
     */
    private File searchBundleIn(Partition partition, File projectRoot, String bundleName) {
        final File partitionFolder = partition.resolve(projectRoot);
        if (partitionFolder == null || !partitionFolder.isDirectory()) {
            return null;
        }
        return findBundleFolderNamed(partitionFolder, bundleName);
    }

    /**
     * Recursive search: a folder with a descriptor of exactly the wanted name.
     */
    private File findBundleFolderNamed(File folder, String bundleName) {
        final File[] entries = folder.listFiles();
        if (entries == null) {
            return null;
        }
        final List<File> subFolders = new ArrayList<>();
        for (File entry : entries) {
            if (!entry.isDirectory()) {
                continue;
            }
            if (service.getBundleDescriptorFile(entry).isFile()) {
                try {
                    if (bundleName.equals(service.loadBundleDescriptor(entry).getName())) {
                        return entry;
                    }
                } catch (Exception unreadable) {
                    // a broken descriptor cannot be a hit - the resolver stays honest
                }
            }
            subFolders.add(entry);
        }
        for (File subFolder : subFolders) {
            final File hit = findBundleFolderNamed(subFolder, bundleName);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    //
    // the materialize-once repository cache (concept section 6, tendency of question 5:
    // per project - the project stays self-contained)
    //

    /**
     * Channel 3 (dependency): the repository entry deep-copies into the project's import cache
     * ({@code .wood/cache/<name>/<version>/}), exactly once - a second resolve serves the
     * existing cache stand, the version pinning is the immutability promise. The repository
     * file itself is never edited through an import.
     *
     * @return the cache stand of the imported bundle, or null when the repository lacks it.
     */
    private File materializeFromRepository(File projectRoot, String bundleName, String version) {
        final File cacheFolder = cacheFolderFor(projectRoot, bundleName, version);
        if (cacheFolder.isDirectory()) {
            return cacheFolder; // materialized once - never refreshed within a version
        }
        final File repositoryFolder = repositoryFolderFor(bundleName, version);
        if (!repositoryFolder.isDirectory() || !service.getBundleDescriptorFile(repositoryFolder).isFile()) {
            return null;
        }
        try {
            copyFolder(repositoryFolder, cacheFolder);
        } catch (IOException e) {
            return null; // guidance comes from the unresolved message
        }
        return cacheFolder;
    }

    private File repositoryFolderFor(String bundleName, String version) {
        return new File(repositoryRoot, bundleName.replace('.', File.separatorChar)
                + File.separatorChar + version);
    }

    /**
     * The per-project import cache: {@code .wood/cache/<name-path>/<version>/}.
     */
    public static File cacheFolderFor(File projectRoot, String bundleName, String version) {
        return new File(new File(new File(projectRoot, ProjectFileService.PROJECT_FOLDER), "cache"),
                bundleName.replace('.', File.separatorChar) + File.separatorChar + version);
    }

    private void copyFolder(File source, File target) throws IOException {
        if (!target.mkdirs() && !target.isDirectory()) {
            throw new IOException("Could not create folder: " + target.getAbsolutePath());
        }
        final File[] entries = source.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            final File copy = new File(target, entry.getName());
            if (entry.isDirectory()) {
                copyFolder(entry, copy);
            } else {
                Files.copy(entry.toPath(), copy.toPath());
            }
        }
    }

    //
    // the conflict rule (binding): no magic - two versions of one imported bundle in one
    // project are refused with a clear message
    //

    /**
     * Scans the project's own bundles for version imports and collects the clear refusals of
     * the conflict rule: the same imported bundle name with TWO DIFFERENT versions in one
     * project. The same version twice is the normal share case, not a conflict.
     *
     * @param projectRoot the folder that contains .wood/project.json, not null.
     * @return the refusal lines (one per conflicted bundle name), empty when clean.
     */
    public List<String> findVersionConflicts(File projectRoot) {
        Objects.requireNonNull(projectRoot, "projectRoot must not be null");
        final Map<String, Set<String>> versionsByBundle = new LinkedHashMap<>();
        final Map<String, List<File>> importersByBundle = new LinkedHashMap<>();
        final ProjectDocument document = loadProjectOrNull(projectRoot);
        if (document != null) {
            // the conflict scan sees the LIVE project only: isolated sources are view-only old
            // worlds (their own imports included) and never take part in the identity conflict
            for (Partition partition : document.getRoots()) {
                if (partition.getType() == PartitionType.ISOLATED) {
                    continue;
                }
                final File partitionFolder = partition.resolve(projectRoot);
                if (partitionFolder != null && partitionFolder.isDirectory()) {
                    collectVersionImports(partitionFolder, versionsByBundle, importersByBundle);
                }
            }
        }
        final List<String> refusals = new ArrayList<>();
        for (Map.Entry<String, Set<String>> conflict : versionsByBundle.entrySet()) {
            if (conflict.getValue().size() > 1) {
                final List<File> importers = importersByBundle.get(conflict.getKey());
                refusals.add("Import conflict: '" + conflict.getKey() + "' is imported as "
                        + new LinkedHashSet<>(conflict.getValue()) + " by " + fileNames(importers)
                        + " - two versions of one imported bundle in one project are refused,"
                        + " pin them to one version (RerollID-equivalent thinking).");
            }
        }
        return refusals;
    }

    private void collectVersionImports(File folder, Map<String, Set<String>> versionsByBundle,
            Map<String, List<File>> importersByBundle) {
        if (!folder.isDirectory()) {
            return;
        }
        if (service.getBundleDescriptorFile(folder).isFile()) {
            try {
                for (BundleImport entry : service.loadBundleDescriptor(folder).getImports()) {
                    if (entry.getBundle() != null && entry.getVersion() != null) {
                        versionsByBundle.computeIfAbsent(entry.getBundle(), k -> new LinkedHashSet<>())
                                .add(entry.getVersion());
                        importersByBundle.computeIfAbsent(entry.getBundle(), k -> new ArrayList<>())
                                .add(folder);
                    }
                }
            } catch (Exception unreadable) {
                // the resolver reports what it can read; broken descriptors surface elsewhere
            }
        }
        final File[] entries = folder.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isDirectory()) {
                    collectVersionImports(entry, versionsByBundle, importersByBundle);
                }
            }
        }
    }

    private String fileNames(List<File> folders) {
        final List<String> names = new ArrayList<>();
        for (File folder : folders) {
            names.add(folder.getName());
        }
        return names.toString();
    }

    private ProjectDocument loadProjectOrNull(File projectRoot) {
        try {
            return service.loadProject(projectRoot);
        } catch (Exception unreadable) {
            return null;
        }
    }

}
