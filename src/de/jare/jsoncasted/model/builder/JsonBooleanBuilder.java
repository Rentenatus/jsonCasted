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
import java.util.Iterator;
import java.util.List;
import de.jare.jsoncasted.model.JsonModellClassBuilder;

/**
 * The JsonBooleanBuilder class handles the conversion of JSON boolean values
 * into Java boolean types. It supports building individual values, lists, and
 * arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonBooleanBuilder implements JsonModellClassBuilder {

    /**
     * Returns the primitive boolean class type.
     *
     * @return The boolean.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return boolean.class;
    }

    /**
     * Builds a boolean value from a JSON item. If the JSON value is "null" or
     * absent, it defaults to false.
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return The boolean representation of the JSON value.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) throws JsonBuildException {
        final String value = jsonItem.getStringValue();
        return value == null || "null".equals(value) ? false : Boolean.valueOf(value);
    }

    /**
     * Builds a list of boolean values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of boolean values.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Boolean> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Boolean> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(Boolean.parseBoolean(action.getStringValue())));
        return ret;
    }

    /**
     * Builds an array of boolean values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return A primitive boolean array.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public boolean[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        final boolean[] ret = new boolean[size];
        int i = 0;
        while (listIterator.hasNext()) {
            ret[i++] = Boolean.parseBoolean(listIterator.next().getStringValue());
        }
        return ret;
    }

    /**
     * Converts a boolean value into its string representation.
     *
     * @param attr The boolean attribute.
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
     * Converts a boolean array into a list of Boolean objects.
     *
     * @param ob The boolean array to convert.
     * @return A list of Boolean values.
     */
    @Override
    public List<?> asCollection(Object ob) {
        boolean[] arr = (boolean[]) ob;
        List<Boolean> ret = new ArrayList<>(arr.length);
        for (boolean value : arr) {
            ret.add(value);
        }
        return ret;
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
