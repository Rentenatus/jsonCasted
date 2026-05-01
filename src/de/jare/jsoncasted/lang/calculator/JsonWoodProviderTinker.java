/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang.calculator;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.wood.WoodProviderBox;
import de.jare.jsoncasted.wood.WoodProviderDefinition;
import java.io.IOException;
import java.util.Objects;

/**
 * The JsonWoodProviderTinker class builds WoodProviderBox instances from scan results.
 * It processes the results of scanning JSON for wood provider definitions and converts
 * them into usable WoodProviderBox objects.
 *
 * @author Janusch Rentenatus
 */
public final class JsonWoodProviderTinker {

    /**
     * Singleton instance of JsonWoodProviderTinker.
     */
    public final static JsonWoodProviderTinker INSTANCE = new JsonWoodProviderTinker();

    private final WoodProviderDefinition definition;

    /**
     * Constructs a JsonWoodProviderTinker instance with the default definition.
     */
    public JsonWoodProviderTinker() {
        this(WoodProviderDefinition.getInstance());
    }

    /**
     * Constructs a JsonWoodProviderTinker instance with a specified definition.
     *
     * @param definition The wood provider definition to use.
     * @throws NullPointerException If definition is null.
     */
    public JsonWoodProviderTinker(WoodProviderDefinition definition) {
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
    }

    /**
     * Builds WoodProviderBox instances from a scan result.
     *
     * @param scanResult The scan result containing provider nodes to process.
     * @param debugLevel The debug level for controlling debug output.
     * @return The tinker result containing built provider boxes and any exceptions.
     * @throws NullPointerException If scanResult or debugLevel is null.
     */
    public JsonWoodProviderTinkerResult build(JsonWoodProviderScanResult scanResult, JsonDebugLevel debugLevel) {
        Objects.requireNonNull(scanResult, "scanResult must not be null");

        JsonWoodProviderTinkerResult result = new JsonWoodProviderTinkerResult();

        for (JsonWoodProviderScanResult.ProviderNodeEntry entry : scanResult.getProviderNodes()) {
            buildEntry(entry, result, debugLevel);
        }

        return result;
    }

    /**
     * Builds a single provider entry from a scan result entry.
     *
     * @param entry The scan entry to process.
     * @param result The tinker result to add results to.
     * @param debugLevel The debug level for controlling debug output.
     */
    private void buildEntry(JsonWoodProviderScanResult.ProviderNodeEntry entry, JsonWoodProviderTinkerResult result, JsonDebugLevel debugLevel) {
        try {
            JsonResource res = JsonResource.forRoot(entry.getOwnerNode());
            res.setLinkingSet(new LinkingSet("self"));
            JsonItem jsonItem = JsonParser.parse(res,
                    definition.getDescriptor(), definition.getWoodProviderBox().getcName(), debugLevel);
            Object instance = JsonBuilder.buildInstance(definition.getModel(), true, jsonItem);

            if (!(instance instanceof WoodProviderBox)) {
                result.registerException(
                        entry,
                        new IllegalStateException("Built instance is not a WoodProviderBox: " + instance)
                );
                return;
            }

            result.registerBox(entry, (WoodProviderBox) instance);
        } catch (JsonParseException | JsonBuildException | NullPointerException | IOException ex) {
            debugLevel.warning(ex, () -> "Failed parse Wood Provider Box.");
            result.registerException(entry, ex);
        }
    }

}
