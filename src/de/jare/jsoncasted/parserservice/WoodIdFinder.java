/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for building linking sets from JSON node trees.
 *
 * <p>This class traverses a JSON node tree and builds a {@link LinkingSet} that tracks:
 * <ul>
 *   <li>All nodes with {@code _woodObjectId} (object identity)</li>
 *   <li>All nodes with {@code _woodLink} (cross-resource references)</li>
 * </ul>
 *
 * <p>The linking set can then be used to resolve cross-resource references
 * in the Wood Json Jack system.</p>
 */
public final class WoodIdFinder {

    private static final Logger LOG = Logger.getLogger(WoodIdFinder.class.getName());

    private WoodIdFinder() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Builds a linking set from the specified root node.
     *
     * <p>Traverses the entire node tree recursively and collects all object IDs
     * and link references into a LinkingSet.</p>
     *
     * @param root the root JsonNode to traverse (must not be null).
     * @param providerName the provider name for this resource (must not be null).
     * @param debugLevel the debug level for logging.
     * @return a LinkingSet containing all object IDs and links found.
     * @throws NullPointerException if root or providerName is null.
     */
    public static LinkingSet buildLinkingSet(JsonNode root, String providerName, JsonDebugLevel debugLevel) {
        Objects.requireNonNull(root, "root must not be null");
        Objects.requireNonNull(providerName, "providerName must not be null");

        LinkingSet result = new LinkingSet(providerName);
        traverse("$", root, providerName, result, debugLevel);
        return result;
    }

    /**
     * Recursively traverses a JSON node tree and populates the linking set.
     *
     * @param path the current path in the tree (for error reporting).
     * @param node the current node to process.
     * @param providerName the provider name for object ID normalization.
     * @param result the LinkingSet to populate.
     * @param debugLevel the debug level for logging.
     */
    private static void traverse(String path, JsonNode node,
            String providerName, LinkingSet result, JsonDebugLevel debugLevel) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            try {
                String key = node.getObjectId(providerName);
                if (key != null) {
                    final LinkNodeEntry linkNodeEntry = new LinkNodeEntry(node, key, path);
                    result.getObjectIdMap().put(key, linkNodeEntry);
                }
            } catch (JsonParseException ex) {
                result.registerException(path, node, ex);
                LOG.log(Level.WARNING, "Could not read _woodObjectId as text.", ex);
            }

            try {
                String normalizedKey = node.getLink(providerName);
                if (normalizedKey != null) {
                    final LinkNodeEntry linkNodeEntry = new LinkNodeEntry(node, normalizedKey, path);
                    result.getLinkMap().put(normalizedKey, linkNodeEntry);
                }
            } catch (JsonParseException ex) {
                result.registerException(path, node, ex);
                LOG.log(Level.WARNING, "Could not read _woodLink as text.", ex);
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

}
