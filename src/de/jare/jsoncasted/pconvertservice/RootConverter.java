/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.parserwriter.JsonParseException;

/**
 * The RootConverter class provides the main entry point for converting JSON resources
 * into JsonItem instances. It coordinates the creation of JsonSystem, wood resolution,
 * and the conversion process.
 *
 * @author Janusch Rentenatus
 */
public final class RootConverter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException Always thrown as this is a utility class.
     */
    public RootConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a JSON resource into a JsonItem using the specified context class name and model.
     * This is the primary method for converting JSON resources to the internal JsonItem model.
     *
     * @param res The JSON resource to convert.
     * @param cName The name of the context/root class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param mainModel The main JsonModel for repository model lookup.
     * @param debugLevel The debug level for controlling debug output.
     * @return The converted JsonItem, or null if the resource or its root is null.
     * @throws JsonParseException If conversion fails.
     * @throws IOException If I/O errors occur during resource processing.
     */
    public static JsonItem convert(JsonResource res, String cName, JsonModelDescriptor descriptor, JsonModel mainModel, JsonDebugLevel debugLevel) throws JsonParseException {
        if (res == null) {
            return null;
        }
        if (res.getRoot() == null) {
            return null;
        }

        JsonSystem sys = JsonSystem.of(res);
        sys.setMainModel(mainModel);
        WoodResolution resolution = WoodResolver.resolve(sys, descriptor, debugLevel);
        return JsonNodeConverter.convert(res, cName, descriptor, resolution, debugLevel);
    }

    /**
     * Converts a JSON resource into a JsonItem using the specified context class name.
     * This method uses the descriptor's model for repository model lookup.
     *
     * @param res The JSON resource to convert.
     * @param cName The name of the context/root class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @return The converted JsonItem, or null if the resource or its root is null.
     * @throws JsonParseException If conversion fails.
     */
    public static JsonItem convert(JsonResource res, String cName, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (descriptor == null) {
            return convert(res, cName, descriptor, (JsonModel) null, debugLevel);
        }
        JsonModel mainModel = getModelFromDescriptor(descriptor);
        return convert(res, cName, descriptor, mainModel, debugLevel);
    }

    /**
     * Attempts to extract the JsonModel from a descriptor.
     * This is a fallback method for when the main model is not directly available.
     *
     * @param descriptor The model descriptor.
     * @return The JsonModel if it can be extracted, null otherwise.
     */
    private static JsonModel getModelFromDescriptor(JsonModelDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        return null;
    }

}
