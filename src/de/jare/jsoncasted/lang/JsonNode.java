/* <copyright> 
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.util.*;

/**
 * Simple JsonNode representation supporting objects, arrays, strings, numbers,
 * booleans and null. This replaces the previous generic HashMap-based class.
 */
public class JsonNode {
    public enum Type { OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL }

    private final Type type;
    private final Map<String, JsonNode> objectValue;
    private final List<JsonNode> arrayValue;
    private final String textValue;
    private final Double numberValue;
    private final Boolean boolValue;

    private JsonNode(Type type,
                     Map<String, JsonNode> objectValue,
                     List<JsonNode> arrayValue,
                     String textValue,
                     Double numberValue,
                     Boolean boolValue) {
        this.type = type;
        this.objectValue = objectValue;
        this.arrayValue = arrayValue;
        this.textValue = textValue;
        this.numberValue = numberValue;
        this.boolValue = boolValue;
    }

    public static JsonNode objectNode() {
        return new JsonNode(Type.OBJECT, new LinkedHashMap<>(), null, null, null, null);
    }

    public static JsonNode arrayNode() {
        return new JsonNode(Type.ARRAY, null, new ArrayList<>(), null, null, null);
    }

    public static JsonNode stringNode(String s) {
        return new JsonNode(Type.STRING, null, null, s, null, null);
    }

    public static JsonNode numberNode(double n) {
        return new JsonNode(Type.NUMBER, null, null, null, n, null);
    }

    public static JsonNode booleanNode(boolean b) {
        return new JsonNode(Type.BOOLEAN, null, null, null, null, b);
    }

    public static JsonNode nullNode() {
        return new JsonNode(Type.NULL, null, null, null, null, null);
    }

    // Mutating helpers for building nodes (return this for chaining)
    public JsonNode put(String key, JsonNode value) {
        if (type != Type.OBJECT) throw new IllegalStateException("not an object node");
        objectValue.put(key, value);
        return this;
    }

    public JsonNode add(JsonNode value) {
        if (type != Type.ARRAY) throw new IllegalStateException("not an array node");
        arrayValue.add(value);
        return this;
    }

    // Accessors
    public Type getType() { return type; }
    public Map<String, JsonNode> asObject() { return objectValue; }
    public List<JsonNode> asArray() { return arrayValue; }
    public String asText() { return textValue; }
    public Double asNumber() { return numberValue; }
    public Boolean asBoolean() { return boolValue; }

    @Override
    public String toString() {
        switch (type) {
            case OBJECT:
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                boolean first = true;
                for (Map.Entry<String, JsonNode> e : objectValue.entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    sb.append('"').append(escape(e.getKey())).append('"').append(':').append(e.getValue().toString());
                }
                sb.append("}");
                return sb.toString();
            case ARRAY:
                StringBuilder sa = new StringBuilder();
                sa.append('[');
                for (int i = 0; i < arrayValue.size(); i++) {
                    if (i > 0) sa.append(',');
                    sa.append(arrayValue.get(i).toString());
                }
                sa.append(']');
                return sa.toString();
            case STRING:
                return '"' + escape(textValue) + '"';
            case NUMBER:
                return numberValue.toString();
            case BOOLEAN:
                return boolValue.toString();
            case NULL:
                return "null";
            default:
                return "null";
        }
    }

    private static String escape(String s) {
        if (s == null) return null;
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
