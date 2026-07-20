/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.writer.getter.GetterFieldInfo;
import de.jare.jsoncasted.io.writer.getter.ObjectGetter;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;

import java.util.List;

/**
 * Processes object nodes and applies definition rules to their children.
 * Works with object/structural nodes in the JSON tree.
 * Collects all objects in DefinitionsContext: candidates or findings (if definitional).
 */
public class ObjectDefinitional {

    private final ObjectGetter objectGetter;
    private final DefinitionsContext definitionsContext;
    private final JsonType jType;

    /**
     * Constructs an ObjectDefinitional with the specified parameters.
     *
     * @param definitionsContext the context for collecting candidates and findings
     * @param jType the JSON type used for serialization
     * @param castingLevel the casting level for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ObjectDefinitional(DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.definitionsContext = definitionsContext;
        this.jType = jType;
        this.objectGetter = new ObjectGetter(definitionsContext, castingLevel, jType, debugLevel);
    }

    /**
     * Constructs an ObjectDefinitional from JsonItemDefinition.
     *
     * @param definition the JSON item definition
     * @param definitionsContext the context for collecting candidates and findings
     * @param jType the JSON type used for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ObjectDefinitional(JsonItemDefinition definition, DefinitionsContext definitionsContext, JsonType jType, JsonDebugLevel debugLevel) {
        this(definitionsContext, jType, definition.getCastingLevel(), debugLevel);
    }

    /**
     * Constructs an ObjectDefinitional from JsonModel for backwards compatibility.
     *
     * @param model the JSON model
     * @param definitionsContext the context for collecting candidates and findings
     * @param jType the JSON type used for serialization
     * @param castingLevel the casting level for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ObjectDefinitional(JsonModel model, DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this(definitionsContext, jType, castingLevel, debugLevel);
    }

    /**
     * Processes an object and all its nested objects, cataloging them in the definitions context.
     *
     * @param ob the object to process
     */
    public void process(Object ob) {
        if (ob == null) {
            return;
        }

        // Skip if already processed
        if (definitionsContext.isInFindings(ob) || definitionsContext.isInCandidates(ob)) {
            return;
        }

        // Calculate the JSON class for this object
        JsonClass jClass = objectGetter.calculateJsonClass(ob);
        if (jClass == null) {
            return;
        }

        // Catalog the object
        if (jType.isDefinitional()) {
            definitionsContext.addToFindings(ob);
        } else {
            definitionsContext.addToCandidates(ob);
        }

        // Process all fields recursively
        List<GetterFieldInfo> fieldInfos = objectGetter.extractFields(jClass, ob);
        for (GetterFieldInfo fieldInfo : fieldInfos) {
            Object attr = fieldInfo.getAttribute();
            if (attr != null) {
                processField(fieldInfo, attr);
            }
        }
    }

    /**
     * Processes a single field, dispatching to the appropriate method based on field type.
     *
     * @param fieldInfo the field information
     * @param attr the attribute value to process
     */
    protected void processField(GetterFieldInfo fieldInfo, Object attr) {
        JsonType fieldType = fieldInfo.getJsonField().getjType();
        
        if (fieldType.isPrimitive()) {
            // Skip primitive fields
            return;
        }
        
        if (fieldInfo.getJsonField().isAsListOrArray()) {
            processListValue(fieldInfo, attr);
        } else if (fieldType instanceof JsonMap jMap) {
            processMapValue(fieldInfo, attr, jMap);
        } else {
            processSingleValue(fieldInfo, attr);
        }
    }

    /**
     * Processes a single non-collection, non-map value.
     *
     * @param fieldInfo the field information
     * @param attr the attribute value to process
     */
    protected void processSingleValue(GetterFieldInfo fieldInfo, Object attr) {
        process(attr);
    }

    /**
     * Processes a list/array value.
     *
     * @param fieldInfo the field information
     * @param attr the attribute value to process
     */
    protected void processListValue(GetterFieldInfo fieldInfo, Object attr) {
        JsonType fieldType = fieldInfo.getJsonField().getjType();
        ListDefinitional listDefinitional = new ListDefinitional(
            definitionsContext,
            fieldType,
            objectGetter.getCastingLevel(),
            objectGetter.getDebugLevel()
        );
        listDefinitional.process(attr);
    }

    /**
     * Processes a map value.
     *
     * @param fieldInfo the field information
     * @param attr the attribute value to process
     * @param jMap the JSON map type
     */
    protected void processMapValue(GetterFieldInfo fieldInfo, Object attr, JsonMap jMap) {
        JsonType fieldType = fieldInfo.getJsonField().getjType();
        MapDefinitional mapDefinitional = new MapDefinitional(
            definitionsContext,
            jMap,
            fieldType,
            objectGetter.getCastingLevel(),
            objectGetter.getDebugLevel()
        );
        mapDefinitional.process(attr);
    }
    
    

    /**
     * Gets the definitions context used by this ObjectDefinitional.
     *
     * @return the definitions context
     */
    public DefinitionsContext getDefinitionsContext() {
        return definitionsContext;
    }

    /**
     * Gets the JSON type used by this ObjectDefinitional.
     *
     * @return the JSON type
     */
    public JsonType getjType() {
        return jType;
    }

    /**
     * Gets the object getter used by this ObjectDefinitional.
     *
     * @return the object getter
     */
    public ObjectGetter getObjectGetter() {
        return objectGetter;
    }

}
