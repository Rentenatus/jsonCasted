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

public final class JsonWoodProviderScanner {

    public final static JsonWoodProviderScanner INSTANCE = new JsonWoodProviderScanner();

    public JsonWoodProviderScanResult scan(JsonNode root) {
        Objects.requireNonNull(root, "root must not be null");

        JsonWoodProviderScanResult result = new JsonWoodProviderScanResult();
        scanNode(root, "$", result);
        return result;
    }

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

    private void scanObject(JsonNode node, String path, JsonWoodProviderScanResult result) {
        Map<String, JsonNode> children = node.asObjectValues();
        if (children == null || children.isEmpty()) {
            return;
        }

        children.forEach((key, childNode) -> {
            String childPath = path + "." + key;

            if (TERM_WOOD_PROVIDERS.equals(key)) {
                result.registerProviderNode(node, path);
            }

            scanNode(childNode, childPath, result);
        });
    }

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
