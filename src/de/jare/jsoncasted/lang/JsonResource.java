/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.jsoncasted.wood.WoodProviderBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a JSON resource containing parsed content and metadata.
 *
 * <p>
 * A JsonResource encapsulates:</p>
 * <ul>
 * <li>The source file path (if loaded from file)</li>
 * <li>The root {@link JsonNode} of the parsed JSON structure</li>
 * <li>Provider information and imported provider synonyms</li>
 * <li>Linking information for cross-resource references</li>
 * <li>Exception tracking during parsing</li>
 * </ul>
 *
 * <p>
 * Resources can be created empty, from files, or with pre-parsed JSON
 * nodes.</p>
 */
public final class JsonResource {

    private String resourceFile;
    private String providerName;
    private JsonNode root;
    private WoodProviderBox expectedBox;
    private List<String> importedProviderSynonyms;
    private final List<JsonExceptionEntry> exceptions = new ArrayList<>();

    private LinkingSet linkingSet;

    private JsonResource() {
        this.importedProviderSynonyms = new ArrayList<>();
        this.providerName = "self";
    }

    /**
     * Creates an empty JsonResource with no file or content.
     *
     * @return a new empty JsonResource.
     */
    public static JsonResource empty() {
        return new JsonResource();
    }

    /**
     * Creates a JsonResource for the specified file path.
     *
     * @param resourceFile the path to the JSON file.
     * @return a new JsonResource configured for the file.
     */
    public static JsonResource forFile(String resourceFile) {
        JsonResource resource = new JsonResource();
        resource.setResourceFile(resourceFile);
        return resource;
    }

    /**
     * Creates a JsonResource with the specified root node.
     *
     * @param root the root JsonNode of the parsed structure.
     * @return a new JsonResource with the root node set.
     */
    public static JsonResource forRoot(JsonNode root) {
        JsonResource resource = new JsonResource();
        resource.setRoot(root);
        return resource;
    }

    /**
     * Creates a JsonResource with both file path and root node.
     *
     * @param resourceFile the path to the JSON file.
     * @param root the root JsonNode of the parsed structure.
     * @return a new JsonResource with file and root set.
     */
    public static JsonResource loaded(String resourceFile, JsonNode root) {
        JsonResource resource = new JsonResource();
        resource.setResourceFile(resourceFile);
        resource.setRoot(root);
        return resource;
    }

    /**
     * Creates a JsonResource with file path, root node, and imported provider
     * synonyms.
     *
     * @param resourceFile the path to the JSON file.
     * @param root the root JsonNode of the parsed structure.
     * @param importedProviderSynonyms list of imported provider synonyms.
     * @return a new JsonResource with all properties set.
     */
    public static JsonResource loaded(String resourceFile, JsonNode root, List<String> importedProviderSynonyms) {
        JsonResource resource = new JsonResource();
        resource.setResourceFile(resourceFile);
        resource.setRoot(root);
        resource.setImportedProviderSynonyms(importedProviderSynonyms);
        return resource;
    }

    /**
     * Returns the file path of this resource.
     *
     * @return the resource file path, or {@code null} if not set.
     */
    public String getResourceFile() {
        return resourceFile;
    }

    /**
     * Sets the file path for this resource.
     *
     * @param resourceFile the path to the JSON file.
     */
    public void setResourceFile(String resourceFile) {
        this.resourceFile = resourceFile;
    }

    /**
     * Returns the provider name for this resource.
     *
     * @return the provider name.
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the provider name for this resource.
     *
     * @param providerName the provider name to set.
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Returns the root JsonNode of this resource.
     *
     * @return the root node, or {@code null} if not set.
     */
    public JsonNode getRoot() {
        return root;
    }

    /**
     * Sets the root JsonNode for this resource.
     *
     * @param root the root JsonNode to set.
     */
    public void setRoot(JsonNode root) {
        this.root = root;
    }

