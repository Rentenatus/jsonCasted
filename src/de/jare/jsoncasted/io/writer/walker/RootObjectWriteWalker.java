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
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import java.util.List;

/**
 *
 * @author Janusch Renteantus
 */
public class RootObjectWriteWalker extends ObjectWriteWalker {

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param strategie
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategie strategie, DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategie, definitionsContext, jType, castingLevel, debugLevel);
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategie strategie, DefinitionsContext definitionsContext, JsonType jType, String intentString, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategie, definitionsContext, jType, intentString, castingLevel, debugLevel);
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategie strategie, DefinitionsContext definitionsContext, JsonType jType, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategie, definitionsContext, jType, intentPath, castingLevel, debugLevel);
    }

    @Override
    public void write(Object ob) throws NullPointerException, ClassCastException {
        write(ob, woodMetadata);
    }

    /**
     * Writes an root object as a JSON structure.
     *
     * @param ob The object to serialize.
     * @param woodMetadata
     * @throws NullPointerException If the object has no associated JSON class.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    protected void write(Object ob, WoodMetadataInjection woodMetadata) throws NullPointerException, ClassCastException {
        setWoodMetadata(woodMetadata);
        if (!(ob instanceof List<?>)) {
            super.write(ob);
            return;
        }
        List<?> myList = (List<?>) ob;
        if (myList.isEmpty()) {
            strategie.writePath(intentPath);
            strategie.writeStartArray(ob, true, intentPath);
            strategie.writeEndArray(ob, true, false, intentPath);
            return;
        }
        Object ob0 = myList.get(0);
        JsonClass jClass = calculateJsonClass(ob0);
        writeList(jClass, ob0, intentPath);
    }

}
