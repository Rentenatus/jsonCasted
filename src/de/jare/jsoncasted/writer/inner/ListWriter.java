/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.writer.inner;

import de.jare.jsoncasted.model.JsonType;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 * The ListWriter class handles the serialization of JSON list structures. It
 * converts collections into JSON array format while maintaining indentation and
 * type information.
 *
 * @author Janusch Rentenatus
 */
class ListWriter {

    private final JsonItemDefinition definition;
    private final JsonType jType;
    private String intentString;

    /**
     * Constructs a ListWriter instance with default indentation.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     */
    public ListWriter(JsonItemDefinition definition, JsonType jType) {
        this.definition = definition;
        this.jType = jType;
        this.intentString = "";
    }

    /**
     * Constructs a ListWriter instance with a specified indentation string.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     */
    public ListWriter(JsonItemDefinition definition, JsonType jType, String intentString) {
        this.definition = definition;
        this.jType = jType;
        this.intentString = intentString;
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

        if (!jType.isPrimitive()) {
            out.println();
            out.print(iString);
        }

        Collection<?> list = (ob instanceof Collection<?>) ? (Collection<?>) ob : jType.asList(ob);
        Iterator<?> it = list.iterator();

        while (it.hasNext()) {
            Object next = it.next();
            writeEntry(out, next, iString);

            if (it.hasNext()) {
                out.print(", ");
                if (!jType.isPrimitive()) {
                    out.println();
                    out.print(iString);
                }
            }
        }

        if (!jType.isPrimitive()) {
            out.println();
            out.print(intentString);
        }
        out.print(']');
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
        } else if (jType.isPrimitive()) {
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
        out.print(jType.toString(attr));
    }

    /**
     * Serializes a complex JSON object.
     *
     * @param out The PrintWriter for output.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeObject(PrintWriter out, Object attr, String iString) {
        ObjectWriter reWriter = new ObjectWriter(definition, jType, iString);
        reWriter.write(out, reWriter.calculateJsonClass(attr), attr);
    }
}
