/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * The MapWriter class handles the serialization of JSON map structures. It converts HashMap<String, T> into JSON format
 * while maintaining indentation, type information, and error handling. Unlike ObjectWriter which uses reflection,
 * MapWriter directly accesses map entries using the get method.
 *
 * @author Janusch Rentenatus
 */
class MapWriter extends ObjectWriter {

    private final JsonMap jMap;

    /**
     * Constructs a MapWriter instance with default indentation.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public MapWriter(JsonModel model, JsonMap jMap, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(model, jMap, castingLevel, debugLevel);
        this.jMap = jMap;

    }

    /**
     * Constructs a MapWriter instance with a specified indentation string.
     *
     * @param castingLevel the casting level for serialization
     * @param model The JSON model.
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public MapWriter(JsonModel model, JsonMap jMap, String intentString, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(model, jMap, intentString, castingLevel, debugLevel);
        this.jMap = jMap;

    }

    /**
     * Writes a JSON object representation.
     *
     * @param out The PrintWriter for output.
     * @param jClass The JSON class defining the object's structure.
     * @param ob The object to serialize.
     */
    @Override
    public void write(final PrintWriter out, final JsonClass jClass, final Object ob) {
        if (!(ob instanceof JsonInstance<?> inst)) {
            debugLevel.warning(new ClassCastException(), () -> "Expected instance of JsonInstance<?>");
            return;
        }

        String iString = intentString + "  ";
        writeCast(jClass, out, ob, iString);

        Iterator<String> it = inst.keySet().iterator();
        boolean isFollowing = false;

        if (it.hasNext()) {
            out.println();
        }

        while (it.hasNext()) {
            final String nextName = it.next();
            Object attr = inst.get(nextName);
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
            out.print(nextName);
            out.print('"');
            out.print(": ");
            if (attr == null) {
                out.print("null");
                continue;
            }
            if (jMap.isAsListOrArray()) {
                writeList(out, jMap.getItemClass(), attr, iString);
            } else {
                writeSingle(out, jMap.getItemClass(), attr, iString);
            }
        }

        if (isFollowing) {
            out.println();
            out.print(intentString);
        }
        out.print('}');
        out.flush();
    }

}
