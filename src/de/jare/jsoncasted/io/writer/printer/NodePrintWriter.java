/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer.printer;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.io.writer.getter.ObjectGetter;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonType;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * The ObjectWriter class handles the serialization of JSON object structures. It converts objects into JSON format
 * while maintaining indentation, type information, and error handling.
 *
 * @author Janusch Rentenatus
 */
public class NodePrintWriter {

    String intentString;
    final ObjectGetter objectGetter;

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public NodePrintWriter(DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.intentString = "";
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public NodePrintWriter(DefinitionsContext definitionsContext, JsonType jType, String intentString, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.intentString = intentString;
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
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
     * Writes a JsonNode array or a single JsonNode element as a list.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param node The JsonNode to serialize.
     * @param iString
     */
    public void writeNodeArray(PrintWriter out, JsonNode node, String iString) {
        out.print('[');
        if (node != null && node.getType() != JsonNodeType.ARRAY) {
            writeNode(out, node, iString + "  ");
        } else {
            writeNodeArrayItems(out, node, iString + "  ");
        }
        out.print(iString);
        out.print(']');
        out.flush();
    }

    /**
     * Writes the array items of a JsonNode array.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param node The JsonNode array to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeNodeArrayItems(PrintWriter out, JsonNode node, String iString) {
        List<JsonNode> list = node.asArray();
        if (list != null && !list.isEmpty()) {
            out.println();
            String childIndent = iString + " ";
            boolean first = true;
            for (JsonNode item : list) {
                if (!first) {
                    out.print(',');
                    out.println();
                }
                first = false;
                out.print(childIndent);
                writeNode(out, item, childIndent);
            }
            out.println();
        }
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
