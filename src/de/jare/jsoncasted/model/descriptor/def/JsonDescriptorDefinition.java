/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor.def;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonFieldTypeNote;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonDescriptorDefinition implements JsonItemDefinition {

    public static final JsonDescriptorDefinition INSTANCE = new JsonDescriptorDefinition();

    public static JsonDescriptorDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass descriptType;
    private final JsonClass descriptField;
    private final JsonClass descriptModel;

    public JsonDescriptorDefinition() {
        model = new JsonModel("Seed");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");

        JsonMap stringMap = model.newRawJsonMapIndividually((new JsonInstance<String>()).getClass(), (String) null, asString);
        JsonClass collectionTypeEnum = model.newJsonEnumByName(JsonCollectionType.class);
        JsonClass nodeTypeEnum = model.newJsonEnumByName(JsonNodeType.class);

        JsonClass descriptTypeNote = model.newJsonReflect(JsonFieldTypeNote.class);
        descriptTypeNote.setDefinitional(true);
        descriptTypeNote.addCParam("typeName", asString);
        descriptTypeNote.addCParam("collectionType", collectionTypeEnum);

        descriptField = model.newJsonReflect(JsonFieldDescriptor.class, descriptTypeNote);
        descriptField.addCParam("fieldName", asString);
        descriptField.addCParam("required", asBoolean);
        descriptField.addCParam("constructorParam", asBoolean);
        descriptField.addCParam("getter", asString);
        descriptField.addCParam("setter", asString);

        descriptType = model.newJsonReflect(JsonTypeDescriptor.class);
        descriptType.setDefinitional(true);
        descriptType.addCParam("typeName", asString);
        descriptType.addField("nodeType", nodeTypeEnum, "getNodeType", "withNodeType");
        descriptType.addField("skippingNulls", asBoolean, "isSkippingNulls", "withSkippingNulls");
        descriptType.addField("primitive", asBoolean, "isPrimitive", "withPrimitive");
        descriptType.addField("recursive", asBoolean, "isRecursive", "withRecursive");
        descriptType.addField("mappingAllFields", descriptTypeNote);
        descriptType.addField("parent", descriptType);

        JsonMap typeMap = model.newRawJsonMapIndividually((new JsonInstance<JsonTypeDescriptor>()).getClass(), (String) null, descriptType);
        JsonMap modeldMap = model.newRawJsonMapIndividually((new JsonInstance<JsonModelDescriptor>()).getClass(), (String) null, descriptField);
        descriptModel = model.newJsonReflect(JsonModelDescriptor.class);
        descriptModel.addCParam("modelName", asString);
        descriptModel.addField("describedTypes", typeMap);
        descriptModel.addField("repoDescriptors", modeldMap);

        /*
          private final String modelName;
          private final Map<String, JsonTypeDescriptor> describedTypes = new LinkedHashMap<>();
          private final Map<String, JsonModelDescriptor> repoDescriptors = new LinkedHashMap<>();
         */
    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    public JsonClass getDescriptType() {
        return descriptType;
    }

    public JsonClass getDescriptField() {
        return descriptField;
    }

    public JsonClass getDescriptModel() {
        return descriptModel;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
