/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.record.DefinitionsContextObjectRecord;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;
import java.util.List;

/**
 * A walker that writes the root object and handles the _woodDefinitions container.
 * This walker extends ObjectWriteWalker to provide specialized handling for root-level
 * objects, including writing object definitions in the _woodDefinitions section.
 *
 * @author Janusch Rentenatus
 */
public class RootObjectWriteWalker extends ObjectWriteWalker {

    /**
     * Constructs a RootObjectWriteWalker instance with default indentation.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategy, definitionsContext, jType, null, null, castingLevel, debugLevel);
    }

    /**
     * Constructs a RootObjectWriteWalker instance with a specified indentation string.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategy, definitionsContext, jType, null, null, intentPath, castingLevel, debugLevel);
    }

    @Override
    public void write(Object ob) throws NullPointerException, ClassCastException {
        if (!(ob instanceof List<?>)) {
            super.write(ob);
            return;
        }
        List<?> myList = (List<?>) ob;
        if (myList.isEmpty()) {
            strategy.writePath(intentPath);
            strategy.writeStartArray(null, ob, true, intentPath);
            strategy.writeEndArray(ob, true, false, intentPath);
            return;
        }
        Object ob0 = myList.get(0);
        JsonClass jClass = calculateJsonClass(ob0);
        writeList(jClass, ob0, null, null, intentPath);
    }

    @Override
    public boolean hasFieldKeys(JsonClass jClass, final Object ob) {
        final DefinitionsContext definitionsContext = this.objectGetter.getDefinitionsContext();
        return objectGetter.hasFieldKeys(jClass, ob)
                || definitionsContext.hasDefinitions();
    }

    /**
     * Writes the _woodDefinitions container with all definition objects.
     *
     * @param iString the indentation path
     */
    @Override
    protected void writeDefinitions(WriteNodePath iString) {
        final DefinitionsContext definitionsContext = objectGetter.getDefinitionsContext();
        List<DefinitionsContextObjectRecord> records = definitionsContext.getDefinitionRecords();
        if (records.isEmpty()) {
            return;
        }
        List<Object> objects = records.stream()
                .map(DefinitionsContextObjectRecord::getObject)
                .toList();

        // Write _woodDefinitions start
        strategy.writeAttrName(null, false, JsonTerms.TERM_WOOD_DEFINITIONS, iString);
        strategy.writeStartArray(null, objects, false, iString);

        WriteNodePath entryIndent = iString.append("  ");

        Iterator<DefinitionsContextObjectRecord> it = records.iterator();
        while (it.hasNext()) {
            DefinitionsContextObjectRecord next = it.next();
            Object ob = next.getObject();

            writeDefinitionEntry(ob, next.getJsonType(), entryIndent);
            if (it.hasNext()) {
                strategy.writeArraySeparator(false, entryIndent);
            }
        }
        // Write _woodDefinitions end
        strategy.writeEndArray(objects, true, true, iString);

    }

    /**
     * Writes an individual JSON entry, handling primitive and object types.
     * For definitions, always writes class information unless casting level is NEVER.
     *
     * @param entry The object to serialize.
     * @param jsonType The JSON type of the entry.
     * @param iString The indentation string for formatted output.
     */
    protected void writeDefinitionEntry(Object entry, JsonType jsonType, WriteNodePath iString) {
        // Check if this object should be written as a link reference
        if (shouldWriteAsLink(entry)) {
            writeAsLink(entry, iString);
            return;
        }

        if (entry == null) {
            strategy.writeAttrNull(iString);
        } else {
            // For definitions, ensure class information is always written unless casting is NEVER
            JsonCastingLevel effectiveCastingLevel = objectGetter.getCastingLevel();
            if (effectiveCastingLevel != JsonCastingLevel.NEVER) {
                effectiveCastingLevel = JsonCastingLevel.ALWAYS_CLASS_DEF;
            }

            if (jsonType instanceof JsonMap jMap) {
                MapWriteWalker mapWriter = new MapWriteWalker(
                        strategy,
                        objectGetter.getDefinitionsContext(),
                        jMap, null,
                        iString,
                        effectiveCastingLevel,
                        objectGetter.getDebugLevel()
                );
                mapWriter.writeObject(mapWriter.calculateJsonClass(entry), entry);
            } else if (jsonType instanceof JsonClass jClass) {
                ObjectWriteWalker reWriter = new ObjectWriteWalker(strategy, objectGetter.getDefinitionsContext(), jsonType, null, null, iString, effectiveCastingLevel, objectGetter.getDebugLevel());
                reWriter.writeObject(jClass, entry);
            }
        }
    }
}
