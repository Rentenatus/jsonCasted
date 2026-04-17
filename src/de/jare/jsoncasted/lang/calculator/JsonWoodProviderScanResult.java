/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.lang.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class JsonWoodProviderScanResult {

    private final List<ProviderNodeEntry> providerNodes = new ArrayList<>();

    void registerProviderNode(JsonNode ownerNode, String path) {
        providerNodes.add(new ProviderNodeEntry(ownerNode, path));
    }

    public List<ProviderNodeEntry> getProviderNodes() {
        return Collections.unmodifiableList(providerNodes);
    }

    public boolean hasProviderNodes() {
        return !providerNodes.isEmpty();
    }

    public boolean isEmpty() {
        return providerNodes.isEmpty();
    }

    public static final class ProviderNodeEntry {

        private final JsonNode ownerNode;
        private final String path;

        public ProviderNodeEntry(JsonNode ownerNode, String path) {
            this.ownerNode = Objects.requireNonNull(ownerNode, "ownerNode must not be null");
            this.path = Objects.requireNonNull(path, "path must not be null");
        }

        public JsonNode getOwnerNode() {
            return ownerNode;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return "ProviderNodeEntry{"
                    + "path='" + path + '\''
                    + '}';
        }
    }
}
