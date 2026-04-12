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

public class LinkingSet {

    private String providerName;
    private Map<String, LinkNodeEntry> objectIdMap;
    private Map<String, LinkNodeEntry> linkMap;
    private final Set<String> providerSynonyms;
    private final List<JsonExceptionEntry> scanExceptions;

    public LinkingSet(String providerName) {
        this.providerName = providerName;
        this.objectIdMap = new LinkedHashMap<>();
        this.linkMap = new LinkedHashMap<>();
        this.scanExceptions = new ArrayList<>();
        this.providerSynonyms = new LinkedHashSet<>();
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Map<String, LinkNodeEntry> getObjectIdMap() {
        return objectIdMap;
    }

    public void setObjectIdMap(Map<String, LinkNodeEntry> objectIdMap) {
        this.objectIdMap = objectIdMap != null ? new LinkedHashMap<>(objectIdMap) : new LinkedHashMap<>();
    }

    public Map<String, LinkNodeEntry> getLinkMap() {
        return linkMap;
    }

    public void setLinkMap(Map<String, LinkNodeEntry> linkMap) {
        this.linkMap = linkMap != null ? new LinkedHashMap<>(linkMap) : new LinkedHashMap<>();
    }

    public LinkNodeEntry findObjectById(String objectId) {
        Objects.requireNonNull(objectId, "objectId must not be null");
        return objectIdMap.get(providerName + "::" + objectId);
    }

    public LinkNodeEntry findLinkTarget(String link) {
        Objects.requireNonNull(link, "link must not be null");
        return linkMap.get(link);
    }

    public boolean hasUnconnectedLinks() {
        return !getUnconnectedSet().isEmpty();
    }

    public Set<String> getUnconnectedSet() {
        Set<String> result = new LinkedHashSet<>(linkMap.keySet());
        result.removeAll(objectIdMap.keySet());
        return result;
    }

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

    public void registerException(String path, JsonNode node, Exception exception) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(exception, "exception must not be null");

        scanExceptions.add(new JsonExceptionEntry(node, path, exception));
    }

    public Map<String, LinkNodeEntry> getUnmodifiableObjectIdMap() {
        return Collections.unmodifiableMap(objectIdMap);
    }

    public Map<String, LinkNodeEntry> getUnmodifiableLinkMap() {
        return Collections.unmodifiableMap(linkMap);
    }

    public Set<String> getUnmodifiableUnconnectedSet() {
        return Collections.unmodifiableSet(getUnconnectedSet());
    }

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
