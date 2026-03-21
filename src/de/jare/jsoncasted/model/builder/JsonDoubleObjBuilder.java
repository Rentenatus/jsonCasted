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
 * The JsonDoubleObjBuilder class handles the conversion of JSON numeric values
 * into Java Double objects. It supports building individual values, lists, and
 * arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonDoubleObjBuilder implements JsonModellClassBuilder {

    /**
     * Returns the Double wrapper class type.
     *
     * @return The Double.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return Double.class;
    }

    /**
     * Builds a Double object from a JSON item. If the JSON value is "null" or
     * absent, it returns null instead of a numeric value.
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return A Double object representing the JSON value, or null if
     * undefined.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) {
        final String value = jsonItem.getStringValue();
        return value == null || "null".equals(value) ? null : Double.valueOf(value);
    }

    /**
     * Builds a list of Double values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of Double objects.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Double> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Double> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(Double.parseDouble(action.getStringValue())));
        return ret;
    }

    /**
     * Builds an array of Double objects from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of Double objects.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public Object[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildList(jType, listIterator, size).toArray(new Double[size]);
    }

    /**
     * Converts a Double object into its string representation.
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
     * Converts a Double array into a list of Double objects.
     *
     * @param ob The Double array to convert.
     * @return A list of Double values.
     */
    @Override
    public List<?> asCollection(Object ob) {
        Double[] arr = (Double[]) ob;
        return Arrays.asList(arr);
    }
}
