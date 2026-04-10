/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.writer.inner;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ObjectWriter class handles the serialization of JSON object structures.
 * It converts objects into JSON format while maintaining indentation, type
 * information, and error handling.
 *
 * @author Janusch Rentenatus
 */
public class ObjectWriter {

    private final JsonItemDefinition definition;
    private final JsonType jType;
    String intentString;

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     */
    public ObjectWriter(JsonItemDefinition definition, JsonType jType) {
        this.definition = definition;
        this.jType = jType;
        this.intentString = "";
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     */
    public ObjectWriter(JsonItemDefinition definition, JsonType jType, String intentString) {
        this.definition = definition;
        this.jType = jType;
        this.intentString = intentString;
    }

    /**
     * Writes an object as a JSON structure.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param ob The object to serialize.
     * @throws NullPointerException If the object has no associated JSON class.
     * @throws ClassCastException If the object does not match the expected JSON
     * type.
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
     * @throws ClassCastException If the object does not match the expected JSON
     * type.
     */
    protected JsonClass calculateJsonClass(Object ob) throws NullPointerException, ClassCastException {
        if (jType instanceof JsonMap jMap) {
            JsonClass itemClass = jMap.getItemClass();
            return itemClass;
        }
        JsonClass jClass = definition.getModel().getJsonClass(ob.getClass());
        if (jClass == null) {
            final String msg = "No description found for " + ob.getClass().getTypeName() + ".";
            final NullPointerException ex = new NullPointerException(msg);
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            throw ex;
        }
        if (jType != null && !jType.contains(jClass) && !jClass.isSubOf(jType)) {
            final String msg = "Item has the class '" + jClass.getcName()
                    + "', but the root should have been '" + jType.getcName() + "'.";
            final ClassCastException ex = new ClassCastException(msg);
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
    public void write(PrintWriter out, JsonClass jClass, Object ob) {
        if (jType != null && jType.needCast(definition.getCastingLevel())
                || jClass.needCast(definition.getCastingLevel())) {
            out.print('(');
            out.print(jClass.getcName());
            out.print(')');
        }
        out.print('{');
        String iString = intentString + "  ";
        if (jType != null && jType.needClassDef(definition.getCastingLevel())
                || jClass.needClassDef(definition.getCastingLevel())) {
            out.println();
            out.print(iString);
            out.print("\"_class\": \"");
            out.print(jClass.getcName());
            out.print('\"');
            if (jClass.hasFieldKeys(ob)) {
                out.print(',');
            }
        }
        if (jClass.hasFieldKeys(ob)) {
            out.println();
        }
        Iterator<String> it = jClass.keysForWriteIterator(ob);
        boolean isFollowing = false;

        while (it.hasNext()) {
            JsonField next = jClass.get(it.next());
            Object attr = jClass.getAttr(next, ob);
            if (attr == null && jClass.isSkippingNulls()) {
                continue;
            }
            if (isFollowing) {
                out.print(',');
                out.println();
            }
            isFollowing = true;
            out.print(iString);
            out.print('"');
            out.print(next.getfName());
            out.print('"');
            out.print(": ");
            writeAttr(out, next, attr, iString);
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
        } else if (fieldType.isPrimitive()) {
            writePrimitive(out, fieldType, attr, iString);
        } else {
            writeObject(out, fieldType, attr, iString);
        }
    }

    /**
     * Writes a primitive JSON value.
     */
    protected void writePrimitive(PrintWriter out, JsonType jTypePrim, Object attr, String iString) {
        out.print(jTypePrim.toString(attr));
    }

    /**
     * Writes a JSON list representation.
     */
    protected void writeList(PrintWriter out, JsonType jTypeItem, Object attr, String iString) {
        ListWriter reWriter = new ListWriter(definition, jTypeItem, iString);
        reWriter.write(out, attr);
    }

    /**
     * Writes a JSON object representation.
     */
    protected void writeObject(PrintWriter out, JsonType jTypeItem, Object attr, String iString) {
        ObjectWriter reWriter = new ObjectWriter(definition, jTypeItem, iString);
        reWriter.write(out, attr);
    }

    /**
     * Schreibt eine JsonNode-Struktur als JSON.
     */
    public void writeNode(PrintStream out, JsonNode node) {
        writeNode(new PrintWriter(out), node, intentString);
    }

    /**
     * Schreibt eine JsonNode-Struktur als JSON.
     */
    public void writeNode(PrintWriter out, JsonNode node) {
        writeNode(out, node, intentString);
    }

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

    protected void writeNodeArray(PrintWriter out, JsonNode node, String iString) {
        ListWriter reWriter = new ListWriter(definition, null, iString);
        reWriter.writeNode(out, node);
    }

    // einfacher Escape-Helper, analog zu JsonNode.toString()
    private static String escape(String s) {
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
