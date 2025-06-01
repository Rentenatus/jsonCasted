/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.JsonType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import de.jare.jsoncasted.model.JsonModellClassBuilder;

/**
 * The JsonBooleanObjBuilder class handles the conversion of JSON boolean values
 * into Java Boolean objects. It supports building individual values, lists, and
 * arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonBooleanObjBuilder implements JsonModellClassBuilder {

    /**
     * Returns the Boolean wrapper class type.
     *
     * @return The Boolean.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return Boolean.class;
    }

    /**
     * Builds a Boolean object from a JSON item. If the JSON value is "null" or
     * absent, it returns null instead of a primitive value.
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return A Boolean object representing the JSON value, or null if
     * undefined.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) {
        final String value = jsonItem.getStringValue();
        return value == null || "null".equals(value) ? null : Boolean.valueOf(value);
    }

    /**
     * Builds a list of Boolean values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of Boolean objects.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Boolean> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Boolean> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(Boolean.parseBoolean(action.getStringValue())));
        return ret;
    }

    /**
     * Builds an array of Boolean objects from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of Boolean objects.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public Object[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildList(jType, listIterator, size).toArray(new Boolean[size]);
    }

    /**
     * Converts a Boolean object into its string representation.
     *
     * @param attr The Boolean attribute.
     * @return "true", "false", or "null" if the value is undefined.
     */
    @Override
    public String toString(Object attr) {
        if (Boolean.TRUE.equals(attr)) {
            return Boolean.TRUE.toString();
        } else if (Boolean.FALSE.equals(attr)) {
            return Boolean.FALSE.toString();
        } else {
            return "null";
        }
    }

    /**
     * Converts a Boolean array into a list of Boolean objects.
     *
     * @param ob The Boolean array to convert.
     * @return A list of Boolean values.
     */
    @Override
    public List<?> asList(Object ob) {
        Boolean[] arr = (Boolean[]) ob;
        return Arrays.asList(arr);
    }

    /**
     * Provides the standard getter prefix for boolean properties ("is").
     *
     * @return The getter prefix "is".
     */
    @Override
    public String getterPre() {
        return "is";
    }
}
