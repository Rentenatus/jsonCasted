/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Converter to transform a JsonNode tree into the library's JsonItem model.
 */
public class JsonNodeConverter {

    public static JsonItem convert(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node == null) {
            return null;
        }
        switch (node.getType()) {
            case OBJECT:
                return convertObject(node, definition, contextClass, debugLevel);
            case ARRAY:
                return convertArray(node, definition, contextClass, debugLevel);
            case STRING:
                return convertString(node, definition, contextClass, debugLevel);
            case NUMBER:
                return convertNumber(node, definition, contextClass, debugLevel);
            case LONG:
                return convertLongNumber(node, definition, contextClass, debugLevel);
            case BOOLEAN:
                return convertBoolean(node, definition, contextClass, debugLevel);
            case NULL:
                return convertNull(node, definition, contextClass, debugLevel);
            default:
                throw new JsonParseException("Unsupported JsonNode type: " + node.getType());
        }
    }

    private static JsonItem convertObject(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        Map<String, JsonNode> map = node.asObject();
        JsonClass useClass = contextClass;
        // detect _class property
        if (map.containsKey("_class")) {
            JsonNode clsNode = map.get("_class");
            if (clsNode != null && clsNode.getType() == JsonNodeType.STRING) {
                String cname = clsNode.asText();
                if (definition != null && definition.getModel() != null) {
                    JsonClass found = definition.getModel().getJsonClass(cname);
                    if (found == null) {
                        // try endsWith (best-effort)
                        found = definition.getModel().getEndsWith(cname);
                    }
                    if (found == null && cname.contains(".")) {
                        // try simple name (e.g., java.lang.String -> String)
                        String simple = cname.substring(cname.lastIndexOf('.') + 1);
                        found = definition.getModel().getJsonClass(simple);
                    }
                    if (found != null) {
                        useClass = found;
                    }
                }
            }
        }
        JsonObject obj = new JsonObject(useClass);
        // attach JsonClass to the JsonNode for editor/debugging
        node.setJsonClass(useClass);
        // copy properties, skip _class
        for (Map.Entry<String, JsonNode> e : map.entrySet()) {
            String key = e.getKey();
            if ("_class".equals(key)) {
                continue;
            }
            JsonNode child = e.getValue();
            JsonItem converted = convert(child, definition, null, debugLevel);
            obj.putParam(key, converted);
        }
        return obj;
    }

    private static JsonItem convertArray(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        ArrayList<JsonItem> list = new ArrayList<>();
        for (JsonNode child : node.asArray()) {
            list.add(convert(child, definition, null, debugLevel));
        }
        return new JsonList(list, true, null);
    }

    private static JsonItem convertString(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        return new JsonValue(node.toText(), contextClass);
    }

    private static JsonItem convertNumber(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node.asText() != null) {
            return new JsonValue(node.asText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asNumber(), contextClass);
    }

    private static JsonItem convertLongNumber(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node.asText() != null) {
            return new JsonValue(node.asText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asLong(), contextClass);
    }

    private static JsonItem convertBoolean(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node.asText() != null) {
            return new JsonValue(node.asText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asBoolean(), contextClass);
    }

    private static JsonItem convertNull(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        return new JsonValue(contextClass);
    }

    private static void checkType(JsonNode node, JsonClass contextClass) throws JsonParseException {
        if (node.getType() != contextClass.getNodeType()) {
            // allow LONG where NUMBER is expected
            if (!(node.getType() == JsonNodeType.LONG && contextClass.getNodeType() == JsonNodeType.NUMBER)) {
                throw new JsonParseException("JsonNode type: " + node.getType() + " to class type missmatch.");
            }
        }
    }
}
