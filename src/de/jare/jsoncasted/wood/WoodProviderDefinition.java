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

/**
 *
 * @author Janusch Rentenatus
 */
public class WoodProviderDefinition implements JsonItemDefinition {

    public static final WoodProviderDefinition INSTANCE = new WoodProviderDefinition();

    public static WoodProviderDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass woodProviderBox;

    public WoodProviderDefinition() {
        model = new JsonModel("WoodJson");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");

        JsonClass provider = model.newJsonReflect(WoodProvider.class);
        provider.addCParam("synonym", asString);
        provider.addCParam("filename", asString);

        woodProviderBox = model.newJsonReflect(WoodProviderBox.class);
        woodProviderBox.addCParam("_woodProviders", provider, "getProvider", LIST);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    public JsonClass getWoodProviderBox() {
        return woodProviderBox;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
