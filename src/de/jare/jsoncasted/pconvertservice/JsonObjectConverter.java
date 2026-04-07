/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonObjectConverter {

    public static JsonItem convertObject(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        JsonObjectConverter converter = new JsonObjectConverter(node, contextClass, descriptor, debugLevel);
        return converter.convertObject();
    }

    final Map<String, JsonNode> values;
    final JsonObject myObject;
    final JsonTypeDescriptor contextClass;
    final JsonModelDescriptor descriptor;
    final JsonDebugLevel debugLevel;

    JsonObjectConverter(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        this.values = node.asObjectValues();
        if (contextClass == null && values != null) {
            final JsonNode cast = values.get(TERM_CLASS);
            if (cast != null) {
                contextClass = descriptor.getType(cast.asText());
            }
        }
        node.setJsonDescriptor(contextClass);
        if (contextClass == null) {
            throw new JsonParseException("No Class.");
        }
        this.contextClass = contextClass;
        this.myObject = new JsonObject(contextClass);
        this.descriptor = descriptor;
        this.debugLevel = debugLevel;
    }

    public JsonItem convertObject() throws JsonParseException {
        ArrayList<JsonParseException> exList = new ArrayList<>();
        values.forEach((paramName, childNode) -> {
            try {
                calculateParam(paramName, childNode);
            } catch (JsonParseException ex) {
                exList.add(ex);
            }
        });
        if (exList.isEmpty()) {
            return myObject;
        }
        if (exList.size() == 1) {
            throw exList.get(0);
        }
        StringJoiner joiner = new StringJoiner("; ");
        for (JsonParseException ex : exList) {
            joiner.add(ex.getMessage());
        }
        throw new JsonParseException(joiner.toString(), exList.get(0));
    }

    protected void calculateParam(String paramName, JsonNode childNode) throws JsonParseException {
        if (contextClass.getMappingAllFields() != null) {
            calculateMapEntry(paramName, childNode);
            return;
        }
        JsonFieldDescriptor field = contextClass.getField(paramName);
        if (field == null) {
            return;
        }
        JsonTypeDescriptor childType = descriptor.getType(field.getTypeName());
        Map<String, JsonNode> childValues = childNode.asObjectValues();
        final JsonNode cast = childValues == null ? null : childValues.get(TERM_CLASS);
        if (childType == null) {
            if (cast == null) {
                Logger.getGlobal().log(Level.WARNING, "Missing cast.");
                return;
            }
            childType = descriptor.getType(cast.asText());
            if (childType == null) {
                Logger.getGlobal().log(Level.WARNING, "Unknown class.");
                return;
            }
        }
        if (!childType.contains(childType)) {
            Logger.getGlobal().log(Level.WARNING, "Wrong class.");
            return;
        }

        JsonItem paramObject = field.isAsListOrArray()
                ? JsonNodeConverter.convertArray(childNode, childType, field.isAsList(), descriptor, debugLevel)
                : JsonNodeConverter.convert(childNode, childType, descriptor, debugLevel);
        myObject.putParam(paramName, paramObject);

    }

    protected void calculateMapEntry(String paramName, JsonNode childNode) throws JsonParseException {
        JsonFieldDescriptor field = contextClass.getMappingAllFields();
        JsonTypeDescriptor childType = descriptor.getType(field.getTypeName());
        JsonItem paramObject = field.isAsListOrArray()
                ? JsonNodeConverter.convertArray(childNode, childType, field.isAsList(), descriptor, debugLevel)
                : JsonNodeConverter.convert(childNode, childType, descriptor, debugLevel);
        myObject.putParam(paramName, paramObject);

    }

}
