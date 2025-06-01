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
 * The JsonFloatBuilder class handles the conversion of JSON numeric values into
 * Java primitive float types. It supports building individual values, lists,
 * and arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonFloatBuilder implements JsonModellClassBuilder {

    /**
     * Returns the primitive float class type.
     *
     * @return The float.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return float.class;
    }

    /**
     * Builds a float value from a JSON item. If the JSON value is "null" or
     * absent, it defaults to NaN (Not a Number).
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return The float representation of the JSON value.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) {
        final String value = jsonItem.getStringValue();
        return value == null || "null".equals(value) ? Float.NaN : Float.valueOf(value);
    }

    /**
     * Builds a list of float values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of float values.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Float> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Float> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(Float.parseFloat(action.getStringValue())));
        return ret;
    }

    /**
     * Builds an array of float values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return A primitive float array.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public float[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Float> list = buildList(jType, listIterator, size);
        float[] ret = new float[list.size()];
        int i = 0;
        for (Float value : list) {
            ret[i++] = value;
        }
        return ret;
    }

    /**
     * Converts a float value into its string representation.
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
     * Converts a float array into a list of Float objects.
     *
     * @param ob The float array to convert.
     * @return A list of Float values.
     */
    @Override
    public List<?> asList(Object ob) {
        float[] arr = (float[]) ob;
        List<Float> ret = new ArrayList<>(arr.length);
        for (float value : arr) {
            ret.add(value);
        }
        return ret;
    }
}
