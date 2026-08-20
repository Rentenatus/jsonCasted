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
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonIntegerObjBuilder;
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

    private static final JsonClass JSON_CLASS_INTEGER = new JsonClass("Integer", JsonNodeType.LONG, new JsonIntegerObjBuilder());

    final WriteNodePath intentPath;
    final WriteStrategy strategie;
    final ObjectGetter objectGetter;
    WoodMetadataInjection woodMetadata;
    final private JsonField parentField;
    final private Object parent;

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param strategie
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param parentField
     * @param parent
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriteWalker(WriteStrategy strategie, DefinitionsContext definitionsContext, JsonType jType, JsonField parentField, Object parent, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = new WriteNodePath("", new ArrayList<>());
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.woodMetadata = null;
        this.parentField = parentField;
        this.parent = parent;
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param parentField
     * @param parent
     * @param jType The JSON type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ObjectWriteWalker(WriteStrategy strategie, DefinitionsContext definitionsContext, JsonType jType, JsonField parentField, Object parent, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.strategie = strategie;
        this.intentPath = intentPath;
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
        this.woodMetadata = null;
        this.parentField = parentField;
        this.parent = parent;
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
    public void writeObject(JsonClass jClass, final Object ob) {
        // Skip if already processed
        if (strategie.skippProzess(jClass, ob)) {
            return;
        }

        if (intentPath.ids().contains(ob)) {
            try {
                WriteNodePath iString = intentPath.append("  ");
                writeStart(jClass, ob, iString);
                strategie.writeHasFieldKeys(jClass, ob, iString);
                strategie.writeAttrName(jClass, false, JsonTerms.TERM_CYCLE_HASHCODE, iString);
                strategie.writePrimitive(JSON_CLASS_INTEGER, ob.hashCode(), iString);
            } finally {
                strategie.writeEnd(jClass, ob, true, true, intentPath);
            }
            return;
        }

        boolean isFollowing = false;
        boolean hasFieldKeys = false;
        WriteNodePath iString = intentPath.append("  ").appendOb(ob);
        try {
            writeStart(jClass, ob, iString);
            if (WoodMetadataInjection.hasInjection(woodMetadata)) {
                woodMetadata.popWood(strategie, iString, objectGetter.getDebugLevel());
                woodMetadata = null;
            }

            hasFieldKeys = objectGetter.hasFieldKeys(jClass, ob);
            if (hasFieldKeys) {
                strategie.writeHasFieldKeys(jClass, ob, iString);
                strategie.writeAttrName(jClass, isFollowing, JsonTerms.TERM_HASHCODE, iString);
                strategie.writePrimitive(JSON_CLASS_INTEGER, ob.hashCode(), iString);
                isFollowing = true;
            }

            List< GetterFieldInfo> fieldInfos = objectGetter.extractFields(jClass, ob);
            for (GetterFieldInfo fieldInfo : fieldInfos) {
                Object attr = fieldInfo.getAttribute();
                JsonField next = fieldInfo.getJsonField();

                if (attr == null && jClass.isSkippingNulls()) {
                    continue;
                }

                strategie.writeAttrName(jClass, isFollowing, next.getfName(), iString);
                writeAttr(next, attr, ob, iString);
                isFollowing = true;
            }
        } finally {
            strategie.writeEnd(jClass, ob, isFollowing, hasFieldKeys, intentPath);
        }
    }

    void writeStart(final JsonClass jClass, final Object ob, WriteNodePath iString) {
        final boolean needsCast = objectGetter.needsCast(jClass);
        final boolean needsClassDef = !needsCast && objectGetter.needsClassDef(jClass);
        strategie.writeStart(jClass, ob, parentField, parent, needsCast, needsClassDef, iString);
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
            strategie.writeAttrNull(iString);
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
            strategie.writePrimitive(fieldType, attr, iString);
        } else if (fieldType instanceof JsonMap jMap) {
            writeMap(jMap, attr, definitionalOwner, iString);
        } else {
            writeObject(fieldType, attr, jField, definitionalOwner, iString);
        }
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
        ListWriteWalker reWriter = new ListWriteWalker(strategie, objectGetter.getDefinitionsContext(), jTypeItem, jField, definitionalOwner, iString, objectGetter.getCastingLevel(), objectGetter.getDebugLevel());
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
     * @param definitionalOwner
     * @param iString The indentation string for formatted output.
     */
    protected void writeMap(JsonMap jMap, Object attr, Object definitionalOwner, WriteNodePath iString) {
        MapWriteWalker reWriter = new MapWriteWalker(strategie, objectGetter.getDefinitionsContext(), jMap, definitionalOwner, iString, objectGetter.getCastingLevel(), objectGetter.getDebugLevel());
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
     * @param jField
     * @param definitionalOwner
     * @param iString The indentation string for formatted output.
     */
    protected void writeObject(JsonType jTypeItem, Object attr, JsonField jField, Object definitionalOwner, WriteNodePath iString) {
        ObjectWriteWalker reWriter = new ObjectWriteWalker(strategie, objectGetter.getDefinitionsContext(), jTypeItem, jField, definitionalOwner, iString, objectGetter.getCastingLevel(), objectGetter.getDebugLevel());
        if (WoodMetadataInjection.hasInjection(woodMetadata)) {
            reWriter.setWoodMetadata(woodMetadata);
        }
        reWriter.writeObject(reWriter.calculateJsonClass(attr), attr);
    }

}
