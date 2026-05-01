/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Manages object identities and links within a JSON resource for cross-referencing.
 *
 * <p>A LinkingSet tracks:</p>
 * <ul>
 *   <li>Object IDs and their corresponding nodes ({@link #getObjectIdMap()})</li>
 *   <li>Link references and their target nodes ({@link #getLinkMap()})</li>
 *   <li>Unconnected links that cannot be resolved</li>
 *   <li>Scan exceptions that occurred during linking</li>
 * </ul>
 *
 * <p>This is used to resolve cross-resource references in the Wood Json Jack system.</p>
 */
public class LinkingSet {

    private String providerName;
    private Map<String, LinkNodeEntry> objectIdMap;
    private Map<String, LinkNodeEntry> linkMap;
    private final Set<String> providerSynonyms;
    private final List<JsonExceptionEntry> scanExceptions;

    /**
     * Constructs a LinkingSet for the specified provider.
     *
     * @param providerName the name of the provider this linking set belongs to.
     */
    public LinkingSet(String providerName) {
        this.providerName = providerName;
        this.objectIdMap = new LinkedHashMap<>();
        this.linkMap = new LinkedHashMap<>();
        this.scanExceptions = new ArrayList<>();
        this.providerSynonyms = new LinkedHashSet<>();
    }

    /**
     * Returns the provider name for this linking set.
     *
     * @return the provider name.
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the provider name for this linking set.
     *
     * @param providerName the provider name to set.
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * Returns the map of object IDs to their corresponding node entries.
     *
     * @return the object ID map (may be modified).
     */
    public Map<String, LinkNodeEntry> getObjectIdMap() {
        return objectIdMap;
    }

    /**
     * Sets the object ID map.
     *
     * @param objectIdMap the map to set, or {@code null} to create an empty map.
     */
    public void setObjectIdMap(Map<String, LinkNodeEntry> objectIdMap) {
        this.objectIdMap = objectIdMap != null ? new LinkedHashMap<>(objectIdMap) : new LinkedHashMap<>();
    }

    /**
     * Returns the map of link references to their corresponding node entries.
     *
     * @return the link map (may be modified).
     */
    public Map<String, LinkNodeEntry> getLinkMap() {
        return linkMap;
    }

    /**
     * Sets the link map.
     *
     * @param linkMap the map to set, or {@code null} to create an empty map.
     */
    public void setLinkMap(Map<String, LinkNodeEntry> linkMap) {
        this.linkMap = linkMap != null ? new LinkedHashMap<>(linkMap) : new LinkedHashMap<>();
    }

    /**
     * Finds a node entry by its object ID.
     *
     * @param objectId the object ID to search for.
     * @return the corresponding LinkNodeEntry, or {@code null} if not found.
     * @throws NullPointerException if objectId is null.
     */
    public LinkNodeEntry findObjectById(String objectId) {
        Objects.requireNonNull(objectId, "objectId must not be null");
        return objectIdMap.get(providerName + "::" + objectId);
    }

    /**
     * Finds a node entry by its link reference.
     *
     * @param link the link reference to search for.
     * @return the corresponding LinkNodeEntry, or {@code null} if not found.
     * @throws NullPointerException if link is null.
     */
    public LinkNodeEntry findLinkTarget(String link) {
        Objects.requireNonNull(link, "link must not be null");
        return linkMap.get(link);
    }

    /**
     * Checks if there are any unconnected links in this set.
     *
     * @return {@code true} if there are unconnected links, {@code false} otherwise.
     */
    public boolean hasUnconnectedLinks() {
        return !getUnconnectedSet().isEmpty();
    }

    /**
     * Returns the set of unconnected link keys.
     *
     * @return set of link keys that do not have corresponding objects.
     */
    public Set<String> getUnconnectedSet() {
        Set<String> result = new LinkedHashSet<>(linkMap.keySet());
        result.removeAll(objectIdMap.keySet());
        return result;
    }

    /**
     * Returns a map of unconnected links to their entries.
     *
     * @return map of unconnected link keys to LinkNodeEntry objects.
     */
    public Map<String, LinkNodeEntry> getUnconnectedLinkMap() {
        Map<String, LinkNodeEntry> result = new LinkedHashMap<>();
        for (String key : getUnconnectedSet()) {
            LinkNodeEntry entry = linkMap.get(key);
            if (entry != null) {
                result.put(key, entry);
            }
        }
        return result;
    }

    /**
     * Registers an exception that occurred during scanning/parsing.
     *
     * @param path the path where the exception occurred.
     * @param node the JsonNode where the exception occurred.
     * @param exception the exception that occurred.
     * @throws NullPointerException if path, node, or exception is null.
     */
    public void registerException(String path, JsonNode node, Exception exception) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(exception, "exception must not be null");

        scanExceptions.add(new JsonExceptionEntry(node, path, exception));
    }

    /**
     * Returns an unmodifiable view of the object ID map.
     *
     * @return unmodifiable map of object IDs to LinkNodeEntry objects.
     */
    public Map<String, LinkNodeEntry> getUnmodifiableObjectIdMap() {
        return Collections.unmodifiableMap(objectIdMap);
    }

    /**
     * Returns an unmodifiable view of the link map.
     *
     * @return unmodifiable map of link references to LinkNodeEntry objects.
     */
    public Map<String, LinkNodeEntry> getUnmodifiableLinkMap() {
        return Collections.unmodifiableMap(linkMap);
    }

    /**
     * Returns an unmodifiable view of the unconnected link set.
     *
     * @return unmodifiable set of unconnected link keys.
     */
    public Set<String> getUnmodifiableUnconnectedSet() {
        return Collections.unmodifiableSet(getUnconnectedSet());
    }

    /**
     * Returns an unmodifiable list of scan exceptions.
     *
     * @return unmodifiable list of JsonExceptionEntry objects.
     */
    public List<JsonExceptionEntry> getScanExceptions() {
        return Collections.unmodifiableList(scanExceptions);
    }

    @Override
    public String toString() {
        return "LinkingSet{" 
                + "providerName='" + providerName + '\''
                + ", objectIdMapSize=" + (objectIdMap != null ? objectIdMap.size() : 0)
                + ", linkMapSize=" + (linkMap != null ? linkMap.size() : 0)
                + ", unconnectedSize=" + getUnconnectedSet().size()
                + '}';
    }
}
