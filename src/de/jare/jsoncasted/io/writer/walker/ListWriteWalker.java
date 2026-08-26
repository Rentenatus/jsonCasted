/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.record.DefinitionsContextObjectRecord;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.getter.ListGetter;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.model.item.JsonField;

/**
 *
 * @author Janusch Renteantus
 */
public class ListWriteWalker {

    final WriteNodePath intentPath;
    final WriteStrategy strategy;
    final ListGetter listGetter;
    final private JsonField parentField;
    final private Object parent;

    /**
     * Constructs a ListWriteWalker instance.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param parentField the parent JSON field
     * @param jType The JSON type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ListWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, JsonField parentField, Object parent, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategy = strategy;
        this.parentField = parentField;
        this.parent = parent;
        this.intentPath = intentPath;
        this.listGetter = new ListGetter(definitionsContext, jType, castingLevel, debugLevel);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param ob The object to serialize.
     */
    public void writeList(final Object ob) {
        WriteNodePath iString = intentPath.append("  ");
        strategy.writeStartArray(listGetter.getjType(), ob, listGetter.isPrimitive(), iString);
        boolean isFollowing = false;
        try {
            Iterator<?> it = listGetter.iterator(ob);

            while (it.hasNext()) {
                Object next = it.next();

                // Skip if already processed
                if (strategy.skipProcess(listGetter.getjType(), next)) {
                    continue;
                }

                writeEntry(next, iString);
                if (it.hasNext()) {
                    strategy.writeArraySeparator(listGetter.isPrimitive(), iString);
                }

                isFollowing = true;
            }
        } finally {
            strategy.writeEndArray(ob, listGetter.isPrimitive(), isFollowing, iString);
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
            strategy.writeAttrNull(iString);
        } else if (listGetter.isPrimitive()) {
            strategy.writePrimitive(listGetter.getjType(), entry, iString);
        } else if (listGetter.getjType() instanceof JsonMap jMap) {
            writeMap(jMap, entry, iString);
        } else {
            writeObject(listGetter.getjType(), entry, iString);
        }
    }

    /**
     * Checks if an object should be written as a link reference instead of inline.
     *
     * @param entry the object to check
     * @return true if the object should be written as a link
     */
    protected boolean shouldWriteAsLink(Object entry) {
        if (entry == null) {
            return false;
        }
        DefinitionsContext context = listGetter.getDefinitionsContext();
        DefinitionsContextObjectRecord record = context.getRecord(entry);
        return record != null && record.isAssigned();
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jMap The JSON type of the object.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeMap(JsonMap jMap, Object attr, WriteNodePath iString) {
        MapWriteWalker reWriter = new MapWriteWalker(
                strategy,
                listGetter.getDefinitionsContext(),
                jMap,
                null,
                iString,
                listGetter.getCastingLevel(),
                listGetter.getDebugLevel()
        );
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
        ObjectWriteWalker reWriter = new ObjectWriteWalker(
                strategy,
                listGetter.getDefinitionsContext(),
                jTypeItem,
                parentField,
                parent, iString, listGetter.getCastingLevel(),
                listGetter.getDebugLevel()
        );
        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

}
