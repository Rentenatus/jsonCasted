/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.wood;

import de.jare.jsonconfig.def.*;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonBuildException;
import static de.jare.jsoncasted.model.JsonCollectionType.ARRAY;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonReflectBuilder;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import static de.jare.jsoncasted.parserwriter.JsonValidationMethod.ENDSWITH;
import static de.jare.jsoncasted.parserwriter.JsonValidationMethod.EQUALS;
import de.jare.jsonconfig.item.ConfigFeature;
import de.jare.jsonconfig.item.ConfigLogging;
import de.jare.jsonconfig.item.ConfigProfile;
import de.jare.jsonconfig.item.ConfigProfileType;
import de.jare.jsonconfig.item.ConfigRoot;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        final JsonClass asBoolean = model.getJsonClass("Boolean");

        JsonClass provider = model.newJsonReflect(WoodProvider.class);
        provider.addCParam("synonym", asString);
        provider.addCParam("filename", asString);
        provider.addCParam("myself", asBoolean);

        woodProviderBox = model.newJsonReflect(WoodProviderBox.class);
        woodProviderBox.addCParam("providers", provider, LIST);

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
