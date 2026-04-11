/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WoodIdFinder {

    private static final Logger LOG = Logger.getLogger(WoodIdFinder.class.getName());

    public static Map<String, JsonNode> findWoodObjectIdParents(JsonNode root, String provider) {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(provider, "provider must not be null");

        Map<String, JsonNode> result = new LinkedHashMap<>();
        traverseIdSearch(root, provider, result);
        return result;
    }

    public static Map<String, JsonNode> findWoodLinkParents(JsonNode root) {
        Objects.requireNonNull(root, "root must not be null");

        Map<String, JsonNode> result = new LinkedHashMap<>();
        traverseLinkSearch(root, result);
        return result;
    }

    private static void traverseIdSearch(
            JsonNode node,
            String provider,
            Map<String, JsonNode> result) {

        if (node == null) {
            return;
        }

        if (node.isObject()) {
            JsonNode idNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_OBJECT_ID);

            if (idNode != null) {
                try {
                    String key = provider + "::" + idNode.toText();
                    result.put(key, node);
                } catch (JsonParseException ex) {
                    LOG.log(Level.WARNING, "Could not read _woodObjectId as text.", ex);
                }
            }
        }

        if (node.isObject()) {
            for (JsonNode child : node.asObjectValues().values()) {
                traverseIdSearch(child, provider, result);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                traverseIdSearch(child, provider, result);
            }
        }
    }

    private static void traverseLinkSearch(
            JsonNode node,
            Map<String, JsonNode> result) {

        if (node == null) {
            return;
        }

        if (node.isObject()) {
            JsonNode linkNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_LINK);

            if (linkNode != null) {
                try {
                    String key = linkNode.toText();
                    result.put(key, node);
                } catch (JsonParseException ex) {
                    LOG.log(Level.WARNING, "Could not read _woodLink as text.", ex);
                }
            }
        }

        if (node.isObject()) {
            for (JsonNode child : node.asObjectValues().values()) {
                traverseLinkSearch(child, result);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                traverseLinkSearch(child, result);
            }
        }
    }
}
