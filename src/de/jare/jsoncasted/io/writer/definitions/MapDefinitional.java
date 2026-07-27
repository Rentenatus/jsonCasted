/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;

/**
 * Processes map nodes and catalogs all objects in DefinitionsContext. Works with JsonInstance map structures in the
 * JSON tree. Collects all objects in DefinitionsContext: candidates or findings (if definitional).
 */
@Deprecated
public class MapDefinitional extends ObjectDefinitional {

    private final JsonMap jMap;

    /**
     * Constructs a MapDefinitional with the specified parameters.
     *
     * @param definitionsContext the context for collecting candidates and findings
     * @param jMap the JSON map type
     * @param jType the JSON type used for serialization
     * @param castingLevel the casting level for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public MapDefinitional(DefinitionsContext definitionsContext, JsonMap jMap, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(definitionsContext, jType, castingLevel, debugLevel);
        this.jMap = jMap;
    }

    /**
     * Constructs a MapDefinitional from JsonItemDefinition.
     *
     * @param definition the JSON item definition
     * @param definitionsContext the context for collecting candidates and findings
     * @param jMap the JSON map type
     * @param jType the JSON type used for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public MapDefinitional(JsonItemDefinition definition, DefinitionsContext definitionsContext, JsonMap jMap, JsonType jType, JsonDebugLevel debugLevel) {
        this(definitionsContext, jMap, jType, definition.getCastingLevel(), debugLevel);
    }

    /**
     * Constructs a MapDefinitional from JsonModel for backwards compatibility.
     *
     * @param model the JSON model
     * @param definitionsContext the context for collecting candidates and findings
     * @param jMap the JSON map type
     * @param jType the JSON type used for serialization
     * @param castingLevel the casting level for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public MapDefinitional(JsonModel model, DefinitionsContext definitionsContext, JsonMap jMap, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this(definitionsContext, jMap, jType, castingLevel, debugLevel);
    }

    /**
     * Processes a JsonInstance map and all its nested values, cataloging them in the definitions context.
     *
     * @param ob the JsonInstance map to process
     */
    public void processMap(Object ob) {
        if (ob == null) {
            return;
        }

        if (!(ob instanceof JsonInstance<?> inst)) {
            return;
        }

        Iterator<String> it = inst.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Object value = inst.get(key);

            if (value != null) {
                // Skip if already processed
                if (getDefinitionsContext().isInFindings(value) || getDefinitionsContext().isInCandidates(value)) {
                    continue;
                }

                // Catalog the value
                if (getjType().isDefinitional()) {
                    getDefinitionsContext().addToFindings(value);
                } else {
                    getDefinitionsContext().addToCandidates(value);
                }

                // Process nested objects recursively
                super.process(value);
            }
        }
    }

    /**
     * Gets the JSON map type used by this MapDefinitional.
     *
     * @return the JSON map type
     */
    public JsonMap getjMap() {
        return jMap;
    }

    // getDefinitionsContext(), getjType(), getObjectGetter() are inherited from ObjectDefinitional
}
