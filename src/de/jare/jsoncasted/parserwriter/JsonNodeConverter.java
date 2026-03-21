package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.model.item.JsonClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Converter to transform a JsonNode tree into the library's JsonItem model.
 */
public class JsonNodeConverter {

    public static JsonItem convert(JsonNode node, JsonItemDefinition definition, JsonClass contextClass, JsonDebugLevel debugLevel) throws JsonParseException {
        if (node == null) return null;
        switch (node.getType()) {
            case OBJECT:
                return convertObject(node, definition, contextClass, debugLevel);
            case ARRAY:
                return convertArray(node, definition, contextClass, debugLevel);
            case STRING:
                return new JsonValue(node.asText(), contextClass);
            case NUMBER:
                return new JsonValue(node.asNumber().toString(), contextClass);
            case BOOLEAN:
                return new JsonValue(node.asBoolean().toString(), contextClass);
            case NULL:
                return new JsonValue("null", contextClass);
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
            if (clsNode != null && clsNode.getType() == JsonNode.Type.STRING) {
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
            if ("_class".equals(key)) continue;
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
}
