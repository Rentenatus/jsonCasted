/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.getter;

import de.jare.debug.DebugTuple;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Getter class for extracting and analyzing object nodes in JSON structure. Provides methods to access class
 * information, type metadata, and child nodes.
 */
public class ObjectGetter {

    final DefinitionsContext definitionsContext;
    final JsonCastingLevel castingLevel;
    final JsonType jType;
    final JsonDebugLevel debugLevel;

    public JsonModel getModel() {
        return definitionsContext.getModel();
    }

    public DefinitionsContext getDefinitionsContext() {
        return definitionsContext;
    }

    public JsonCastingLevel getCastingLevel() {
        return castingLevel;
    }

    public JsonType getjType() {
        return jType;
    }

    public JsonDebugLevel getDebugLevel() {
        return debugLevel;
    }

    /**
     * Constructs an ObjectGetter with the specified parameters.
     *
     * @param definitionsContext the definitions context containing model and wood IDs
     * @param castingLevel the casting level for serialization
     * @param jType the JSON type used for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ObjectGetter(DefinitionsContext definitionsContext, JsonCastingLevel castingLevel, JsonType jType, JsonDebugLevel debugLevel) {
        this.definitionsContext = definitionsContext;
        this.castingLevel = castingLevel;
        this.jType = jType;
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    /**
     * Extracts field information from an object for writing.
     *
     * @param jClass The JSON class defining the object's structure
     * @param ob The object to extract fields from
     * @return A list of FieldInfo objects containing field name, JsonField, and attribute value
     */
    public List<GetterFieldInfo> extractFields(JsonClass jClass, Object ob) {
        List<GetterFieldInfo> fieldInfos = new ArrayList<>();

        if (jClass == null || ob == null) {
            return fieldInfos;
        }

        Iterator<String> it = jClass.keysForWriteIterator(ob);
        while (it.hasNext()) {
            String fieldName = it.next();
            JsonField next = jClass.getField(fieldName);
            Object attr = jClass.getAttr(next, ob, debugLevel);
            fieldInfos.add(new GetterFieldInfo(fieldName, next, attr));
        }

        return fieldInfos;
    }

    /**
     * Checks if the object has field keys.
     *
     * @param jClass The JSON class
     * @param ob The object to check
     * @return true if the object has field keys
     */
    public boolean hasFieldKeys(JsonClass jClass, Object ob) {
        return jClass != null && ob != null && jClass.hasFieldKeys(ob);
    }

    /**
     * Checks if a cast is needed for the given JSON class at the current casting level.
     *
     * @param jClass The JSON class to check
     * @return true if a cast is needed
     */
    public boolean needsCast(JsonClass jClass) {
        return (jType != null && jType.needCast(castingLevel)) || jClass.needCast(castingLevel);
    }

    /**
     * Checks if a class definition is needed for the given JSON class at the current casting level.
     *
     * @param jClass The JSON class to check
     * @return true if a class definition is needed
     */
    public boolean needsClassDef(JsonClass jClass) {
        return (jType != null && jType.needClassDef(castingLevel)) || jClass.needClassDef(castingLevel);
    }

    /**
     * Determines the JSON class associated with an object.
     *
     * @param ob The object to analyze.
     * @return The corresponding JsonClass representation.
     * @throws NullPointerException If no class description is found.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    public JsonClass calculateJsonClass(Object ob) throws NullPointerException, ClassCastException {
        if (jType instanceof JsonMap jMap) {
            JsonClass keyClass = jMap.getItemClass();
            return keyClass;
        }
        JsonClass jClass = jType == null ? null : jType.getDirectClass();
        if (jClass == null) {
            jClass = definitionsContext.getModel().getJsonClass(ob.getClass());
        }
        if (jClass == null) {
            final String msg = "No description found for " + ob.getClass().getTypeName() + ".";
            final NullPointerException ex = new NullPointerException(msg);
            debugLevel.warning(() -> new DebugTuple(msg, (Object[]) null));
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            throw ex;
        }
        if (jType != null && !jType.contains(jClass) && !jClass.isSubOf(jType)) {
            final String msg = "Item has the class '" + jClass.getcName()
                    + "', but the root should have been '" + jType.getcName() + "'.";
            final ClassCastException ex = new ClassCastException(msg);
            debugLevel.warning(() -> new DebugTuple(msg, (Object[]) null));
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            throw ex;
        }
        return jClass;
    }

    /**
     * Print warning with throwable, if satisfied.
     *
     * @param thrown the throwable to log
     * @param msgSupplier A function, which when called, produces the desired log message
     */
    public void warning(Throwable thrown, Supplier<String> msgSupplier) {
        getDebugLevel().warning(thrown, msgSupplier);
    }
}
