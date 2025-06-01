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
 * The JsonIntBuilder class handles the conversion of JSON numeric values into
 * Java primitive int types. It supports building individual values, lists, and
 * arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonIntBuilder implements JsonModellClassBuilder {

    /**
     * Returns the primitive integer class type.
     *
     * @return The int.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return int.class;
    }

    /**
     * Builds an integer value from a JSON item. If the JSON value is "null" or
     * absent, it defaults to 0.
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return The integer representation of the JSON value.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) {
        final String value = jsonItem.getStringValue();
        return value == null || "null".equals(value) ? 0 : Integer.valueOf(value);
    }

    /**
     * Builds a list of integer values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of integer values.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Integer> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Integer> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(Integer.parseInt(action.getStringValue())));
        return ret;
    }

    /**
     * Builds an array of integer values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return A primitive integer array.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public int[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Integer> list = buildList(jType, listIterator, 0);
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer value : list) {
            ret[i++] = value;
        }
        return ret;
    }

    /**
     * Converts an integer value into its string representation.
     *
     * @param attr The numeric attribute.
     * @return The string representation of the number, or "null" if undefined.
     */
    @Override
    public String toString(Object attr) {
        if (attr instanceof Number) {
            return attr.toString();
        } else {
            return "null";
        }
    }

    /**
     * Converts an integer array into a list of Integer objects.
     *
     * @param ob The integer array to convert.
     * @return A list of Integer values.
     */
    @Override
    public List<?> asList(Object ob) {
        int[] arr = (int[]) ob;
        List<Integer> ret = new ArrayList<>(arr.length);
        for (int value : arr) {
            ret.add(value);
        }
        return ret;
    }
}
