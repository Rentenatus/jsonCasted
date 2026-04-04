package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.lang.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Ergebnis eines JsonLinkScanner-Durchlaufs.
 *
 * Enthält:
 * - eindeutige ObjectIds
 * - gefundene Link-Nodes
 * - verwendete Provider-Synonyme aus _woodLink
 * - doppelte ObjectIds
 * - Scan-Exceptions (z.B. Parsing-Fehler bei toText())
 *
 * Keine echte Auflösung auf Zielobjekte.
 */
public final class JsonLinkScanResult {

    private final Map<String, JsonNode> objectIdToNode = new LinkedHashMap<>();
    private final Map<String, String> objectIdToPath = new LinkedHashMap<>();
    private final List<LinkNodeEntry> linkNodes = new ArrayList<>();
    private final Set<String> providerSynonyms = new LinkedHashSet<>();
    private final List<DuplicateObjectIdEntry> duplicateObjectIds = new ArrayList<>();
    private final List<ScanExceptionEntry> scanExceptions = new ArrayList<>();

    void registerObjectId(String objectId, JsonNode node, String path) {
        Objects.requireNonNull(objectId, "objectId must not be null");
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(path, "path must not be null");

        JsonNode previousNode = objectIdToNode.putIfAbsent(objectId, node);
        if (previousNode == null) {
            objectIdToPath.put(objectId, path);
            return;
        }

        duplicateObjectIds.add(new DuplicateObjectIdEntry(
                objectId,
                previousNode,
                objectIdToPath.get(objectId),
                node,
                path
        ));
    }

    void registerLinkNode(JsonNode node, String woodLink, String path) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(woodLink, "woodLink must not be null");
        Objects.requireNonNull(path, "path must not be null");

        linkNodes.add(new LinkNodeEntry(node, woodLink, path));

        String providerSynonym = extractProviderSynonym(woodLink);
        if (providerSynonym != null && !providerSynonym.isBlank()) {
            providerSynonyms.add(providerSynonym);
        }
    }

    void registerException(String path, JsonNode node, Exception exception) {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(exception, "exception must not be null");

        scanExceptions.add(new ScanExceptionEntry(path, node, exception));
    }

    public Map<String, JsonNode> getObjectIdToNode() {
        return Collections.unmodifiableMap(objectIdToNode);
    }

    public Map<String, String> getObjectIdToPath() {
        return Collections.unmodifiableMap(objectIdToPath);
    }

    public List<LinkNodeEntry> getLinkNodes() {
        return Collections.unmodifiableList(linkNodes);
    }

    public Set<String> getProviderSynonyms() {
        return Collections.unmodifiableSet(providerSynonyms);
    }

    public List<DuplicateObjectIdEntry> getDuplicateObjectIds() {
        return Collections.unmodifiableList(duplicateObjectIds);
    }

    public List<ScanExceptionEntry> getScanExceptions() {
        return Collections.unmodifiableList(scanExceptions);
    }

    public JsonNode findNodeByObjectId(String objectId) {
        return objectIdToNode.get(objectId);
    }

    public String findPathByObjectId(String objectId) {
        return objectIdToPath.get(objectId);
    }

    public boolean containsObjectId(String objectId) {
        return objectIdToNode.containsKey(objectId);
    }

    public boolean containsProviderSynonym(String providerSynonym) {
        return providerSynonyms.contains(providerSynonym);
    }

    public boolean hasDuplicateObjectIds() {
        return !duplicateObjectIds.isEmpty();
    }

    public boolean hasLinks() {
        return !linkNodes.isEmpty();
    }

    public boolean hasProviderSynonyms() {
        return !providerSynonyms.isEmpty();
    }

    public boolean hasExceptions() {
        return !scanExceptions.isEmpty();
    }

    public boolean isEmpty() {
        return objectIdToNode.isEmpty()
                && linkNodes.isEmpty()
                && providerSynonyms.isEmpty()
                && duplicateObjectIds.isEmpty()
                && scanExceptions.isEmpty();
    }

    public Set<String> getObjectIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(objectIdToNode.keySet()));
    }

    private String extractProviderSynonym(String woodLink) {
        int separatorIndex = woodLink.indexOf("::");
        if (separatorIndex <= 0) {
            return null;
        }

        String provider = woodLink.substring(0, separatorIndex).trim();
        return provider.isEmpty() ? null : provider;
    }

    public static final class LinkNodeEntry {

        private final JsonNode node;
        private final String woodLink;
        private final String path;

        public LinkNodeEntry(JsonNode node, String woodLink, String path) {
            this.node = Objects.requireNonNull(node, "node must not be null");
            this.woodLink = Objects.requireNonNull(woodLink, "woodLink must not be null");
            this.path = Objects.requireNonNull(path, "path must not be null");
        }

        public JsonNode getNode() {
            return node;
        }

        public String getWoodLink() {
            return woodLink;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return "LinkNodeEntry{"
                    + "woodLink='" + woodLink + '\''
                    + ", path='" + path + '\''
                    + '}';
        }
    }

    public static final class DuplicateObjectIdEntry {

        private final String objectId;
        private final JsonNode firstNode;
        private final String firstPath;
        private final JsonNode duplicateNode;
        private final String duplicatePath;

        public DuplicateObjectIdEntry(
                String objectId,
                JsonNode firstNode,
                String firstPath,
                JsonNode duplicateNode,
                String duplicatePath
        ) {
            this.objectId = Objects.requireNonNull(objectId, "objectId must not be null");
            this.firstNode = Objects.requireNonNull(firstNode, "firstNode must not be null");
            this.firstPath = Objects.requireNonNull(firstPath, "firstPath must not be null");
            this.duplicateNode = Objects.requireNonNull(duplicateNode, "duplicateNode must not be null");
            this.duplicatePath = Objects.requireNonNull(duplicatePath, "duplicatePath must not be null");
        }

        public String getObjectId() {
            return objectId;
        }

        public JsonNode getFirstNode() {
            return firstNode;
        }

        public String getFirstPath() {
            return firstPath;
        }

        public JsonNode getDuplicateNode() {
            return duplicateNode;
        }

        public String getDuplicatePath() {
            return duplicatePath;
        }

        @Override
        public String toString() {
            return "DuplicateObjectIdEntry{"
                    + "objectId='" + objectId + '\''
                    + ", firstPath='" + firstPath + '\''
                    + ", duplicatePath='" + duplicatePath + '\''
                    + '}';
        }
    }

    public static final class ScanExceptionEntry {

        private final String path;
        private final JsonNode node;
        private final Exception exception;

        public ScanExceptionEntry(String path, JsonNode node, Exception exception) {
            this.path = Objects.requireNonNull(path, "path must not be null");
            this.node = node;
            this.exception = Objects.requireNonNull(exception, "exception must not be null");
        }

        public String getPath() {
            return path;
        }

        public JsonNode getNode() {
            return node;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            return "ScanExceptionEntry{"
                    + "path='" + path + '\''
                    + ", exception=" + exception
                    + '}';
        }
    }
}