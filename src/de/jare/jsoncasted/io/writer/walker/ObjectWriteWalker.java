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
import de.jare.jsoncasted.io.writer.getter.GetterFieldInfo;
import de.jare.jsoncasted.io.writer.getter.ObjectGetter;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.List;

/**
 *
 * @author Janusch Renteantus
 */
public class ObjectWriteWalker {

    final WriteNodePath intentPath;
    final WriteStrategie strategie;
    final ObjectGetter objectGetter;
    WoodMetadataInjection woodMetadata;

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param strategie
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriteWalker(WriteStrategie strategie, DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = new WriteNodePath("");
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.woodMetadata = null;
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
    public ObjectWriteWalker(WriteStrategie strategie, DefinitionsContext definitionsContext, JsonType jType, String intentString, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = new WriteNodePath(intentString);
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.woodMetadata = null;
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
    public ObjectWriteWalker(WriteStrategie strategie, DefinitionsContext definitionsContext, JsonType jType, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = intentPath;
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.woodMetadata = null;
    }

    public void setWoodMetadata(WoodMetadataInjection woodMetadata) {
        this.woodMetadata = woodMetadata;
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
        strategie.writePath(intentPath);
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
    public void writeObject(final JsonClass jClass, final Object ob) {
        // Skip if already processed
        if (strategie.skippProzess(jClass, ob)) {
            return;
        }

        WriteNodePath iString = intentPath.append("  ");
        writeStart(jClass, ob, iString);
        if (WoodMetadataInjection.hasInjection(woodMetadata)) {
            woodMetadata.popWood(strategie, iString, objectGetter.getDebugLevel());
            woodMetadata = null;
        }

        boolean isFollowing = false;
        boolean hasFieldKeys = false;
        try {

            hasFieldKeys = objectGetter.hasFieldKeys(jClass, ob);
            if (hasFieldKeys) {
                strategie.writeHasFieldKeys(jClass, ob, iString);
            }

            List< GetterFieldInfo> fieldInfos = objectGetter.extractFields(jClass, ob);
            for (GetterFieldInfo fieldInfo : fieldInfos) {
                Object attr = fieldInfo.getAttribute();
                JsonField next = fieldInfo.getJsonField();

                if (attr == null && jClass.isSkippingNulls()) {
                    continue;
                }

                strategie.writeAttrName(jClass, isFollowing, next.getfName(), iString);
                writeAttr(next, attr, iString);
                isFollowing = true;
            }
        } finally {
            strategie.writeEnd(jClass, ob, isFollowing, hasFieldKeys, intentPath);
        }
    }

    void writeStart(final JsonClass jClass, final Object ob, WriteNodePath iString) {
        final boolean needsCast = objectGetter.needsCast(jClass);
        final boolean needsClassDef = !needsCast && objectGetter.needsClassDef(jClass);
        strategie.writeStart(jClass, ob, needsCast, needsClassDef, iString);
    }

    /**
     * Writes a JSON attribute based on its type.
     *
     * @param jField The JSON field definition.
     * @param attr The attribute value.
     * @param iString The indentation string for formatted output.
     */
    protected void writeAttr(JsonField jField, Object attr, WriteNodePath iString) {
        if (attr == null) {
            strategie.writeAttrNull(iString);
            return;
        }
        final JsonType fieldType = jField.getjType();
        if (jField.isAsListOrArray()) {
            writeList(fieldType, attr, iString);
        } else {
            writeSingle(fieldType, attr, iString);
        }
    }

    public void writeSingle(final JsonType fieldType, Object attr, WriteNodePath iString) {
        if (fieldType.isPrimitive()) {
            strategie.writePrimitive(fieldType, attr, iString);
        } else if (fieldType instanceof JsonMap jMap) {
            writeMap(jMap, attr, iString);
        } else {
            writeObject(fieldType, attr, iString);
        }
    }

    /**
     * Writes a JSON list representation.
     *
     * @param jTypeItem The JSON type of list items.
     * @param attr The list to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeList(JsonType jTypeItem, Object attr, WriteNodePath iString) {
        ListWriteWalker reWriter = new ListWriteWalker(strategie, objectGetter.getDefinitionsContext(), jTypeItem, iString, objectGetter.getCastingLevel(), objectGetter.getDebugLevel());
        if (WoodMetadataInjection.hasInjection(woodMetadata)) {
            reWriter.setWoodMetadata(woodMetadata);
        }
        reWriter.writeList(attr);
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jMap The JSON type of the object.
     * @param attr The object to serialize.
     * @param iString The indentation string for formatted output.
     */
    protected void writeMap(JsonMap jMap, Object attr, WriteNodePath iString) {
        MapWriteWalker reWriter = new MapWriteWalker(strategie, objectGetter.getDefinitionsContext(), jMap, iString, objectGetter.getCastingLevel(), objectGetter.getDebugLevel());
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
        ObjectWriteWalker reWriter = new ObjectWriteWalker(strategie, objectGetter.getDefinitionsContext(), jTypeItem, iString, objectGetter.getCastingLevel(), objectGetter.getDebugLevel());
        if (WoodMetadataInjection.hasInjection(woodMetadata)) {
            reWriter.setWoodMetadata(woodMetadata);
        }
        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

}
