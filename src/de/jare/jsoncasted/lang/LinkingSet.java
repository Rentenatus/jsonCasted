package de.jare.jsoncasted.lang;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LinkingSet {

    private String providerName;
    private Map<String, JsonNode> objectIdMap;
    private Map<String, JsonNode> linkMap;

    public LinkingSet(String providerName,
                      Map<String, JsonNode> objectIdMap,
                      Map<String, JsonNode> linkMap) {
        this.providerName = providerName;
        this.objectIdMap = objectIdMap != null ? new LinkedHashMap<>(objectIdMap) : new LinkedHashMap<>();
        this.linkMap = linkMap != null ? new LinkedHashMap<>(linkMap) : new LinkedHashMap<>();
    }

    public LinkingSet(String providerName) {
        this(providerName, null, null);
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public Map<String, JsonNode> getObjectIdMap() {
        return objectIdMap;
    }

    public void setObjectIdMap(Map<String, JsonNode> objectIdMap) {
        this.objectIdMap = objectIdMap != null ? new LinkedHashMap<>(objectIdMap) : new LinkedHashMap<>();
    }

    public Map<String, JsonNode> getLinkMap() {
        return linkMap;
    }

    public void setLinkMap(Map<String, JsonNode> linkMap) {
        this.linkMap = linkMap != null ? new LinkedHashMap<>(linkMap) : new LinkedHashMap<>();
    }

    public JsonNode findObjectById(String objectId) {
        Objects.requireNonNull(objectId, "objectId must not be null");
        return objectIdMap.get(providerName + "::" + objectId);
    }

    public JsonNode findLinkTarget(String link) {
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

    public Map<String, JsonNode> getUnconnectedLinkMap() {
        Map<String, JsonNode> result = new LinkedHashMap<>();
        for (String key : getUnconnectedSet()) {
            JsonNode node = linkMap.get(key);
            if (node != null) {
                result.put(key, node);
            }
        }
        return result;
    }

    public Map<String, JsonNode> getUnmodifiableObjectIdMap() {
        return Collections.unmodifiableMap(objectIdMap);
    }

    public Map<String, JsonNode> getUnmodifiableLinkMap() {
        return Collections.unmodifiableMap(linkMap);
    }

    public Set<String> getUnmodifiableUnconnectedSet() {
        return Collections.unmodifiableSet(getUnconnectedSet());
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