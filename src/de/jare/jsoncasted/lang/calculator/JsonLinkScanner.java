package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Scannt einen JsonNode-Baum nach WoodJJ-Id-Metadaten.
 *
 */
public final class JsonLinkScanner {

    public static final String KEY_WOOD_OBJECT_ID = "_woodObjectId";
    public static final String KEY_WOOD_LINK = "_woodLink";

    public JsonLinkScanResult scan(JsonNode root) {
        Objects.requireNonNull(root, "root must not be null");

        JsonLinkScanResult result = new JsonLinkScanResult();
        scanNode(root, "$", result);
        return result;
    }

    private void scanNode(JsonNode node, String path, JsonLinkScanResult result) {
        if (node == null) {
            return;
        }

        if (node.isArray()) {
            scanArray(node, path, result);
        } else if (node.isObject()) {
            scanObject(node, path, result);
        }

    }

    private void scanObject(JsonNode node, String path, JsonLinkScanResult result) {
        Map<String, JsonNode> children = node.asObjectValues();
        if (children == null || children.isEmpty()) {
            return;
        }
        children.forEach((key, childNode) -> {
            String childPath = path + "." + key;
            scanObjectRegister(node, key, childNode, path, result);
            scanNode(childNode, childPath, result);
        });
    }

    private void scanArray(JsonNode node, String path, JsonLinkScanResult result) {
        List<JsonNode> array = node.asArray();
        if (array == null || array.isEmpty()) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            final JsonNode childNode = array.get(i);
            String childPath = path + "[" + i + "]";
            scanNode(childNode, childPath, result);
        }

    }

    private void scanObjectRegister(JsonNode node, String key, JsonNode childNode, String path, JsonLinkScanResult result) {
        if (KEY_WOOD_OBJECT_ID.equals(key)) {
            if (childNode.isNull()) {
                result.registerException(path, childNode, new NullPointerException(KEY_WOOD_OBJECT_ID + " must not be null."));
            } else {
                try {
                    result.registerObjectId(childNode.toText(), node, path);
                } catch (JsonParseException ex) {
                    result.registerException(path, childNode, ex);
                }
            }
        }

        if (KEY_WOOD_LINK.equals(key)) {
            if (childNode.isNull()) {
                result.registerException(path, childNode, new NullPointerException(KEY_WOOD_LINK + " must not be null."));
            } else {
                try {
                    result.registerLinkNode(node, childNode.toText(), path);
                } catch (JsonParseException ex) {
                    result.registerException(path, childNode, ex);
                }
            }
        }
    }

}
