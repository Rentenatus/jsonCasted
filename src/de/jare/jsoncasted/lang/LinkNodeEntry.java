/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.util.Objects;

/**
 *
 * @author Janusch Rentenatus
 */
public final class LinkNodeEntry {

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
