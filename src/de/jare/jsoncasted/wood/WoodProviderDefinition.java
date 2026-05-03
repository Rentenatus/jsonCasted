/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.wood;

import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_PROVIDERS;

/**
 * The WoodProviderDefinition class defines the JSON model structure for wood
 * providers. It creates a singleton instance that describes how WoodProvider
 * and WoodProviderBox classes should be parsed from JSON data.
 *
 * @author Janusch Rentenatus
 */
public class WoodProviderDefinition implements JsonItemDefinition {

    /**
     * Singleton instance of WoodProviderDefinition.
     */
    public static final WoodProviderDefinition INSTANCE = new WoodProviderDefinition();

    /**
     * Returns the singleton instance.
     *
     * @return The singleton WoodProviderDefinition instance.
     */
    public static WoodProviderDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass woodProviderBox;

    /**
     * Constructs a WoodProviderDefinition instance. Creates the model and
     * defines the structure for WoodProvider and WoodProviderBox classes.
     */
    public WoodProviderDefinition() {
        model = new JsonModel("WoodJson");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");

        JsonClass provider = model.newJsonReflect(WoodProvider.class, null);
        provider.addCParam("synonym", asString);
        provider.addCParam("filename", asString);

        woodProviderBox = model.newJsonReflect(WoodProviderBox.class, null);
        woodProviderBox.addCParam(TERM_WOOD_PROVIDERS, provider, "getProvider", LIST);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    /**
     * Returns the JsonClass for WoodProviderBox.
     *
     * @return The JsonClass describing the WoodProviderBox structure.
     */
    public JsonClass getWoodProviderBox() {
        return woodProviderBox;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
