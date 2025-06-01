/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModellClassBuilder;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JsonMapBuilder class is responsible for converting JSON maps into
 * structured Java objects. It supports building single instances, lists, and
 * arrays while handling instantiation errors gracefully.
 *
 * @author Janusch Rentenatus
 */
public class JsonMapBuilder implements JsonModellClassBuilder {

    private final Class<? extends JsonInstance> singular;
    private final JsonClass itemClass;

    /**
     * Constructs a JsonMapBuilder instance with the specified class type and
     * item class.
     *
     * @param singular The target JsonInstance class type.
     * @param itemClass The JSON class describing the map structure.
     */
    public JsonMapBuilder(Class<? extends JsonInstance> singular, JsonClass itemClass) {
        this.singular = singular;
        this.itemClass = itemClass;
    }

    /**
     * Builds a JsonInstance from a JSON map.
     *
     * @param aThis The JSON map structure.
     * @param jsonItem The JSON item containing the mapped values.
     * @return A constructed JsonInstance containing mapped properties.
     * @throws JsonBuildException If instance creation fails.
     */
    public JsonInstance buildMap(JsonMap aThis, JsonItem jsonItem) throws JsonBuildException {
        JsonInstance ret = createInstance();
        for (String para : jsonItem.getParamSet()) {
            JsonItem item = jsonItem.getParam(para);
            ret.putObject(para, item.buildInstance());
        }
        return ret;
    }

    /**
     * Creates a new instance of JsonInstance.
     *
     * @return A new JsonInstance.
     * @throws JsonBuildException If instantiation fails due to reflection
     * errors.
     */
    protected JsonInstance createInstance() throws JsonBuildException {
        Object ob = null;
        try {
            ob = singular.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            throw new JsonBuildException(ex.getMessage(), ex);
        }
        try {
            return (JsonInstance) ob;
        } catch (ClassCastException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            throw new JsonBuildException(ex.getMessage(), ex);
        }
    }

    /**
     * Builds a list of JsonInstance objects from a JSON map array.
     *
     * @param aThis The JSON map structure.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of JsonInstance objects.
     * @throws JsonBuildException If instance creation fails.
     */
    public List<JsonInstance> buildMapList(JsonMap aThis, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        List<JsonInstance> ret = new ArrayList<>(size);
        while (listIterator.hasNext()) {
            JsonItem next = listIterator.next();
            ret.add(buildMap(aThis, next));
        }
        return ret;
    }

    /**
     * Builds an array of JsonInstance objects from a JSON map array.
     *
     * @param aThis The JSON map structure.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of JsonInstance objects.
     * @throws JsonBuildException If instance creation fails.
     */
    public JsonInstance[] buildMapArray(JsonMap aThis, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        JsonInstance[] ret = new JsonInstance[size];
        int index = 0;
        while (listIterator.hasNext()) {
            JsonItem next = listIterator.next();
            ret[index] = buildMap(aThis, next);
            index++;
        }
        return ret;
    }

    /**
     * Not supported for this builder.
     *
     * @param ob Object
     * @return none
     * @throws UnsupportedOperationException
     */
    @Override
    public List<?> asList(Object ob) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Not supported for this builder.
     *
     * @param ob Object
     * @return none
     * @throws UnsupportedOperationException
     */
    @Override
    public Class<?> getSingularClass() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Builds a JsonInstance from a JSON map.
     *
     * @param aThis The JSON map structure.
     * @param jsonItem The JSON item containing the mapped values.
     * @return A constructed JsonInstance containing mapped properties.
     * @throws JsonBuildException If instance creation fails.
     */
    @Override
    public Object build(JsonClass aThis, JsonItem jsonItem) throws JsonBuildException {
        return buildMap((JsonMap) aThis, jsonItem);
    }

    /**
     * Builds a list of JsonInstance objects from a JSON map array.
     *
     * @param aThis The JSON map structure.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of JsonInstance objects.
     * @throws JsonBuildException If instance creation fails.
     */
    @Override
    public Object buildList(JsonType aThis, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildMapList((JsonMap) aThis, listIterator, size);
    }

    /**
     * Builds an array of JsonInstance objects from a JSON map array.
     *
     * @param aThis The JSON map structure.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of JsonInstance objects.
     * @throws JsonBuildException If instance creation fails.
     */
    @Override
    public Object buildArray(JsonType aThis, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildMapArray((JsonMap) aThis, listIterator, size);
    }

    /**
     * Not supported for this builder.
     *
     * @param attr Object
     * @return none
     * @throws UnsupportedOperationException
     */
    @Override
    public String toString(Object attr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
