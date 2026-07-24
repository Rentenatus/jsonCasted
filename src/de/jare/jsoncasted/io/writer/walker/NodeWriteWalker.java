/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategie;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import static de.jare.jsoncasted.lang.JsonNodeType.ARRAY;
import static de.jare.jsoncasted.lang.JsonNodeType.BOOLEAN;
import static de.jare.jsoncasted.lang.JsonNodeType.LONG;
import static de.jare.jsoncasted.lang.JsonNodeType.NULL;
import static de.jare.jsoncasted.lang.JsonNodeType.NUMBER;
import static de.jare.jsoncasted.lang.JsonNodeType.OBJECT;
import static de.jare.jsoncasted.lang.JsonNodeType.STRING;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Janusch Renteantus
 */
public class NodeWriteWalker {

    final WriteNodePath intentPath;
    private final WriteStrategie strategie;
    private final JsonDebugLevel debugLevel;

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param debugLevel The debug level for controlling debug output.
     */
    public NodeWriteWalker(WriteStrategie strategie, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = new WriteNodePath("");
        this.debugLevel = debugLevel;
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param intentPath
     * @param debugLevel The debug level for controlling debug output.
     */
    public NodeWriteWalker(WriteStrategie strategie, WriteNodePath intentPath, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = intentPath;
        this.debugLevel = debugLevel;
    }

    /**
     * Writes a JsonNode structure as JSON.
     *
     * @param node The JsonNode to write.
     */
    public void writeNode(JsonNode node) {
        writeNode(node, intentPath);
    }

    /**
     * Writes a JsonNode structure as JSON with a specified indentation.
     *
     * @param node The JsonNode to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeNode(JsonNode node, WriteNodePath iString) {
        if (node == null) {
            strategie.writeAttrNull(iString);
            return;
        }
        JsonNodeType type = node.getType();
        switch (type) {
            case OBJECT:
                writeNodeObject(node, iString);
                break;
            case ARRAY:
                writeNodeArray(node, iString);
                break;
            case STRING:
                strategie.writeNodeValue('"' + escape(node.asText()) + '"', iString);
                break;
            case NUMBER:
                strategie.writeNodeValue(node.asNumber(), iString);
                break;
            case LONG:
                strategie.writeNodeValue(node.asLong(), iString);
                break;
            case BOOLEAN:
                strategie.writeNodeValue(node.asBoolean(), iString);
                break;
            case NULL:
            default:
                strategie.writeAttrNull(iString);
                break;
        }
    }

    /**
     * Writes a JsonNode object as JSON.
     *
     * @param node The JsonNode object to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeNodeObject(JsonNode node, WriteNodePath iString) {
        strategie.writeStart(null, node, false, false, iString);
        boolean isFollowing = false;
        try {
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
        } finally {
            strategie.writeEnd(null, node, isFollowing, false, iString);
        }
    }

    /**
     * Writes a JsonNode array or a single JsonNode element as a list.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param node The JsonNode to serialize.
     * @param iString
     */
    public void writeNodeArray(JsonNode node, WriteNodePath iString) {
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
    protected void writeNodeArrayItems(JsonNode node, String iString) {
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
