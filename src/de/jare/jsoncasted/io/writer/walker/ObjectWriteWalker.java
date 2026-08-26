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
import de.jare.jsoncasted.io.writer.getter.GetterFieldInfo;
import de.jare.jsoncasted.io.writer.getter.ObjectGetter;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.record.DefinitionsContextObjectRecord;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonLongObjBuilder;
import de.jare.jsoncasted.model.builder.JsonStringBuilder;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Janusch Renteantus
 */
public class ObjectWriteWalker {

    private static final JsonClass JSON_CLASS_LONG = new JsonClass("Long", JsonNodeType.LONG, new JsonLongObjBuilder());
    private static final JsonClass JSON_CLASS_STRING = new JsonClass("String", JsonNodeType.STRING, new JsonStringBuilder());

    final WriteNodePath intentPath;
    final WriteStrategy strategy;
    final ObjectGetter objectGetter;
    final private JsonField parentField;
    final private Object parent;

    /**
     * Constructs an ObjectWriteWalker instance with default indentation.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param parentField the parent JSON field
     * @param parent the parent object
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, JsonField parentField, Object parent, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategy = strategy;
        this.intentPath = new WriteNodePath("", new ArrayList<>());
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.parentField = parentField;
        this.parent = parent;
    }

    /**
     * Constructs an ObjectWriteWalker instance with a specified indentation string.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param parentField the parent JSON field
     * @param parent the parent object
     * @param jType The JSON type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, JsonField parentField, Object parent, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategy = strategy;
        this.intentPath = intentPath;
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.parentField = parentField;
        this.parent = parent;
    }

    /**
     * Writes an object as a JSON structure.
     *
     * @param ob The object to serialize.
     * @throws NullPointerException If the object has no associated JSON class.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    protected void write(Object ob) throws NullPointerException, ClassCastException {
        JsonClass jClass = calculateJsonClass(ob);
        strategy.writePath(intentPath);
        writeObject(jClass, ob);
    }

    /**
     * Determines the JSON class associated with an object. Delegates to the ObjectGetter.
     *
     * @param ob The object to analyze.
     * @return The corresponding JsonClass representation.
     * @throws NullPointerException If no class description is found.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    protected JsonClass calculateJsonClass(Object ob) throws NullPointerException, ClassCastException {
        return objectGetter.calculateJsonClass(ob);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jClass The JSON class defining the object's structure.
     * @param ob The object to serialize.
     */
    public void writeObject(JsonClass jClass, final Object ob) {
        // Skip if already processed
        if (strategy.skipProcess(jClass, ob)) {
            return;
        }

        // Check if object should be written as link reference (ASSIGNED objects or Circle)
        if (shouldWriteAsLink(ob) || intentPath.ids().contains(ob)) {
            writeObjectAsLink(jClass, ob);
        } else {
            writeObjectProf(jClass, ob);
        }
    }

    /**
     * Writes an object as a _woodLink reference.
     *
     * @param ob the object to write as link
     * @param jClass
     */
    protected void writeObjectAsLink(JsonClass jClass, final Object ob) {
        try {
            WriteNodePath iString = intentPath.append("  ");
            writeStart(jClass, ob, iString);
            strategy.writeHasFieldKeys(jClass, ob, iString);

            // Write _woodLink with repository key when available, otherwise write _woodObjectId
            String repoKey = objectGetter.getDefinitionsContext().getRepositoryKey(ob);
            if (repoKey != null) {
                strategy.writeAttrName(jClass, false, JsonTerms.TERM_WOOD_LINK, iString);
                strategy.writePrimitive(JSON_CLASS_STRING, repoKey, iString);
            } else {
                // Write _woodObjectId if a local ID is assigned in DefinitionsContext
                DefinitionsContextObjectRecord record = objectGetter.getDefinitionsContext().getRecord(ob);
                if (record != null) {
                    long localId = record.getLocalId();
                    if (localId >= 0) {
                        strategy.writeAttrName(jClass, false, JsonTerms.TERM_WOOD_OBJECT_ID, iString);
                        strategy.writePrimitive(JSON_CLASS_LONG, localId, iString);
                    }
                }
            }
        } finally {
            strategy.writeEnd(jClass, ob, true, true, intentPath);
        }
    }

