package de.jare.jsoncasted.lang;

import de.jare.jsoncasted.lang.calculator.JsonLinkScanResult;
import de.jare.jsoncasted.wood.WoodProviderBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class JsonResource {

    private String resourceFile;
    private String providerName;
    private JsonNode root;
    private WoodProviderBox expectedBox;
    private List<String> importedProviderSynonyms;
    private JsonLinkScanResult scanResult;
    private final List<JsonExceptionEntry> exceptions = new ArrayList<>();

    private Map<String, JsonNode> objectIdMap;
    private Map<String, JsonNode> linkMap;

    private JsonResource() {
        this.importedProviderSynonyms = new ArrayList<>();
        this.providerName = "self";
    }

    public static JsonResource empty() {
        return new JsonResource();
    }

    public static JsonResource forFile(String resourceFile) {
        JsonResource resource = new JsonResource();
        resource.setResourceFile(resourceFile);
        return resource;
    }

    public static JsonResource forRoot(JsonNode root) {
        JsonResource resource = new JsonResource();
        resource.setRoot(root);
        return resource;
    }

    public static JsonResource loaded(String resourceFile, JsonNode root) {
        JsonResource resource = new JsonResource();
        resource.setResourceFile(resourceFile);
        resource.setRoot(root);
        return resource;
    }

    public static JsonResource loaded(String resourceFile, JsonNode root, List<String> importedProviderSynonyms) {
        JsonResource resource = new JsonResource();
        resource.setResourceFile(resourceFile);
        resource.setRoot(root);
        resource.setImportedProviderSynonyms(importedProviderSynonyms);
        return resource;
    }

    public String getResourceFile() {
        return resourceFile;
    }

    public void setResourceFile(String resourceFile) {
        this.resourceFile = resourceFile;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public JsonNode getRoot() {
        return root;
    }

    public void setRoot(JsonNode root) {
        this.root = root;
    }

    public WoodProviderBox getExpectedBox() {
        return expectedBox;
    }

    public void setExpectedBox(WoodProviderBox expectedBox) {
        this.expectedBox = expectedBox;
    }

    public List<String> getImportedProviderSynonyms() {
        return Collections.unmodifiableList(importedProviderSynonyms);
    }

    public void setImportedProviderSynonyms(List<String> importedProviderSynonyms) {
        if (importedProviderSynonyms == null) {
            this.importedProviderSynonyms = new ArrayList<>();
        } else {
            this.importedProviderSynonyms = new ArrayList<>(importedProviderSynonyms);
        }
    }

    public void addImportedProviderSynonym(String importedProviderSynonym) {
        if (importedProviderSynonym == null || importedProviderSynonym.isBlank()) {
            return;
        }
        this.importedProviderSynonyms.add(importedProviderSynonym);
    }

    public boolean hasImportedProviderSynonyms() {
        return importedProviderSynonyms != null && !importedProviderSynonyms.isEmpty();
    }

    public JsonLinkScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(JsonLinkScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public boolean hasResourceFile() {
        return resourceFile != null && !resourceFile.isBlank();
    }

    public boolean hasRoot() {
        return root != null;
    }

    public boolean hasScanResult() {
        return scanResult != null;
    }

    public boolean isLoaded() {
        return root != null;
    }

    public boolean isUnsaved() {
        return root != null && (resourceFile == null || resourceFile.isBlank());
    }

    public boolean isFileReferenceOnly() {
        return root == null && resourceFile != null && !resourceFile.isBlank();
    }

    public Map<String, JsonNode> getObjectIdMap() {
        return objectIdMap;
    }

    public void setObjectIdMap(Map<String, JsonNode> objectIdMap) {
        this.objectIdMap = objectIdMap;
    }

    public Map<String, JsonNode> getLinkMap() {
        return linkMap;
    }

    public void setLinkMap(Map<String, JsonNode> linkMap) {
        this.linkMap = linkMap;
    }

    @Override
    public String toString() {
        return "JsonResource{"
                + "resourceFile='" + resourceFile + '\''
                + ", hasRoot=" + (root != null)
                + ", importedProviderSynonyms=" + importedProviderSynonyms
                + ", hasScanResult=" + (scanResult != null)
                + '}';
    }

    public void addExceptions(List<JsonExceptionEntry> addExceptions) {
        exceptions.addAll(addExceptions);
    }

}
