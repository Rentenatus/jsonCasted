/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.writer.getter.ListGetter;
import de.jare.jsoncasted.io.writer.getter.ObjectGetter;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;

import java.util.Collection;
import java.util.Iterator;

/**
 * Processes list/array nodes and catalogs all objects in DefinitionsContext. Works with collections and arrays in the
 * JSON tree. Collects all objects in DefinitionsContext: candidates or findings (if definitional).
 */
public class ListDefinitional extends ObjectDefinitional {

    private final ListGetter listGetter;

    /**
     * Constructs a ListDefinitional with the specified parameters.
     *
     * @param definitionsContext the context for collecting candidates and findings
     * @param jType the JSON type used for serialization
     * @param castingLevel the casting level for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ListDefinitional(DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(definitionsContext, jType, castingLevel, debugLevel);
        this.listGetter = new ListGetter(definitionsContext, jType, castingLevel, debugLevel);
    }

    /**
     * Constructs a ListDefinitional from JsonItemDefinition.
     *
     * @param definition the JSON item definition
     * @param definitionsContext the context for collecting candidates and findings
     * @param jType the JSON type used for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ListDefinitional(JsonItemDefinition definition, DefinitionsContext definitionsContext, JsonType jType, JsonDebugLevel debugLevel) {
        this(definitionsContext, jType, definition.getCastingLevel(), debugLevel);
    }

    /**
     * Constructs a ListDefinitional from JsonModel for backwards compatibility.
     *
     * @param model the JSON model
     * @param definitionsContext the context for collecting candidates and findings
     * @param jType the JSON type used for serialization
     * @param castingLevel the casting level for serialization
     * @param debugLevel the debug level for controlling debug output
     */
    public ListDefinitional(JsonModel model, DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this(definitionsContext, jType, castingLevel, debugLevel);
    }

    /**
     * Processes a collection/array and all its nested objects, cataloging them in the definitions context.
     *
     * @param ob the collection or array to process
     */
    public void processList(Object ob) {
        if (ob == null) {
            return;
        }

        // Get the collection
        Collection<?> collection = listGetter.extractCollection(ob);
        if (collection == null) {
            return;
        }

        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            Object element = it.next();
            if (element != null) {
                // Skip if already processed
                if (getDefinitionsContext().isInFindings(element) || getDefinitionsContext().isInCandidates(element)) {
                    continue;
                }

                // Catalog the element
                if (getjType().isDefinitional()) {
                    getDefinitionsContext().addToFindings(element);
                } else {
                    getDefinitionsContext().addToCandidates(element);
                }

                // Process nested objects recursively
                super.process(element);
            }
        }
    }

    /**
     * Gets the list getter used by this ListDefinitional.
     *
     * @return the list getter
     */
    public ListGetter getListGetter() {
        return listGetter;
    }

    // getDefinitionsContext(), getjType(), getObjectGetter() are inherited from ObjectDefinitional
}
