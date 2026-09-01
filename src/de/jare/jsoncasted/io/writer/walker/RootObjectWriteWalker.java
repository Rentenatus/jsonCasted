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
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonStringBuilder;
import de.jare.jsoncasted.model.descriptor.JsonDefinitionsDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.descriptor.def.JsonDescriptorDefinition;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A walker that writes the root object and handles the _woodDefinitions container. This walker extends
 * ObjectWriteWalker to provide specialized handling for root-level objects, including writing object definitions in the
 * _woodDefinitions section.
 *
 * @author Janusch Rentenatus
 */
public class RootObjectWriteWalker extends ObjectWriteWalker {

    private static final JsonClass JSON_CLASS_STRING = new JsonClass("String", JsonNodeType.STRING, new JsonStringBuilder());

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

    public void writeRoot(Object ob) throws NullPointerException, ClassCastException {
        strategy.writeStartFile();
        write(ob);
        strategy.writeEndFile();
    }

    @Override
    public void write(Object ob) throws NullPointerException, ClassCastException {
        if (!(ob instanceof List<?>)) {
            super.writeObjectProf(calculateJsonClass(ob), ob);
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
     * Writes the _woodDefinitions container with all definition objects. Also writes the model description as
     * _woodModel if present.
     *
     * @param iString the indentation path
     * @return true, if done
     */
    @Override
    protected boolean writeDefinitions(WriteNodePath iString, boolean isFollowing) {
        final DefinitionsContext definitionsContext = objectGetter.getDefinitionsContext();
        List<DefinitionsContextObjectRecord> records = definitionsContext.getDefinitionRecords();

        if (records.isEmpty()) {
            return false;
        }

        List<Object> objects = records.stream()
                .map(DefinitionsContextObjectRecord::getObject)
                .toList();

        // Write _woodDefinitions start
        strategy.writeAttrName(null, isFollowing, JsonTerms.TERM_WOOD_DEFINITIONS, iString);
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
            next.asAssigned();
        }
        // Write _woodDefinitions end
        strategy.writeEndArray(objects, true, true, iString);
        return true;
    }

    /**
     * Writes an individual JSON entry, handling primitive and object types. For definitions, always writes class
     * information unless casting level is NEVER.
     *
     * @param entry The object to serialize.
     * @param jsonType The JSON type of the entry.
     * @param iString The indentation string for formatted output.
     */
    protected void writeDefinitionEntry(Object entry, JsonType jsonType, WriteNodePath iString) {
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
                reWriter.writeObjectProf(jClass, entry);
            }
        }
    }

    /**
     * Writes the JsonModelDescriptor as a _woodModel subtree.
     *
     * @param iString the indentation path
     * @return true if the model descriptor was written, false otherwise
     */
    @Override
    protected boolean writeModelDescription(WriteNodePath iString, boolean isFollowing) {
        JsonModel model = objectGetter.getDefinitionsContext().getModel();
        JsonModelDescriptor descriptorObject = model.getOrCreateDescriptor();
        if (descriptorObject == null || descriptorObject.isEmpty()) {
            return false;
        }
        JsonDescriptorDefinition descriptorDefinition = JsonDescriptorDefinition.INSTANCE;
        final JsonClass jsonClass = descriptorDefinition.getDescriptModel();
        final DefinitionsContext definitionsContext = new DefinitionsContext(model, objectGetter.getDefinitionsContext().getCurrentId());

        // Write _woodModel start
        strategy.writeAttrName(null, isFollowing, JsonTerms.TERM_WOOD_MODEL, iString);
        //strategy.writeStartObject(null, descriptorObject, null, null, false, false, iString);

        final ObjectWriteWalker walker = new ObjectWriteWalker(
                strategy,
                definitionsContext,
                jsonClass,
                null,
                null,
                iString,
                objectGetter.getCastingLevel(),
                objectGetter.getDebugLevel()
        );
        walker.writeObject(jsonClass, descriptorObject);
        objectGetter.getDefinitionsContext().maxCurrentId(definitionsContext.getCurrentId());

        // Write _woodModel end
        //strategy.writeEndObject(null, descriptorObject, true, true, iString);
        return true;
    }

}