    public void writeObjectProf(JsonClass jClass, final Object ob) {
        boolean isFollowing = false;
        boolean hasFieldKeys = false;
        WriteNodePath iString = intentPath.append("  ").appendOb(ob);
        try {
            writeStart(jClass, ob, iString);

            hasFieldKeys = hasFieldKeys(jClass, ob);
            if (hasFieldKeys) {
                strategy.writeHasFieldKeys(jClass, ob, iString);
                writeDefinitions(iString);

                // Write _woodObjectId if a local ID is assigned in DefinitionsContext
                DefinitionsContextObjectRecord record = objectGetter.getDefinitionsContext().getRecord(ob);
                if (record != null) {
                    long localId = record.getLocalId();
                    if (localId >= 0) {
                        strategy.writeAttrName(jClass, isFollowing, JsonTerms.TERM_WOOD_OBJECT_ID, iString);
                        strategy.writePrimitive(JSON_CLASS_LONG, localId, iString);
                        isFollowing = true;
                    }
                }
            }

            List< GetterFieldInfo> fieldInfos = objectGetter.extractFields(jClass, ob);
            for (GetterFieldInfo fieldInfo : fieldInfos) {
                Object attr = fieldInfo.getAttribute();
                JsonField next = fieldInfo.getJsonField();

                if (attr == null && jClass.isSkippingNulls()) {
                    continue;
                }

                strategy.writeAttrName(jClass, isFollowing, next.getfName(), iString);
                writeAttr(next, attr, ob, iString);
                isFollowing = true;
            }
        } finally {
            strategy.writeEnd(jClass, ob, isFollowing, hasFieldKeys, intentPath);
        }
    }

    public boolean hasFieldKeys(JsonClass jClass, final Object ob) {
        return objectGetter.hasFieldKeys(jClass, ob);
    }

    void writeStart(final JsonClass jClass, final Object ob, WriteNodePath iString) {
        final boolean needsCast = objectGetter.needsCast(jClass);
        final boolean needsClassDef = !needsCast && objectGetter.needsClassDef(jClass);
        strategy.writeStart(jClass, ob, parentField, parent, needsCast, needsClassDef, iString);
    }

    protected void writeDefinitions(WriteNodePath iString) {
        // NoOp, only for roots
    }

    /**
     * Writes a JSON attribute based on its type.
     *
     * @param jField The JSON field definition.
     * @param attr The attribute value.
     * @param owner
     * @param iString The indentation string for formatted output.
     */
    protected void writeAttr(JsonField jField, Object attr, Object owner, WriteNodePath iString) {
        if (attr == null) {
            strategy.writeAttrNull(iString);
            return;
        }
        final JsonType attrType = jField.getjType();
        boolean definitional = jField.getKind().isDefinitional();
        if (jField.isAsListOrArray()) {
            writeList(attrType, attr, jField, definitional ? owner : null, iString);
        } else {

            writeSingle(attrType, attr, jField, definitional ? owner : null, iString);
        }
    }

    public void writeSingle(final JsonType fieldType, Object attr, JsonField jField, Object definitionalOwner, WriteNodePath iString) {

        if (fieldType.isBoxOrPrimitive()) {
            strategy.writePrimitive(fieldType, attr, iString);
        } else if (fieldType instanceof JsonMap jMap) {
            writeMap(jMap, attr, definitionalOwner, iString);
        } else {
            writeObject(fieldType, attr, jField, definitionalOwner, iString);
        }
    }

    /**
     * Checks if an object should be written as a link reference instead of inline. An object is written as a link if it
     * is assigned in the definitions context.
     *
     * @param attr the object to check
     * @return true if the object should be written as a link
     */
    protected boolean shouldWriteAsLink(Object attr) {
        if (attr == null) {
            return false;
        }
        DefinitionsContextObjectRecord record = objectGetter.getDefinitionsContext().getRecord(attr);
        return record != null && record.isAssigned();
    }

    /**
     * Writes a JSON list representation.
     *
     * @param jTypeItem The JSON type of list items.
     * @param attr The list to serialize.
     * @param jField
     * @param definitionalOwner
     * @param iString The indentation string for formatted output.
     */
    protected void writeList(JsonType jTypeItem, Object attr, JsonField jField, Object definitionalOwner, WriteNodePath iString) {
        ListWriteWalker reWriter = new ListWriteWalker(
                strategy,
                objectGetter.getDefinitionsContext(),
                jTypeItem,
                jField,
                definitionalOwner,
                iString,
                objectGetter.getCastingLevel(),
                objectGetter.getDebugLevel()
        );
        reWriter.writeList(attr);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jMap The JSON type of the object.
     * @param attr The object to serialize.
     * @param definitionalOwner
     * @param iString The indentation string for formatted output.
     */
    protected void writeMap(JsonMap jMap, Object attr, Object definitionalOwner, WriteNodePath iString) {
        MapWriteWalker reWriter = new MapWriteWalker(strategy,
                objectGetter.getDefinitionsContext(),
                jMap, definitionalOwner,
                iString,
                objectGetter.getCastingLevel(),
                objectGetter.getDebugLevel()
        );
        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jTypeItem The JSON type of the object.
     * @param attr The object to serialize.
     * @param jField
     * @param definitionalOwner
     * @param iString The indentation string for formatted output.
     */
    protected void writeObject(JsonType jTypeItem, Object attr, JsonField jField, Object definitionalOwner, WriteNodePath iString) {
        ObjectWriteWalker reWriter = new ObjectWriteWalker(
                strategy,
                objectGetter.getDefinitionsContext(),
                jTypeItem,
                jField,
                definitionalOwner,
                iString,
                objectGetter.getCastingLevel(),
                objectGetter.getDebugLevel()
        );
        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

}
