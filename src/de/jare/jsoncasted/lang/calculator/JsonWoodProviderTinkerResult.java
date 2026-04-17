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

public final class JsonWoodProviderTinkerResult {

    private final List<BuildEntry> entries = new ArrayList<>();
    private final List<JsonExceptionEntry> exceptions = new ArrayList<>();
    private WoodProviderBox woodProviderBox = null;

    void registerBox(JsonWoodProviderScanResult.ProviderNodeEntry scanEntry, WoodProviderBox box) {
        entries.add(new BuildEntry(scanEntry, box));
        if (woodProviderBox == null) {
            woodProviderBox = box;
        } else {
            woodProviderBox.mergeBox(box);
        }
    }

    void registerException(JsonWoodProviderScanResult.ProviderNodeEntry scanEntry, Exception exception) {
        exceptions.add(new JsonExceptionEntry(scanEntry.getOwnerNode(), scanEntry.getPath(), exception));

    }

    public List<BuildEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<JsonExceptionEntry> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    public boolean hasProvider() {
        return woodProviderBox != null;
    }

    public WoodProviderBox getWoodProviderBox() {
        return woodProviderBox;
    }

    public static final class BuildEntry {

        private final JsonWoodProviderScanResult.ProviderNodeEntry scanEntry;
        private final WoodProviderBox woodProviderBox;

        public BuildEntry(JsonWoodProviderScanResult.ProviderNodeEntry scanEntry, WoodProviderBox woodProviderBox) {
            this.scanEntry = Objects.requireNonNull(scanEntry, "scanEntry must not be null");
            this.woodProviderBox = Objects.requireNonNull(woodProviderBox, "woodProviderBox must not be null");
        }

        public JsonWoodProviderScanResult.ProviderNodeEntry getScanEntry() {
            return scanEntry;
        }

        public WoodProviderBox getWoodProviderBox() {
            return woodProviderBox;
        }

        public JsonNode getOwnerNode() {
            return scanEntry.getOwnerNode();
        }

        public JsonNode getProviderNode() {
            return scanEntry.getProviderNode();
        }

        public String getPath() {
            return scanEntry.getPath();
        }
    }

}
