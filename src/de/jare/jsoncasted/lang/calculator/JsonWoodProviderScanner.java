/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.lang.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_PROVIDERS;

/**
 * The JsonWoodProviderScanner class scans JSON node trees for wood provider definitions.
 * It traverses the entire JSON structure and collects all nodes that contain wood provider
 * information, returning them in a scan result object.
 *
 * @author Janusch Rentenatus
 */
public final class JsonWoodProviderScanner {

    /**
     * Singleton instance of JsonWoodProviderScanner.
     */
    public final static JsonWoodProviderScanner INSTANCE = new JsonWoodProviderScanner();

    /**
     * Scans a JSON node tree for wood provider definitions.
     *
     * @param root The root JSON node to start scanning from.
     * @return A scan result containing all found provider nodes.
     * @throws NullPointerException If root is null.
     */
    public JsonWoodProviderScanResult scan(JsonNode root) {
        Objects.requireNonNull(root, "root must not be null");

        JsonWoodProviderScanResult result = new JsonWoodProviderScanResult();
        scanNode(root, "$", result);
        return result;
    }

    /**
     * Recursively scans a JSON node.
     *
     * @param node The JSON node to scan.
     * @param path The path to the node (used for identification).
     * @param result The scan result to add findings to.
     */
    private void scanNode(JsonNode node, String path, JsonWoodProviderScanResult result) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            scanObject(node, path, result);
        } else if (node.isArray()) {
            scanArray(node, path, result);
        }
    }

    /**
     * Scans a JSON object node for provider definitions.
     *
     * @param node The JSON object node to scan.
     * @param path The path to the node.
     * @param result The scan result to add findings to.
     */
    private void scanObject(JsonNode node, String path, JsonWoodProviderScanResult result) {
        Map<String, JsonNode> children = node.asObjectValues();
        if (children == null || children.isEmpty()) {
            return;
        }

        children.forEach((key, childNode) -> {
            String childPath = path + "." + key;

            if (TERM_WOOD_PROVIDERS.equals(key)) {
                result.registerProviderNode(childNode, path);
            }

            scanNode(childNode, childPath, result);
        });
    }

    /**
     * Scans a JSON array node for provider definitions.
     *
     * @param node The JSON array node to scan.
     * @param path The path to the node.
     * @param result The scan result to add findings to.
     */
    private void scanArray(JsonNode node, String path, JsonWoodProviderScanResult result) {
        List<JsonNode> array = node.asArray();
        if (array == null || array.isEmpty()) {
            return;
        }

        for (int i = 0; i < array.size(); i++) {
            JsonNode childNode = array.get(i);
            String childPath = path + "[" + i + "]";
            scanNode(childNode, childPath, result);
        }
    }

}
