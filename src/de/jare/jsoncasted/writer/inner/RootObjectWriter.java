/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.writer.inner;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 * The RootObjectWriter class extends ObjectWriter and handles the serialization
 * of JSON root-level structures, including objects and arrays. It ensures
 * proper formatting when serializing lists or single objects.
 *
 * @author Janusch Rentenatus
 */
public class RootObjectWriter extends ObjectWriter {

    /**
     * Constructs a RootObjectWriter instance with default indentation.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     */
    public RootObjectWriter(JsonItemDefinition definition, JsonType jType) {
        super(definition, jType);
    }

    /**
     * Constructs a RootObjectWriter instance with a specified indentation
     * string.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     */
    public RootObjectWriter(JsonItemDefinition definition, JsonType jType, String intentString) {
        super(definition, jType, intentString);
    }

    /**
     * Writes an object or list as a JSON structure. If the provided object is a
     * list, it serializes it as a JSON array.
     *
     * @param out The PrintWriter to write the JSON output.
     * @param ob The object or list to serialize.
     */
    @Override
    public void write(PrintWriter out, Object ob) {
        if (!(ob instanceof List<?>)) {
            super.write(out, ob);
            return;
        }

        List<?> myList = (List<?>) ob;
        if (myList.isEmpty()) {
            out.print(intentString);
            out.print("[]");
            return;
        }

        Object ob0 = myList.get(0);
        JsonClass jClass = calculateJsonClass(ob0);
        out.print("[\n");
        Iterator<?> it = myList.iterator();
        out.print(intentString);
        super.write(out, jClass, it.next());

        while (it.hasNext()) {
            out.println(",");
            out.print(intentString);
            super.write(out, jClass, it.next());
        }

        out.print("\n]");
    }
}
