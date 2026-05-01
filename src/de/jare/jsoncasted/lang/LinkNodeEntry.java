/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.util.Objects;

/**
 * Represents a link node entry that associates a JsonNode with its wood link and path.
 *
 * <p>This class is used by {@link LinkingSet} to track nodes that have
 * cross-resource references ({@code _woodLink}) and their locations in the JSON tree.</p>
 *
 * @author Janusch Rentenatus
 */
public final class LinkNodeEntry {

    private final JsonNode node;
    private final String woodLink;
    private final String path;

    /**
     * Constructs a LinkNodeEntry with the node, wood link, and path.
     *
     * @param node the JsonNode containing the link (must not be null).
     * @param woodLink the wood link value (e.g., "save::123456") (must not be null).
     * @param path the path to the node in the JSON tree (must not be null).
     * @throws NullPointerException if node, woodLink, or path is null.
     */
    public LinkNodeEntry(JsonNode node, String woodLink, String path) {
        this.node = Objects.requireNonNull(node, "node must not be null");
        this.woodLink = Objects.requireNonNull(woodLink, "woodLink must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    /**
     * Returns the JsonNode containing the link.
     *
     * @return the JsonNode.
     */
    public JsonNode getNode() {
        return node;
    }

    /**
     * Returns the wood link value.
     *
     * @return the wood link string (e.g., "save::123456").
     */
    public String getWoodLink() {
        return woodLink;
    }

    /**
     * Returns the path to the node in the JSON tree.
     *
     * @return the path string.
     */
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
