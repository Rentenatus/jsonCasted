/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor.def;

import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonCollectionType;
import static de.jare.jsoncasted.model.JsonCollectionType.ARRAY;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import de.jare.jsonconfig.item.ConfigProfileType;
import java.util.HashMap;

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
        
        descriptField = model.newJsonReflect(JsonFieldDescriptor.class);
        
        descriptField.addCParam("fieldName", asString);
        descriptField.addCParam("typeName", asString);
        descriptField.addCParam("collectionType", collectionTypeEnum);
        descriptField.addCParam("required", asBoolean);
        descriptField.addCParam("constructorParam", asBoolean);
        descriptField.addCParam("getter", asString);
        descriptField.addCParam("setter", asString);
        
        descriptType = model.newJsonReflect(JsonTypeDescriptor.class);
        descriptType.setReflective(true);
        descriptType.addCParam("typeName", asString);
        descriptType.addField("nodeType", nodeTypeEnum, "getNodeType", "withNodeType");
        descriptType.addField("skippingNulls", asBoolean, "isSkippingNulls", "withSkippingNulls");
        descriptType.addField("primitive", asBoolean, "isPrimitive", "withPrimitive");
        descriptType.addField("reflective", asBoolean, "isReflective", "withReflective");
        descriptType.addField("mappingAllFields", descriptField);
        descriptType.addField("parent", descriptType);
        
        JsonMap typeMap = model.newRawJsonMapIndividually((new JsonInstance<JsonTypeDescriptor>()).getClass(), (String) null, descriptType);
        JsonMap fieldMap = model.newRawJsonMapIndividually((new JsonInstance<JsonFieldDescriptor>()).getClass(), (String) null, descriptField);
        descriptModel = model.newJsonReflect(JsonModelDescriptor.class);
        descriptModel.addCParam("name", asString);
        descriptModel.addField("describedTypes", typeMap);
        descriptModel.addField("repoDescriptors", fieldMap);

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
