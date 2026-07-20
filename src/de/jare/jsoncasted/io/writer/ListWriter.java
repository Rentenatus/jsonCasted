/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.definitions.DefinitionsContext;
import de.jare.jsoncasted.io.writer.getter.ListGetter;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonType;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The ListWriter class handles the serialization of JSON list structures. It converts collections into JSON array
 * format while maintaining indentation and type information.
 *
 * @author Janusch Rentenatus
 */
class ListWriter {

    private String intentString;
    final ListGetter listGetter;

    /**
     * Constructs a ListWriter instance with default indentation.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     */
    public ListWriter(DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel) {
        this.intentString = "";
        this.listGetter = new ListGetter(definitionsContext, jType, castingLevel, de.jare.debug.JsonDebugLevel.SIMPLE);
    }

    /**
     * Constructs a ListWriter instance with a specified indentation string.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     */
    public ListWriter(DefinitionsContext definitionsContext, JsonType jType, String intentString, JsonCastingLevel castingLevel) {
        this.intentString = intentString;
        this.listGetter = new ListGetter(definitionsContext, jType, castingLevel, de.jare.debug.JsonDebugLevel.SIMPLE);
    }

    /**
     * Constructs a ListWriter instance with a specified indentation string.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ListWriter(DefinitionsContext definitionsContext, JsonType jType, String intentString, JsonCastingLevel castingLevel, de.jare.debug.JsonDebugLevel debugLevel) {
        this.intentString = intentString;
        this.listGetter = new ListGetter(definitionsContext, jType, castingLevel, debugLevel);
    }

    /**
     * Writes a collection or object as a JSON array.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param ob The object or collection to serialize.
     */
    protected void write(PrintWriter out, Object ob) {
        out.print('[');
        String iString = intentString + "  ";

        if (!listGetter.isPrimitive()) {
            out.println();
            out.print(iString);
        }

        Collection<?> list = listGetter.extractCollection(ob);
        Iterator<?> it = listGetter.iterator(ob);

        while (it.hasNext()) {
            Object next = it.next();
            writeEntry(out, next, iString);

            if (it.hasNext()) {
                out.print(", ");
                if (!listGetter.isPrimitive()) {
                    out.println();
                    out.print(iString);
                }
            }
        }

        if (!listGetter.isPrimitive()) {
            out.println();
            out.print(intentString);
        }
        out.print(']');
        out.flush();
    }

    /**
     * Writes an individual JSON entry, handling primitive and object types.
     *
     * @param out The PrintWriter for output.
     * @param entry The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeEntry(PrintWriter out, Object entry, String iString) {
        if (entry == null) {
            out.print("null");
        } else if (listGetter.isPrimitive()) {
            writePrimitive(out, entry, iString);
        } else {
            writeObject(out, entry, iString);
        }
    }

    /**
     * Serializes a primitive JSON value.
     *
     * @param out The PrintWriter for output.
     * @param attr The primitive value to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writePrimitive(PrintWriter out, Object attr, String iString) {
        out.print(listGetter.toPrimitiveString(attr));
    }

    /**
     * Serializes a complex JSON object.
     *
     * @param out The PrintWriter for output.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeObject(PrintWriter out, Object attr, String iString) {
        ObjectWriter reWriter = new ObjectWriter(listGetter.getDefinitionsContext(), listGetter.getjType(), iString, listGetter.getCastingLevel(), listGetter.getDebugLevel());
        reWriter.write(out, reWriter.calculateJsonClass(attr), attr);
    }

    /**
     * Writes a JsonNode array or a single JsonNode element as a list.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param node The JsonNode to serialize.
     */
    public void writeNode(PrintWriter out, JsonNode node) {
        out.print('[');
        if (node != null && node.getType() != JsonNodeType.ARRAY) {
            ObjectWriter reWriter = new ObjectWriter(listGetter.getDefinitionsContext(), null, intentString, listGetter.getCastingLevel(), listGetter.getDebugLevel());
            reWriter.writeNode(out, node);
        } else {
            writeNodeArrayItems(out, node, intentString);
        }
        out.print(intentString);
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
        ObjectWriter reWriter = new ObjectWriter(listGetter.getDefinitionsContext(), null, iString + "  ", listGetter.getCastingLevel(), listGetter.getDebugLevel());
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
                reWriter.writeNode(out, item, childIndent);
            }
            out.println();
        }
    }

}