    /**
     * Returns the expected WoodProviderBox for this resource.
     *
     * @return the expected provider box, or {@code null} if not set.
     */
    public WoodProviderBox getExpectedBox() {
        return expectedBox;
    }

    /**
     * Sets the expected WoodProviderBox for this resource.
     *
     * @param expectedBox the expected provider box to set.
     */
    public void setExpectedBox(WoodProviderBox expectedBox) {
        this.expectedBox = expectedBox;
    }

    /**
     * Returns an unmodifiable list of imported provider synonyms.
     *
     * @return unmodifiable list of imported provider synonyms.
     */
    public List<String> getImportedProviderSynonyms() {
        return Collections.unmodifiableList(importedProviderSynonyms);
    }

    /**
     * Sets the list of imported provider synonyms.
     *
     * @param importedProviderSynonyms the list of synonyms to set, or
     * {@code null} to clear.
     */
    public void setImportedProviderSynonyms(List<String> importedProviderSynonyms) {
        if (importedProviderSynonyms == null) {
            this.importedProviderSynonyms = new ArrayList<>();
        } else {
            this.importedProviderSynonyms = new ArrayList<>(importedProviderSynonyms);
        }
    }

    /**
     * Adds a single imported provider synonym.
     *
     * @param importedProviderSynonym the synonym to add (ignored if null or
     * blank).
     */
    public void addImportedProviderSynonym(String importedProviderSynonym) {
        if (importedProviderSynonym == null || importedProviderSynonym.isBlank()) {
            return;
        }
        this.importedProviderSynonyms.add(importedProviderSynonym);
    }

    /**
     * Checks if this resource has any imported provider synonyms.
     *
     * @return {@code true} if there are imported synonyms, {@code false}
     * otherwise.
     */
    public boolean hasImportedProviderSynonyms() {
        return importedProviderSynonyms != null && !importedProviderSynonyms.isEmpty();
    }

    /**
     * Checks if this resource has a resource file set.
     *
     * @return {@code true} if resource file is set, {@code false} otherwise.
     */
    public boolean hasResourceFile() {
        return resourceFile != null && !resourceFile.isBlank();
    }

    /**
     * Checks if this resource has a root node set.
     *
     * @return {@code true} if root node is set, {@code false} otherwise.
     */
    public boolean hasRoot() {
        return root != null;
    }

    /**
     * Checks if this resource is loaded (has a root node).
     *
     * @return {@code true} if root is not null, {@code false} otherwise.
     */
    public boolean isLoaded() {
        return root != null;
    }

    /**
     * Checks if this resource is unsaved (has root but no file).
     *
     * @return {@code true} if has root but no resource file, {@code false}
     * otherwise.
     */
    public boolean isUnsaved() {
        return root != null && (resourceFile == null || resourceFile.isBlank());
    }

    /**
     * Checks if this resource is a file reference only (no root, has file).
     *
     * @return {@code true} if has file but no root, {@code false} otherwise.
     */
    public boolean isFileReferenceOnly() {
        return root == null && resourceFile != null && !resourceFile.isBlank();
    }

    /**
     * Returns the linking set for cross-resource reference resolution.
     *
     * @return the linking set, or {@code null} if not set.
     */
    public LinkingSet getLinkingSet() {
        return linkingSet;
    }

    /**
     * Sets the linking set for this resource.
     *
     * @param linkingSet the linking set to set.
     */
    public void setLinkingSet(LinkingSet linkingSet) {
        this.linkingSet = linkingSet;
    }

    @Override
    public String toString() {
        return "JsonResource{"
                + "resourceFile='" + resourceFile + '\''
                + ", hasRoot=" + (root != null)
                + ", importedProviderSynonyms=" + importedProviderSynonyms
                + '}';
    }

    /**
     * Adds multiple exception entries to this resource.
     *
     * @param addExceptions the list of exceptions to add.
     */
    public void addExceptions(List<JsonExceptionEntry> addExceptions) {
        exceptions.addAll(addExceptions);
    }

}
