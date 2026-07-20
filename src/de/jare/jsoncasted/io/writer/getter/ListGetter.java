/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.getter;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;

import java.util.Collection;
import java.util.Iterator;

/**
 * Getter class for extracting and analyzing list nodes in JSON structure.
 */
public class ListGetter {

    final JsonModel model;
    final JsonCastingLevel castingLevel;
    final JsonType jType;
    final JsonDebugLevel debugLevel;

    public JsonModel getModel() {
        return model;
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

    public ListGetter(JsonModel model, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        this.model = model;
        this.castingLevel = castingLevel;
        this.jType = jType;
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    public ListGetter(JsonItemDefinition definition, JsonType jType, JsonDebugLevel debugLevel) {
        this.castingLevel = definition.getCastingLevel();
        this.model = definition.getModel();
        this.jType = jType;
        this.debugLevel = debugLevel != null ? debugLevel : JsonDebugLevel.SIMPLE;
    }

    public Collection<?> extractCollection(Object ob) {
        if (ob instanceof Collection<?>) {
            return (Collection<?>) ob;
        }
        return jType.asList(ob);
    }

    public Iterator<?> iterator(Object ob) {
        Collection<?> list = extractCollection(ob);
        return list.iterator();
    }

    public boolean isPrimitive() {
        return jType != null && jType.isPrimitive();
    }

    public String toPrimitiveString(Object attr) {
        return jType.toString(attr);
    }
}
