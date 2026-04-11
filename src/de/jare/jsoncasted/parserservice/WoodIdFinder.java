/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.parserwriter.JsonParseException;
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

    public static LinkingSet buildLinkingSet(JsonNode root, String providerName) {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(providerName, "providerName must not be null");

        LinkingSet result = new LinkingSet(providerName);
        traverse(root, providerName, result);
        return result;
    }

    private static void traverse(JsonNode node, String providerName, LinkingSet result) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            JsonNode idNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_OBJECT_ID);
            if (idNode != null) {
                try {
                    String key = providerName + "::" + idNode.toText();
                    result.getObjectIdMap().put(key, node);
                } catch (JsonParseException ex) {
                    LOG.log(Level.WARNING, "Could not read _woodObjectId as text.", ex);
                }
            }

            JsonNode linkNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_LINK);
            if (linkNode != null) {
                try {
                    String rawKey = linkNode.toText();
                    String normalizedKey = normalizeLinkKey(rawKey, providerName);
                    result.getLinkMap().put(normalizedKey, node);
                } catch (JsonParseException ex) {
                    LOG.log(Level.WARNING, "Could not read _woodLink as text.", ex);
                }
            }

            for (JsonNode child : node.asObjectValues().values()) {
                traverse(child, providerName, result);
            }
            return;
        }

        if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                traverse(child, providerName, result);
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