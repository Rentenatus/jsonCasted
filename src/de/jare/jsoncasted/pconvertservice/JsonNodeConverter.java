/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Converter to transform a JsonNode tree into the library's JsonItem model.
 *
 * @author Janusch Rentenatus
 */
public class JsonNodeConverter {

    public static JsonItem convert(JsonNode node, String cName, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node == null) {
            return null;
        }
        JsonTypeDescriptor contextClass = descriptor.getType(cName);
        if (contextClass == null) {
            return null;
        }
        return convert(node, contextClass, descriptor, debugLevel);
    }

    public static JsonItem convert(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node == null) {
            return null;
        }
        switch (node.getType()) {
            case OBJECT:
                return JsonObjectConverter.convertObject(node, contextClass, descriptor, debugLevel);
            case ARRAY:
                return convertArray(node, contextClass, false, descriptor, debugLevel);
            case STRING:
                return convertString(node, contextClass, descriptor, debugLevel);
            case NUMBER:
                return convertNumber(node, contextClass, descriptor, debugLevel);
            case LONG:
                return convertLongNumber(node, contextClass, descriptor, debugLevel);
            case BOOLEAN:
                return convertBoolean(node, contextClass, descriptor, debugLevel);
            case NULL:
                return convertNull(node, contextClass, descriptor, debugLevel);
            default:
                throw new JsonParseException("Unsupported JsonNode type: " + node.getType());
        }
    }

    protected static JsonItem convertArray(JsonNode node, JsonTypeDescriptor contextClass, boolean asList, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        ArrayList<JsonItem> list = new ArrayList<>();
        for (JsonNode child : node.asArray()) {
            list.add(convert(child, contextClass, descriptor, debugLevel));
        }
        return new JsonList(list, asList, contextClass);
    }

    private static JsonItem convertString(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        return new JsonValue(node.toText(), contextClass);
    }

    private static JsonItem convertNumber(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (contextClass.getNodeType() == JsonNodeType.STRING) {
            return new JsonValue(node.toText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asNumber(), contextClass);
    }

    private static JsonItem convertLongNumber(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (contextClass.getNodeType() == JsonNodeType.STRING) {
            return new JsonValue(node.toText(), contextClass);
        }
        if (contextClass.getNodeType() == JsonNodeType.NUMBER) {
            return new JsonValue(node.toNumber(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asLong(), contextClass);
    }

    private static JsonItem convertBoolean(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (contextClass.getNodeType() == JsonNodeType.STRING) {
            return new JsonValue(node.toText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asBoolean(), contextClass);
    }

    private static JsonItem convertNull(JsonNode node, JsonTypeDescriptor contextClass, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        return new JsonValue(contextClass);
    }

    private static void checkType(JsonNode node, JsonTypeDescriptor contextClass) throws JsonParseException {
        if (node.getType() != contextClass.getNodeType()) {
            // allow LONG where NUMBER is expected
            if (!(node.getType() == JsonNodeType.LONG && contextClass.getNodeType() == JsonNodeType.NUMBER)) {
                throw new JsonParseException("JsonNode type: " + node.getType() + " to class type missmatch.");
            }
        }
    }
}
