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
 * The JsonDoubleBuilder class handles the conversion of JSON numeric values
 * into Java primitive double types. It supports building individual values,
 * lists, and arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonDoubleBuilder implements JsonModellClassBuilder {

    /**
     * Returns the primitive double class type.
     *
     * @return The double.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return double.class;
    }

    /**
     * Builds a double value from a JSON item. If the JSON value is "null" or
     * absent, it defaults to NaN (Not a Number).
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return The double representation of the JSON value.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) {
        final String value = jsonItem.getStringValue();
        return value == null || "null".equals(value) ? Double.NaN : Double.valueOf(value);
    }

    /**
     * Builds a list of double values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of double values.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Double> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Double> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(Double.parseDouble(action.getStringValue())));
        return ret;
    }

    /**
     * Builds an array of double values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return A primitive double array.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public double[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Double> list = buildList(jType, listIterator, size);
        double[] ret = new double[list.size()];
        int i = 0;
        for (Double value : list) {
            ret[i++] = value;
        }
        return ret;
    }

    /**
     * Converts a double value into its string representation.
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
     * Converts a double array into a list of Double objects.
     *
     * @param ob The double array to convert.
     * @return A list of Double values.
     */
    @Override
    public List<?> asList(Object ob) {
        double[] arr = (double[]) ob;
        List<Double> ret = new ArrayList<>(arr.length);
        for (double value : arr) {
            ret.add(value);
        }
        return ret;
    }
}
