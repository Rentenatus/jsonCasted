/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderScanResult;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderScanner;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderTinker;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderTinkerResult;
import de.jare.jsoncasted.io.parserservice.WoodIdFinder;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.io.JsonParseException;

/**
 * The RootConverter class provides the main entry point for converting JSON
 * resources into JsonItem instances. It coordinates the creation of JsonSystem,
 * wood resolution, and the conversion process.
 *
 * @author Janusch Rentenatus
 */
public final class RootConverter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException Always thrown as this is a utility class.
     */
    private RootConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a JSON resource into a JsonItem using the specified context
     * class name and model.This is the primary method for converting JSON
     * resources to the internal JsonItem model.
     *
     * @param res The JSON resource to convert.
     * @param cName The name of the context/root class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @return The converted JsonItem, or null if the resource or its root is
     * null.
     * @throws JsonParseException If conversion fails.
     */
    public static JsonItem convert(JsonResource res, String cName, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (res == null) {
            return null;
        }
        if (res.getRoot() == null) {
            return null;
        }

        // Falls die Resource nicht durch RootParser.parse() initialisiert wurde:
        // Führe die fehlende Provider-/Linking-Initialisierung analog zu RootParser.parse() aus
        if (res.getLinkingSet() == null || res.getExpectedBox() == null) {
            // 1. LinkingSet aufbauen (für _woodObjectId / _woodLink)
            res.setLinkingSet(
                WoodIdFinder.buildLinkingSet(res.getRoot(), res.getProviderName(), debugLevel)
            );

            // 2. Provider scannen und WoodProviderBox bauen
            JsonWoodProviderScanResult scan = JsonWoodProviderScanner.INSTANCE.scan(res.getRoot());
            JsonWoodProviderTinkerResult tinkerResult = JsonWoodProviderTinker.INSTANCE.build(scan, debugLevel);
            res.setExpectedBox(tinkerResult.getWoodProviderBox());

            // 3. Exceptions aus Scan/Tinker sammeln
            if (tinkerResult.hasExceptions()) {
                res.addExceptions(tinkerResult.getExceptions());
            }

            // 4. Definitionen extrahieren (aus _woodDefinitions)
            if (tinkerResult.hasDefinitionEntries()) {
                for (JsonWoodProviderScanResult.DefinitionsNodeEntry defEntry : tinkerResult.getDefinitionEntries()) {
                    JsonNode ownerNode = defEntry.getOwnerNode();
                    JsonNode defsNode = ownerNode.asObjectValues().get(JsonTerms.TERM_WOOD_DEFINITIONS);
                    if (defsNode != null && defsNode.isObject()) {
                        for (JsonNode childNode : defsNode.asObjectValues().values()) {
                            res.addDefinitionNode(childNode);
                        }
                    }
                }
            }
        }

        JsonSystem sys = JsonSystem.of(res);
        WoodResolution resolution = WoodResolver.resolve(sys, descriptor, debugLevel);
        return JsonNodeConverter.convert(res, cName, descriptor, resolution, debugLevel);
    }

}
