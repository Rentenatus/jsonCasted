/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import static de.jare.jsoncasted.lang.JsonNodeType.ARRAY;
import static de.jare.jsoncasted.lang.JsonNodeType.BOOLEAN;
import static de.jare.jsoncasted.lang.JsonNodeType.LONG;
import static de.jare.jsoncasted.lang.JsonNodeType.NULL;
import static de.jare.jsoncasted.lang.JsonNodeType.NUMBER;
import static de.jare.jsoncasted.lang.JsonNodeType.OBJECT;
import static de.jare.jsoncasted.lang.JsonNodeType.STRING;
import java.util.*;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonParseException;

/**
 * Simple JsonNode representation supporting objects, arrays, strings, numbers,
 * booleans and null. This replaces the previous generic HashMap-based class.
 *
 * Note: JsonClass may be attached to OBJECT nodes to aid editing/debugging.
 *
 * @author Janusch Renteantus
 */
public class JsonNode {

    private final JsonNodeType type;
    private final Map<String, JsonNode> objectValue;
    private final List<JsonNode> arrayValue;
    private final String textValue;
    private final Double numberValue;
    private final Long numberLongValue;
    private final Boolean boolValue;
    private JsonClass jsonClass; // optional, set for OBJECT nodes

    private JsonNode(JsonNodeType type,
            Map<String, JsonNode> objectValue,
            List<JsonNode> arrayValue,
            String textValue,
            Double numberValue,
            Long numberLongValue,
            Boolean boolValue) {
        this.type = type;
        this.objectValue = objectValue;
        this.arrayValue = arrayValue;
        this.textValue = textValue;
        this.numberValue = numberValue;
        this.numberLongValue = numberLongValue;
        this.boolValue = boolValue;
        this.jsonClass = null;
    }

    public static JsonNode objectNode() {
        return new JsonNode(JsonNodeType.OBJECT, new LinkedHashMap<>(), null, null, null, null, null);
    }

    public static JsonNode arrayNode() {
        return new JsonNode(JsonNodeType.ARRAY, null, new ArrayList<>(), null, null, null, null);
    }

    public static JsonNode arrayNode(List<JsonNode> arrayValue) {
        return new JsonNode(JsonNodeType.ARRAY, null, arrayValue, null, null, null, null);
    }

    public static JsonNode stringNode(String str) {
        return new JsonNode(JsonNodeType.STRING, null, null, str, null, null, null);
    }

    public static JsonNode numberNode(double number) {
        return new JsonNode(JsonNodeType.NUMBER, null, null, null, number, null, null);
    }

    public static JsonNode longNode(long number) {
        return new JsonNode(JsonNodeType.LONG, null, null, null, null, number, null);
    }

    public static JsonNode booleanNode(boolean aBool) {
        return new JsonNode(JsonNodeType.BOOLEAN, null, null, null, null, null, aBool);
    }

    public static JsonNode varNode(String str) {
        if (str == null) {
            return JsonNode.nullNode();
        }
        String trimmed = str.trim();
        if ("true".equals(trimmed)) {
            return JsonNode.booleanNode(true);
        }
        if ("false".equals(trimmed)) {
            return JsonNode.booleanNode(false);
        }
        // Prefer long when there's no decimal point or exponent
        if (trimmed.matches("-?\\d+")) {
            try {
                return JsonNode.longNode(Long.parseLong(trimmed));
            } catch (NumberFormatException e) {
                // fall through to double parsing as a fallback
            }
        }
        try {
            return JsonNode.numberNode(Double.parseDouble(trimmed));
        } catch (NumberFormatException e) {
            return JsonNode.stringNode(str);
        }
    }

    public static JsonNode nullNode() {
        return new JsonNode(JsonNodeType.NULL, null, null, null, null, null, null);
    }

    // Mutating helpers for building nodes (return this for chaining)
    public JsonNode put(String key, JsonNode value) {
        if (type != JsonNodeType.OBJECT) {
            throw new IllegalStateException("not an object node");
        }
        objectValue.put(key, value);
        return this;
    }

    public JsonNode add(JsonNode value) {
        if (type != JsonNodeType.ARRAY) {
            throw new IllegalStateException("not an array node");
        }
        arrayValue.add(value);
        return this;
    }

    // JsonClass attachment for OBJECT nodes
    public void setJsonClass(JsonClass jc) {
        this.jsonClass = jc;
    }

    public JsonClass getJsonClass() {
        return this.jsonClass;
    }

    // Accessors
    public JsonNodeType getType() {
        return type;
    }

    public Map<String, JsonNode> asObjectValues() {
        return objectValue;
    }

    public List<JsonNode> asArray() {
        return arrayValue;
    }

    public String asText() {
        return textValue;
    }

    public Double asNumber() {
        return numberValue;
    }

    public Long asLong() {
        return numberLongValue;
    }

    public Boolean asBoolean() {
        return boolValue;
    }

    public Double toNumber() {
        if (numberLongValue != null) {
            return numberLongValue.doubleValue();
        }
        return numberValue;
    }

    public String toText() throws JsonParseException {
        switch (type) {
            case STRING:
                return asText();
            case NUMBER:
                return String.valueOf(asNumber());
            case LONG:
                return String.valueOf(asLong());
            case BOOLEAN:
                return String.valueOf(asBoolean());
            case NULL:
                return "null";
            default:
                throw new JsonParseException("Unsupported JsonNode type: " + type);
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case OBJECT:
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                boolean first = true;
                for (Map.Entry<String, JsonNode> e : objectValue.entrySet()) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    sb.append('"').append(escape(e.getKey())).append('"').append(':').append(e.getValue().toString());
                }
                sb.append("}");
                return sb.toString();
            case ARRAY:
                StringBuilder sa = new StringBuilder();
                sa.append('[');
                for (int i = 0; i < arrayValue.size(); i++) {
                    if (i > 0) {
                        sa.append(',');
                    }
                    sa.append(arrayValue.get(i).toString());
                }
                sa.append(']');
                return sa.toString();
            case STRING:
                return '"' + escape(textValue) + '"';
            case NUMBER:
                return numberValue.toString();
            case LONG:
                return numberLongValue.toString();
            case BOOLEAN:
                return boolValue.toString();
            case NULL:
                return "null";
            default:
                return "null";
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
