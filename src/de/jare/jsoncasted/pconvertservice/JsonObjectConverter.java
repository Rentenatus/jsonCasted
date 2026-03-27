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
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
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

    public static JsonItem convertObject(JsonNode node, JsonClass aClass, JsonItemDefinition definition, JsonDebugLevel debugLevel) throws JsonParseException {
        JsonObjectConverter converter = new JsonObjectConverter(node, aClass, definition, debugLevel);
        return converter.convertObject();
    }

    final Map<String, JsonNode> values;
    final JsonObject myObject;
    final JsonClass aClass;
    final JsonItemDefinition definition;
    final JsonDebugLevel debugLevel;

    JsonObjectConverter(JsonNode node, JsonClass aClass, JsonItemDefinition definition, JsonDebugLevel debugLevel) throws JsonParseException {
        this.values = node.asObjectValues();
        if (aClass == null && values != null) {
            final JsonNode cast = values.get("_class");
            if (cast != null) {
                aClass = definition.getModel().getJsonClass(cast.asText());
            }
        }
        node.setJsonClass(aClass);
        if (aClass == null) {
            throw new JsonParseException("No Class.");
        }
        this.aClass = aClass;
        this.myObject = new JsonObject(aClass);
        this.definition = definition;
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

        JsonField field = aClass.get(paramName);
        if (field == null) {
            return;
        }
        final JsonType childType = field.getjType();
        JsonClass childClass = childType.getDirectClass();
        Map<String, JsonNode> childValues = childNode.asObjectValues();
        final JsonNode cast = childValues == null ? null : childValues.get("_class");
        if (childClass == null) {
            if (cast == null) {
                Logger.getGlobal().log(Level.WARNING, "Missing cast.");
                return;
            }
            childClass = definition.getModel().getJsonClass(cast.asText());
            if (childClass == null) {
                Logger.getGlobal().log(Level.WARNING, "Unknown class.");
                return;
            }
        }
        if (!childType.contains(childClass)) {
            Logger.getGlobal().log(Level.WARNING, "Wrong class.");
            return;
        }

        JsonItem paramObject = field.isAsListOrArray()
                ? JsonNodeConverter.convertArray(childNode, childClass, field.isAsList(), definition, debugLevel)
                : JsonNodeConverter.convert(childNode, childClass, definition, debugLevel);
        myObject.putParam(paramName, paramObject);

    }

}
