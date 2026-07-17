/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer;

import de.jare.debug.DebugTuple;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ObjectWriter class handles the serialization of JSON object structures. It converts objects into JSON format
 * while maintaining indentation, type information, and error handling.
 *
 * @author Janusch Rentenatus
 */
public class ObjectWriter {

    private final JsonType jType;
    String intentString;
    final JsonCastingLevel castingLevel;
    final JsonModel model;
    final JsonDebugLevel debugLevel;

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriter(JsonItemDefinition definition, JsonType jType, JsonDebugLevel debugLevel) {
        this.castingLevel = definition.getCastingLevel();
        this.model = definition.getModel();
        this.jType = jType;
        this.intentString = "";
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriter(JsonItemDefinition definition, JsonType jType, String intentString, JsonDebugLevel debugLevel) {
        this.castingLevel = definition.getCastingLevel();
        this.model = definition.getModel();
        this.jType = jType;
        this.intentString = intentString;
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriter(JsonModel model, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.castingLevel = castingLevel;
        this.model = model;
        this.jType = jType;
        this.intentString = "";
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriter(JsonModel model, JsonType jType, String intentString, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.castingLevel = castingLevel;
        this.model = model;
        this.jType = jType;
        this.intentString = intentString;
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    /**
     * Writes an object as a JSON structure.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param ob The object to serialize.
     * @throws NullPointerException If the object has no associated JSON class.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    protected void write(PrintWriter out, Object ob) throws NullPointerException, ClassCastException {
        JsonClass jClass = calculateJsonClass(ob);
        out.print(intentString);
        write(out, jClass, ob);
    }

    /**
     * Determines the JSON class associated with an object.
     *
     * @param ob The object to analyze.
     * @return The corresponding JsonClass representation.
     * @throws NullPointerException If no class description is found.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    protected JsonClass calculateJsonClass(Object ob) throws NullPointerException, ClassCastException {
        if (jType instanceof JsonMap jMap) {
            JsonClass keyClass = jMap.getItemClass();
            return keyClass;
        }
        JsonClass jClass = model.getJsonClass(ob.getClass());
        if (jClass == null) {
            final String msg = "No description found for " + ob.getClass().getTypeName() + ".";
            final NullPointerException ex = new NullPointerException(msg);
            debugLevel.warning(() -> new DebugTuple(msg, (Object[]) null));
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            throw ex;
        }
        if (jType != null && !jType.contains(jClass) && !jClass.isSubOf(jType)) {
            final String msg = "Item has the class '" + jClass.getcName()
                    + "', but the root should have been '" + jType.getcName() + "'.";
            final ClassCastException ex = new ClassCastException(msg);
            debugLevel.warning(() -> new DebugTuple(msg, (Object[]) null));
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            throw ex;
        }
        return jClass;
    }

    /**
     * Writes a JSON object representation.
     *
     * @param out The PrintStream for output.
     * @param jClass The JSON class defining the object's structure.
     * @param ob The object to serialize.
     */
    public void write(PrintStream out, JsonClass jClass, Object ob) {
        write(new PrintWriter(out), jClass, ob);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param out The PrintWriter for output.
     * @param jClass The JSON class defining the object's structure.
     * @param ob The object to serialize.
     */
    public void write(final PrintWriter out, final JsonClass jClass, final Object ob) {
        String iString = intentString + "  ";
        writeCast(jClass, out, ob, iString);

        if (jClass.hasFieldKeys(ob)) {
            out.println();
        }
        Iterator<String> it = jClass.keysForWriteIterator(ob);
        boolean isFollowing = false;

        while (it.hasNext()) {
            JsonField next = jClass.getField(it.next());
            Object attr = jClass.getAttr(next, ob, debugLevel);
            if (attr == null && jClass.isSkippingNulls()) {
                continue;
            }
            if (isFollowing) {
                out.print(',');
                out.println();
            }
            out.print(iString);
            out.print('"');
            out.print(next.getfName());
            out.print('"');
            out.print(": ");
            writeAttr(out, next, attr, iString);
            isFollowing = true;
        }

        if (isFollowing) {
            out.println();
        }
        if (jClass.hasFieldKeys(ob)) {
            out.print(intentString);
        }
        out.print('}');
        out.flush();
    }

    void writeCast(final JsonClass jClass, final PrintWriter out, final Object ob, String iString) {
        if (jType != null && jType.needCast(castingLevel)
                || jClass.needCast(castingLevel)) {
            out.print('(');
            out.print(jClass.getcName());
            out.print(')');
        }
        out.print('{');

        if (jType != null && jType.needClassDef(castingLevel)
                || jClass.needClassDef(castingLevel)) {
            out.println();
            out.print(iString);
            out.print("\"_class\": \"");
            out.print(jClass.getcName());
            out.print('\"');
            if (jClass.hasFieldKeys(ob)) {
                out.print(',');
            }
        }
    }

    /**
     * Writes a JSON attribute based on its type.
     *
     * @param out The PrintWriter for output.
     * @param jField The JSON field definition.
     * @param attr The attribute value.
     * @param iString The indentation string for formatted output.
     */
    protected void writeAttr(PrintWriter out, JsonField jField, Object attr, String iString) {
        if (attr == null) {
            out.print(" null");
            return;
        }
        final JsonType fieldType = jField.getjType();
        if (jField.isAsListOrArray()) {
            writeList(out, fieldType, attr, iString);
        } else {
            writeSingle(out, fieldType, attr, iString);
        }
    }

    public void writeSingle(PrintWriter out, final JsonType fieldType, Object attr, String iString) {
        if (fieldType.isPrimitive()) {
            writePrimitive(out, fieldType, attr, iString);
        } else if (fieldType instanceof JsonMap jMap) {
            writeMap(out, jMap, attr, iString);
        } else {
            writeObject(out, fieldType, attr, iString);
        }
    }

    /**
     * Writes a primitive JSON value.
     *
     * @param out The PrintWriter for output.
     * @param jTypePrim The JSON type of the primitive.
     * @param attr The primitive value to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writePrimitive(PrintWriter out, JsonType jTypePrim, Object attr, String iString) {
        out.print(jTypePrim.toString(attr));
    }

    /**
     * Writes a JSON list representation.
     *
     * @param out The PrintWriter for output.
     * @param jTypeItem The JSON type of list items.
     * @param attr The list to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeList(PrintWriter out, JsonType jTypeItem, Object attr, String iString) {
        ListWriter reWriter = new ListWriter(model, jTypeItem, iString, castingLevel, debugLevel);
        reWriter.write(out, attr);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param out The PrintWriter for output.
     * @param jMap The JSON type of the object.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeMap(PrintWriter out, JsonMap jMap, Object attr, String iString) {
        MapWriter reWriter = new MapWriter(model, jMap, iString, castingLevel, debugLevel);
        reWriter.write(out, reWriter.calculateJsonClass(attr), attr);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param out The PrintWriter for output.
     * @param jTypeItem The JSON type of the object.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeObject(PrintWriter out, JsonType jTypeItem, Object attr, String iString) {
        ObjectWriter reWriter = new ObjectWriter(model, jTypeItem, iString, castingLevel, debugLevel);
        reWriter.write(out, reWriter.calculateJsonClass(attr), attr);
    }

    /**
     * Writes a JsonNode structure as JSON.
     *
     * @param out The PrintStream for output.
     * @param node The JsonNode to write.
     */
    public void writeNode(PrintStream out, JsonNode node) {
        writeNode(new PrintWriter(out), node, intentString);
    }

    /**
     * Writes a JsonNode structure as JSON.
     *
     * @param out The PrintWriter for output.
     * @param node The JsonNode to write.
     */
    public void writeNode(PrintWriter out, JsonNode node) {
        writeNode(out, node, intentString);
    }

    /**
     * Writes a JsonNode structure as JSON with a specified indentation.
     *
     * @param out The PrintWriter for output.
     * @param node The JsonNode to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeNode(PrintWriter out, JsonNode node, String iString) {
        if (node == null) {
            out.print("null");
            return;
        }
        JsonNodeType type = node.getType();
        switch (type) {
            case OBJECT:
                writeNodeObject(out, node, iString);
                break;
            case ARRAY:
                writeNodeArray(out, node, iString);
                break;
            case STRING:
                out.print('"');
                out.print(escape(node.asText()));
                out.print('"');
                break;
            case NUMBER:
                out.print(node.asNumber());
                break;
            case LONG:
                out.print(node.asLong());
                break;
            case BOOLEAN:
                out.print(node.asBoolean());
                break;
            case NULL:
            default:
                out.print("null");
                break;
        }
        out.flush();
    }

    /**
     * Writes a JsonNode object as JSON.
     *
     * @param out The PrintWriter for output.
     * @param node The JsonNode object to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeNodeObject(PrintWriter out, JsonNode node, String iString) {
        out.print('{');
        Map<String, JsonNode> map = node.asObjectValues();
        if (map != null && !map.isEmpty()) {
            out.println();
            String childIndent = iString + "  ";
            boolean first = true;
            for (Map.Entry<String, JsonNode> e : map.entrySet()) {
                if (!first) {
                    out.print(',');
                    out.println();
                }
                first = false;
                out.print(childIndent);
                out.print('"');
                out.print(escape(String.valueOf(e.getKey())));
                out.print('"');
                out.print(": ");
                writeNode(out, (JsonNode) e.getValue(), childIndent);
            }
            out.println();
            out.print(iString);
        }
        out.print('}');
        out.flush();
    }

    /**
     * Writes a JsonNode array as JSON.
     *
     * @param out The PrintWriter for output.
     * @param node The JsonNode array to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeNodeArray(PrintWriter out, JsonNode node, String iString) {
        ListWriter reWriter = new ListWriter(model, null, iString, castingLevel, debugLevel);
        reWriter.writeNode(out, node);
    }

    /**
     * Escapes special characters in a string for JSON output. Handles backslash, quote, newline, carriage return, and
     * tab characters.
     *
     * @param s The string to escape.
     * @return The escaped string safe for JSON output.
     */
    static String escape(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
