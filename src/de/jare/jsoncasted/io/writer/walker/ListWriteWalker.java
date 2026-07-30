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
import de.jare.jsoncasted.io.writer.getter.ListGetter;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;
import de.jare.jsoncasted.io.writer.WriteStrategy;

/**
 *
 * @author Janusch Renteantus
 */
public class ListWriteWalker {

    final WriteNodePath intentPath;
    final WriteStrategy strategie;
    final ListGetter listGetter;
    WoodMetadataInjection woodMetadata;

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
    public ListWriteWalker(WriteStrategy strategie, DefinitionsContext definitionsContext, JsonType jType, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = intentPath;
        this.listGetter = new ListGetter(definitionsContext, jType, castingLevel, debugLevel);
        this.woodMetadata = null;
    }

    public void setWoodMetadata(WoodMetadataInjection woodMetadata) {
        this.woodMetadata = woodMetadata;
    }

    /**
     * Writes a JSON object representation.
     *
     * @param ob The object to serialize.
     */
    public void writeList(final Object ob) {
        WriteNodePath iString = intentPath.append("  ");
        strategie.writeStartArray(ob, listGetter.isPrimitive(), iString);
        boolean isFollowing = false;
        try {
            Iterator<?> it = listGetter.iterator(ob);

            while (it.hasNext()) {
                Object next = it.next();

                // Skip if already processed
                if (strategie.skippProzess(listGetter.getjType(), next)) {
                    continue;
                }

                writeEntry(next, iString);
                if (it.hasNext()) {
                    strategie.writeArraySeparator(listGetter.isPrimitive(), iString);
                }

                isFollowing = true;
            }
        } finally {
            strategie.writeEndArray(ob, listGetter.isPrimitive(), isFollowing, iString);
        }
    }

    /**
     * Writes an individual JSON entry, handling primitive and object types.
     *
     * @param entry The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeEntry(Object entry, WriteNodePath iString) {
        if (entry == null) {
            strategie.writeAttrNull(iString);
        } else if (listGetter.isPrimitive()) {
            strategie.writePrimitive(listGetter.getjType(), entry, iString);
        } else if (listGetter.getjType() instanceof JsonMap jMap) {
            writeMap(jMap, entry, iString);
        } else {
            writeObject(listGetter.getjType(), entry, iString);
        }
    }

//
// Array as Item of Array 
//
//
//    /**
//     * Writes a JSON list representation.
//     *
//     * @param jTypeItem The JSON type of list items.
//     * @param attr The list to serialize.
//     * @param iString The indentation string for formatted output.
//     */
//    protected void writeList(JsonType jTypeItem, Object attr, WriteNodePath iString) {
//        ListWriteWalker reWriter = new ListWriteWalker(strategie, listGetter.getDefinitionsContext(), jTypeItem, iString, listGetter.getCastingLevel(), listGetter.getDebugLevel());
//            if (WoodMetadataInjection.hasInjection(woodMetadata)) reWriter.setWoodMetadata(woodMetadata);
//        reWriter.writeList(attr);
//    }
//
//
// Array as Item of Array 
//
//
    /**
     * Writes a JSON object representation.
     *
     * @param jMap The JSON type of the object.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeMap(JsonMap jMap, Object attr, WriteNodePath iString) {
        MapWriteWalker reWriter = new MapWriteWalker(strategie, listGetter.getDefinitionsContext(), jMap, iString, listGetter.getCastingLevel(), listGetter.getDebugLevel());
        if (WoodMetadataInjection.hasInjection(woodMetadata)) {
            reWriter.setWoodMetadata(woodMetadata);
        }
        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jTypeItem The JSON type of the object.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeObject(JsonType jTypeItem, Object attr, WriteNodePath iString) {
        ObjectWriteWalker reWriter = new ObjectWriteWalker(strategie, listGetter.getDefinitionsContext(), jTypeItem, iString, listGetter.getCastingLevel(), listGetter.getDebugLevel());
        if (WoodMetadataInjection.hasInjection(woodMetadata)) {
            reWriter.setWoodMetadata(woodMetadata);
        }

        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

}
