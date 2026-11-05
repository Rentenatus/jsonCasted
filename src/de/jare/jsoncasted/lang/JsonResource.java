/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import static de.jare.jsoncasted.lang.JsonTerms.SELF_SYNONYM;
import de.jare.jsoncasted.wood.WoodProviderBox;
import java.io.File;
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
 * Resources can be created empty, from files, or with pre-parsed JSON nodes.</p>
 */
public final class JsonResource {

    private String resourceFile;
    private String providerName;
    // Bundle-scoped IDs (concept WoodJsonJack CONCEPT_ProjectView.md section 7, strategy B): the
    // namespace label of the owning bundle - null means the session default namespace, which is
    // the single-world behavior of every session without a project layer.
    private String namespace;
    private JsonNode root;
    private WoodProviderBox expectedBox;
    private List<String> importedProviderSynonyms;
    private final List<JsonExceptionEntry> exceptions = new ArrayList<>();
    private List<JsonNode> definitionNodes;

    private LinkingSet linkingSet;

    // State model of doc/JSON_BUNDLE.md: every file carries its own status and fingerprint - diff and merge must
    // also work for a standalone resource outside any bundle. The consumption axis: REGISTERED (parsed, not yet
    // fed into a resolution) vs CONSUMED (used by at least one resolution - no silent reload then); the freshness
    // axis (FRESH/STALE) is computable from the fingerprint alone. The bundle aggregates, the resource carries.
    private ResourceFingerprint fingerprint;
    private boolean consumed;

    private JsonResource() {
        this.importedProviderSynonyms = new ArrayList<>();
        this.providerName = SELF_SYNONYM;
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
     * Creates a JsonResource with file path, root node, and imported provider synonyms.
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
     * Returns the namespace label of the owning bundle (bundle-scoped IDs, concept strategy B):
     * Wood object IDs and provider synonyms are unique within their bundle only - resolution
     * happens relative to the asking bundle. Null means the session default namespace.
     *
     * @return the namespace label, or null for the session default.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace label of the owning bundle (bundle-scoped IDs, concept strategy B).
     * The label is assigned by the wiring layer that knows bundle membership; the engine itself
     * never guesses it.
     *
     * @param namespace the namespace label, or null for the session default.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
     * @param importedProviderSynonyms the list of synonyms to set, or {@code null} to clear.
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
     * @param importedProviderSynonym the synonym to add (ignored if null or blank).
     */
    public void addImportedProviderSynonym(String importedProviderSynonym) {
        if (importedProviderSynonym == null || importedProviderSynonym.isBlank()) {
            return;
        }
        this.importedProviderSynonyms.add(importedProviderSynonym);
    }

    /**
     * Returns an unmodifiable list of definition nodes.
     *
     * @return unmodifiable list of definition nodes, or empty list if none.
     */
    public List<JsonNode> getDefinitionNodes() {
        return definitionNodes != null ? Collections.unmodifiableList(definitionNodes) : Collections.emptyList();
    }

    /**
     * Sets the list of definition nodes.
     *
     * @param definitionNodes the list of definition nodes to set, or null to clear.
     */
    public void setDefinitionNodes(List<JsonNode> definitionNodes) {
        this.definitionNodes = definitionNodes != null ? new ArrayList<>(definitionNodes) : new ArrayList<>();
    }

    /**
     * Adds a definition node.
     *
     * @param node the definition node to add (ignored if null).
     */
    public void addDefinitionNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (this.definitionNodes == null) {
            this.definitionNodes = new ArrayList<>();
        }
        this.definitionNodes.add(node);
    }

    /**
     * Checks if this resource has any definition nodes.
     *
     * @return true if definition nodes exist, false otherwise.
     */
    public boolean hasDefinitionNodes() {
        return definitionNodes != null && !definitionNodes.isEmpty();
    }

    /**
     * Checks if this resource has any imported provider synonyms.
     *
     * @return {@code true} if there are imported synonyms, {@code false} otherwise.
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
     * @return {@code true} if has root but no resource file, {@code false} otherwise.
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

    /**
     * Captures the fingerprint of the origin file (concept doc/JSON_BUNDLE.md, state model): the load-time state
     * the freshness axis compares against. No-op for resources without a file (in-memory constructs, definitions).
     */
    public void captureFingerprint() {
        if (hasResourceFile()) {
            this.fingerprint = ResourceFingerprint.of(new File(resourceFile));
        }
    }

    /**
     * Returns the captured fingerprint of the origin file.
     *
     * @return the fingerprint, or null when never captured or the resource has no file.
     */
    public ResourceFingerprint getFingerprint() {
        return fingerprint;
    }

    /**
     * Overwrites the captured fingerprint - the refresh hook for a successful save or an explicit reload.
     *
     * @param fingerprint the new fingerprint, may be null.
     */
    public void setFingerprint(ResourceFingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    /**
     * Tells whether this resource was already fed into at least one resolution (consumption axis, concept
     * doc/JSON_BUNDLE.md): a consumed instance must not be silently reloaded - existing resolutions would
     * diverge from the state they were built on. REGISTERED is the opposite of CONSUMED.
     *
     * @return true when at least one resolution used this instance.
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Flags this resource as consumed - called by the first resolution that uses the instance.
     */
    public void markConsumed() {
        this.consumed = true;
    }

    /**
     * FRESHness is a detection, not a decision (concept doc/JSON_BUNDLE.md): the captured fingerprint against
     * the file as it is NOW. An in-memory resource (no captured fingerprint) is fresh by definition - there is no
     * file state to detach from; a vanished or unreadable origin file counts as stale (the cache detached).
     *
     * @return true when the origin file deviates from the captured load-time state.
     */
    public boolean isStale() {
        if (fingerprint == null) {
            return false;
        }
        if (!hasResourceFile()) {
            return false;
        }
        final ResourceFingerprint current = ResourceFingerprint.of(new File(resourceFile));
        return current == null || fingerprint.differsFrom(current);
    }

    /**
     * The opposite of {@link #isStale()}.
     *
     * @return true when the cached instance still maps its origin file.
     */
    public boolean isFresh() {
        return !isStale();
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
