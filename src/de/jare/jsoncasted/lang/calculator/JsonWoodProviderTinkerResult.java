/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.lang.JsonExceptionEntry;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.wood.WoodProviderBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The JsonWoodProviderTinkerResult class contains the results of building WoodProviderBox
 * instances from a scan result. It stores all successfully built provider boxes and
 * any exceptions that occurred during the build process.
 *
 * @author Janusch Rentenatus
 */
public final class JsonWoodProviderTinkerResult {

    private final List<BuildEntry> entries = new ArrayList<>();
    private final List<JsonExceptionEntry> exceptions = new ArrayList<>();
    private WoodProviderBox woodProviderBox = null;

    /**
     * Registers a successfully built WoodProviderBox.
     *
     * @param scanEntry The scan entry that produced this box.
     * @param box The built WoodProviderBox.
     * @throws NullPointerException If scanEntry or box is null.
     */
    void registerBox(JsonWoodProviderScanResult.ProviderNodeEntry scanEntry, WoodProviderBox box) {
        entries.add(new BuildEntry(scanEntry, box));
        if (woodProviderBox == null) {
            woodProviderBox = box;
        } else {
            woodProviderBox.mergeBox(box);
        }
    }

    /**
     * Registers an exception that occurred during building.
     *
     * @param scanEntry The scan entry where the exception occurred.
     * @param exception The exception that occurred.
     * @throws NullPointerException If scanEntry or exception is null.
     */
    void registerException(JsonWoodProviderScanResult.ProviderNodeEntry scanEntry, Exception exception) {
        exceptions.add(new JsonExceptionEntry(scanEntry.getOwnerNode(), scanEntry.getPath(), exception));
    }

    /**
     * Returns an unmodifiable list of all build entries.
     *
     * @return An unmodifiable list of build entries.
     */
    public List<BuildEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns an unmodifiable list of all exceptions that occurred during building.
     *
     * @return An unmodifiable list of exceptions.
     */
    public List<JsonExceptionEntry> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * Checks if any build entries exist.
     *
     * @return true if at least one entry was built, false otherwise.
     */
    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    /**
     * Checks if any exceptions occurred during building.
     *
     * @return true if at least one exception occurred, false otherwise.
     */
    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    /**
     * Checks if a WoodProviderBox was produced.
     *
     * @return true if a provider box was produced, false otherwise.
     */
    public boolean hasProvider() {
        return woodProviderBox != null;
    }

    /**
     * Returns the merged WoodProviderBox containing all successfully built providers.
     *
     * @return The merged WoodProviderBox, or null if none was built.
     */
    public WoodProviderBox getWoodProviderBox() {
        return woodProviderBox;
    }

    /**
     * The BuildEntry class represents a single successfully built WoodProviderBox.
     * It stores the scan entry that produced the box and the box itself.
     */
    public static final class BuildEntry {

        private final JsonWoodProviderScanResult.ProviderNodeEntry scanEntry;
        private final WoodProviderBox woodProviderBox;

        /**
         * Constructs a BuildEntry instance.
         *
         * @param scanEntry The scan entry that produced this box.
         * @param woodProviderBox The built WoodProviderBox.
         * @throws NullPointerException If scanEntry or woodProviderBox is null.
         */
        public BuildEntry(JsonWoodProviderScanResult.ProviderNodeEntry scanEntry, WoodProviderBox woodProviderBox) {
            this.scanEntry = Objects.requireNonNull(scanEntry, "scanEntry must not be null");
            this.woodProviderBox = Objects.requireNonNull(woodProviderBox, "woodProviderBox must not be null");
        }

        /**
         * Returns the scan entry that produced this box.
         *
         * @return The scan entry.
         */
        public JsonWoodProviderScanResult.ProviderNodeEntry getScanEntry() {
            return scanEntry;
        }

        /**
         * Returns the built WoodProviderBox.
         *
         * @return The WoodProviderBox.
         */
        public WoodProviderBox getWoodProviderBox() {
            return woodProviderBox;
        }

        /**
         * Returns the owner JSON node from the scan entry.
         *
         * @return The owner JSON node.
         */
        public JsonNode getOwnerNode() {
            return scanEntry.getOwnerNode();
        }

        /**
         * Returns the path from the scan entry.
         *
         * @return The path string.
         */
        public String getPath() {
            return scanEntry.getPath();
        }
    }

}
