/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang.calculator;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.wood.WoodProviderBox;
import de.jare.jsoncasted.wood.WoodProviderDefinition;
import java.io.IOException;
import java.util.Objects;

public final class JsonWoodProviderTinker {

    public final static JsonWoodProviderTinker INSTANCE = new JsonWoodProviderTinker();

    private final WoodProviderDefinition definition;

    public JsonWoodProviderTinker() {
        this(WoodProviderDefinition.getInstance());
    }

    public JsonWoodProviderTinker(WoodProviderDefinition definition) {
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
    }

    public JsonWoodProviderTinkerResult build(JsonWoodProviderScanResult scanResult) {
        Objects.requireNonNull(scanResult, "scanResult must not be null");

        JsonWoodProviderTinkerResult result = new JsonWoodProviderTinkerResult();

        for (JsonWoodProviderScanResult.ProviderNodeEntry entry : scanResult.getProviderNodes()) {
            buildEntry(entry, result);
        }

        return result;
    }

    private void buildEntry(JsonWoodProviderScanResult.ProviderNodeEntry entry, JsonWoodProviderTinkerResult result) {
        try {
            JsonItem jsonItem = JsonParser.parse(entry.getOwnerNode(), definition, definition.getWoodProviderBox());
            Object instance = jsonItem.buildInstance();

            if (!(instance instanceof WoodProviderBox)) {
                result.registerException(
                        entry,
                        new IllegalStateException("Built instance is not a WoodProviderBox: " + instance)
                );
                return;
            }

            result.registerBox(entry, (WoodProviderBox) instance);
        } catch (JsonParseException | JsonBuildException | NullPointerException | IOException ex) {
            result.registerException(entry, ex);
        }
    }

}
