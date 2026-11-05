/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import de.jare.jsoncasted.io.JsonObjectWriter;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.JsonWriteException;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.item.JsonClass;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Load and save of the project layer documents (concept: CONCEPT_ProjectView.md, sections 1, 2
 * and 4) plus the validation rules of sections 2 and 4. The service works purely on files and
 * the two documents; it holds no state of its own.
 *
 * Rules enforced here (validation, load time):
 * <ul>
 * <li>Partition names are non-empty and unique within the project.</li>
 * <li>Partition type is one of the six known roles.</li>
 * <li>Two partitions must not mount the same physical folder (canonical mount key).</li>
 * <li>Bundle descriptors declare a non-empty name and files list; the main file is part of the
 * files.</li>
 * <li>Every import names a bundle and carries exactly one channel: path (reactor) or version
 * (dependency).</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class ProjectFileService {

    public static final String PROJECT_FOLDER = ".wood";
    public static final String PROJECT_FILE = "project.json";
    public static final String BUNDLE_FILE = ".woodbundle.json";

    private final WoodProjectDefinition definition;

    public ProjectFileService() {
        this.definition = WoodProjectDefinition.getInstance();
    }

    /**
     * @param projectRoot candidate project folder
     * @return true when the folder contains a project document (is a project root)
     */
    public boolean isProjectRoot(File projectRoot) {
        return projectRoot != null && getProjectDocumentFile(projectRoot).isFile();
    }

    public File getProjectDocumentFile(File projectRoot) {
        return new File(new File(projectRoot, PROJECT_FOLDER), PROJECT_FILE);
    }

    public File getBundleDescriptorFile(File bundleFolder) {
        return new File(bundleFolder, BUNDLE_FILE);
    }

    /**
     * Loads the project document out of {@code .wood/project.json} of the given project root.
     *
     * @param projectRoot the folder that contains .wood/project.json
     * @return the parsed document
     * @throws JsonParseException when the document is syntactically broken
     * @throws JsonBuildException when the document does not match the project model
     * @throws IOException when the file cannot be read
     */
    public ProjectDocument loadProject(File projectRoot) throws JsonParseException, JsonBuildException, IOException {
        return (ProjectDocument) loadRoot(getProjectDocumentFile(projectRoot), definition.getProjectRoot());
    }

    /**
     * Saves the project document into {@code .wood/project.json} of the given project root.
     *
     * @param projectRoot the folder that contains .wood/project.json
     * @param document the document to persist
     * @throws IOException when the file cannot be written
     * @throws JsonWriteException when the document cannot be serialized
     * @throws JsonParseException when the model cannot be serialized
     */
    public void saveProject(File projectRoot, ProjectDocument document)
            throws IOException, JsonParseException, JsonWriteException {
        ensureParentDirectory(getProjectDocumentFile(projectRoot));
        JsonObjectWriter.write(document, getProjectDocumentFile(projectRoot), definition, definition.getProjectRoot());
    }

    /**
     * Loads the bundle descriptor ({@code .woodbundle.json}) of the given bundle folder.
     *
     * @param bundleFolder the folder that contains .woodbundle.json
     * @return the parsed descriptor
     * @throws JsonParseException when the descriptor is syntactically broken
     * @throws JsonBuildException when the descriptor does not match the bundle model
     * @throws IOException when the file cannot be read
     */
    public BundleDescriptor loadBundleDescriptor(File bundleFolder)
            throws JsonParseException, JsonBuildException, IOException {
        return (BundleDescriptor) loadRoot(getBundleDescriptorFile(bundleFolder), definition.getBundleRoot());
    }

    /**
     * Saves the bundle descriptor into {@code .woodbundle.json} of the given bundle folder.
     *
     * @param bundleFolder the folder that contains .woodbundle.json
     * @param descriptor the descriptor to persist
     * @throws IOException when the file cannot be written
     * @throws JsonWriteException when the descriptor cannot be serialized
     * @throws JsonParseException when the model cannot be serialized
     */
    public void saveBundleDescriptor(File bundleFolder, BundleDescriptor descriptor)
            throws IOException, JsonParseException, JsonWriteException {
        ensureParentDirectory(getBundleDescriptorFile(bundleFolder));
        JsonObjectWriter.write(descriptor, getBundleDescriptorFile(bundleFolder), definition, definition.getBundleRoot());
    }

    private Object loadRoot(File file, JsonClass root) throws JsonParseException, JsonBuildException, IOException {
        WoodResolution reso = JsonParser.parse(file, definition, root);
        JsonItem item = reso.getAnswer();
        if (item == null) {
            if (reso.hasExceptions() && !reso.getUnmodifiableExceptions().isEmpty()) {
                throw reso.getUnmodifiableExceptions().get(0);
            }
            throw new JsonParseException("Parsing of " + file + " failed.");
        }
        return JsonBuilder.buildInstance(definition.getModel(), true, item);
    }

    /**
     * Validates a project document against the binding rules of the concept.
     *
     * @param projectRoot the folder that contains .wood/project.json (base of relative paths)
     * @param document the document to check
     * @return the problems found, empty when the document is valid
     */
    public List<String> validateProject(File projectRoot, ProjectDocument document) {
        List<String> problems = new ArrayList<>();
        if (document == null) {
            problems.add("No project document.");
            return problems;
        }
        if (document.getName() == null || document.getName().isBlank()) {
            problems.add("Project name is missing.");
        }
        Set<String> seenNames = new HashSet<>();
        Set<String> seenMounts = new HashSet<>();
        for (int i = 0; i < document.getRoots().size(); i++) {
            Partition partition = document.getRoots().get(i);
            String label = "Partition[" + i + "]";
            if (partition.getName() == null || partition.getName().isBlank()) {
                problems.add(label + ": name is missing.");
            } else {
                label = "Partition '" + partition.getName() + "'";
                if (!seenNames.add(partition.getName())) {
                    problems.add(label + ": name is not unique.");
                }
            }
            if (partition.getType() == null) {
                problems.add(label + ": type is missing (src, test, assets, json, gen, target).");
            }
            if (partition.getPath() == null || partition.getPath().isBlank()) {
                problems.add(label + ": path is missing.");
            }
            String mountKey = partition.mountKey(projectRoot);
            if (!mountKey.isEmpty() && !seenMounts.add(mountKey)) {
                problems.add(label + ": mounts a folder that is already mounted by another partition ("
                        + mountKey + ").");
            }
        }
        return problems;
    }

    /**
     * Validates a bundle descriptor against the binding rules of the concept.
     *
     * @param bundleFolder the folder that contains the descriptor (name fallback source)
     * @param descriptor the descriptor to check
     * @return the problems found, empty when the descriptor is valid
     */
    public List<String> validateBundle(File bundleFolder, BundleDescriptor descriptor) {
        List<String> problems = new ArrayList<>();
        if (descriptor == null) {
            problems.add("No bundle descriptor.");
            return problems;
        }
        if (descriptor.getName() == null || descriptor.getName().isBlank()) {
            if (bundleFolder == null) {
                problems.add("Bundle name is missing.");
            } else {
                problems.add("Bundle name is missing (expected: folder name '" + bundleFolder.getName() + "').");
            }
        }
        if (descriptor.getFiles().isEmpty()) {
            problems.add("Bundle '" + descriptor.getName() + "': files list is empty.");
        }
        if (descriptor.getMain() == null || descriptor.getMain().isBlank()) {
            problems.add("Bundle '" + descriptor.getName() + "': main file is missing.");
        } else if (!descriptor.hasFile(descriptor.getMain())) {
            problems.add("Bundle '" + descriptor.getName() + "': main file '" + descriptor.getMain()
                    + "' is not part of the files list.");
        }
        for (int i = 0; i < descriptor.getImports().size(); i++) {
            BundleImport imp = descriptor.getImports().get(i);
            String label = "Bundle '" + descriptor.getName() + "', import[" + i + "]";
            if (imp.getBundle() == null || imp.getBundle().isBlank()) {
                problems.add(label + ": bundle name is missing.");
            }
            if (!imp.hasChannel()) {
                problems.add(label + ": exactly one channel is required - path (reactor) or version (dependency).");
            }
        }
        return problems;
    }

    private void ensureParentDirectory(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("Cannot create directory " + parent);
        }
    }

}
