/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor.def;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonDescriptorDefinitionAsFile implements JsonItemDefinition {

    public static final JsonDescriptorDefinitionAsFile INSTANCE = new JsonDescriptorDefinitionAsFile();

    public static JsonDescriptorDefinitionAsFile getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass descriptModel;

    public JsonDescriptorDefinitionAsFile() {
        model = new JsonModel("Light Model");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");

        descriptModel = model.newJsonReflect(JsonModelDescriptor.class);
        descriptModel.addCParam("modelName", asString);
        descriptModel.addField("modelFile", asString);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    @Override
    public JsonClass getRootClass() {
        return descriptModel;
    }

    public JsonClass getDescriptModel() {
        return descriptModel;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
