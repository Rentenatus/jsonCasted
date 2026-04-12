/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WoodIdFinder {

    private static final Logger LOG = Logger.getLogger(WoodIdFinder.class.getName());
    private static final String PREFIX_THIS = "this::";
    private static final String PREFIX_SELF = "self::";

    private WoodIdFinder() {
        throw new IllegalStateException("Utility class");
    }

    public static LinkingSet buildLinkingSet(JsonNode root, String providerName, JsonDebugLevel debugLevel) {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(providerName, "providerName must not be null");

        LinkingSet result = new LinkingSet(providerName);
        traverse("$", root, providerName, result, debugLevel);
        return result;
    }

    private static void traverse(String path, JsonNode node,
            String providerName, LinkingSet result, JsonDebugLevel debugLevel) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            JsonNode idNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_OBJECT_ID);
            if (idNode != null) {
                try {
                    String key = providerName + "::" + idNode.toText();
                    final LinkNodeEntry linkNodeEntry = new LinkNodeEntry(node, key, path);
                    result.getObjectIdMap().put(key, linkNodeEntry);
                } catch (JsonParseException ex) {
                    result.registerException(path, node, ex);
                    LOG.log(Level.WARNING, "Could not read _woodObjectId as text.", ex);
                }
            }

            JsonNode linkNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_LINK);
            if (linkNode != null) {
                try {
                    String rawKey = linkNode.toText();
                    String normalizedKey = normalizeLinkKey(rawKey, providerName);
                    final LinkNodeEntry linkNodeEntry = new LinkNodeEntry(node, normalizedKey, path);
                    result.getLinkMap().put(normalizedKey, linkNodeEntry);
                } catch (JsonParseException ex) {
                    result.registerException(path, node, ex);
                    LOG.log(Level.WARNING, "Could not read _woodLink as text.", ex);
                }
            }

            Map<String, JsonNode> children = node.asObjectValues();
            if (children == null || children.isEmpty()) {
                return;
            }
            children.forEach((key, childNode) -> {
                String childPath = path + "." + key;
                traverse(childPath, childNode, providerName, result, debugLevel);
            });

            return;
        }

        if (node.isArray()) {
            List<JsonNode> array = node.asArray();
            for (int i = 0; i < array.size(); i++) {
                final JsonNode childNode = array.get(i);
                String childPath = path + "[" + i + "]";
                traverse(childPath, childNode, providerName, result, debugLevel);
            }
        }
    }

    private static String normalizeLinkKey(String linkKey, String providerName) {
        if (linkKey == null) {
            return null;
        }
        if (linkKey.startsWith(PREFIX_THIS)) {
            return providerName + "::" + linkKey.substring(PREFIX_THIS.length());
        }
        if (linkKey.startsWith(PREFIX_SELF)) {
            return providerName + "::" + linkKey.substring(PREFIX_SELF.length());
        }
        return linkKey;
    }
}
