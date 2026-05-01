/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_PROVIDERS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The JsonWoodProviderScanResult class contains the results of scanning a JSON node
 * tree for wood provider definitions. It stores all found provider nodes along with
 * their paths in the JSON structure.
 *
 * @author Janusch Rentenatus
 */
public final class JsonWoodProviderScanResult {

    private final List<ProviderNodeEntry> providerNodes = new ArrayList<>();

    /**
     * Registers a provider node found during scanning.
     *
     * @param childNode The JSON node containing the provider definition.
     * @param path The path to the parent node in the JSON structure.
     */
    void registerProviderNode(JsonNode childNode, String path) {
        JsonNode ownerNode = JsonNode.objectNode();
        ownerNode.asObjectValues().put(TERM_WOOD_PROVIDERS, childNode);
        providerNodes.add(new ProviderNodeEntry(ownerNode, path));
    }

    /**
     * Returns an unmodifiable list of all provider node entries found during scanning.
     *
     * @return An unmodifiable list of provider node entries.
     */
    public List<ProviderNodeEntry> getProviderNodes() {
        return Collections.unmodifiableList(providerNodes);
    }

    /**
     * Checks if any provider nodes were found during scanning.
     *
     * @return true if at least one provider node was found, false otherwise.
     */
    public boolean hasProviderNodes() {
        return !providerNodes.isEmpty();
    }

    /**
     * Checks if no provider nodes were found during scanning.
     *
     * @return true if no provider nodes were found, false otherwise.
     */
    public boolean isEmpty() {
        return providerNodes.isEmpty();
    }

    /**
     * The ProviderNodeEntry class represents a single wood provider node found during scanning.
     * It stores the owner node (containing the provider) and the path to that node.
     */
    public static final class ProviderNodeEntry {

        private final JsonNode ownerNode;
        private final String path;

        /**
         * Constructs a ProviderNodeEntry instance.
         *
         * @param ownerNode The JSON node that owns/contains the provider.
         * @param path The path to the owner node in the JSON structure.
         * @throws NullPointerException If ownerNode or path is null.
         */
        public ProviderNodeEntry(JsonNode ownerNode, String path) {
            this.ownerNode = Objects.requireNonNull(ownerNode, "ownerNode must not be null");
            this.path = Objects.requireNonNull(path, "path must not be null");
        }

        /**
         * Returns the owner JSON node.
         *
         * @return The owner JSON node.
         */
        public JsonNode getOwnerNode() {
            return ownerNode;
        }

        /**
         * Returns the path to the owner node.
         *
         * @return The path string.
         */
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
