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
import de.jare.jsoncasted.tools.SimpleStringSplitter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import de.jare.jsoncasted.model.JsonModellClassBuilder;

/**
 * The JsonStringBuilder class handles the conversion of JSON string values into
 * Java String objects. It supports building individual values, lists, and
 * arrays while ensuring proper escape character handling.
 *
 * @author Janusch Rentenatus
 */
public class JsonStringBuilder implements JsonModellClassBuilder, SimpleStringSplitter {

    /**
     * Returns the String class type.
     *
     * @return The String.class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return String.class;
    }

    /**
     * Builds a string value from a JSON item. Escape sequences such as \n, \t,
     * \r, and \" are replaced with their respective characters.
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the value.
     * @return The processed string representation of the JSON value.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) {
        return buildString(jsonItem);
    }

    /**
     * Builds a list of string values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of processed string values.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<String> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<String> ret = new ArrayList<>(size);
        listIterator.forEachRemaining(action -> ret.add(buildString(action)));
        return ret;
    }

    /**
     * Processes a JSON string by replacing escape sequences with their
     * respective characters.
     *
     * @param jsonItem The JSON item containing the string.
     * @return The processed string value.
     */
    private String buildString(JsonItem jsonItem) {
        String roh = simpleReplace(jsonItem.getStringValue(), "\\\"", "\"");
        roh = simpleReplace(roh, "\\r", "\r");
        roh = simpleReplace(roh, "\\t", "\t");
        roh = simpleReplace(roh, "\\b", "\b");
        roh = simpleReplace(roh, "\\f", "\f");
        return simpleReplace(roh, "\\n", "\n");
    }

    /**
     * Builds an array of string values from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of processed string values.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public Object[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildList(jType, listIterator, 0).toArray(new String[size]);
    }

    /**
     * Converts a string value into a properly escaped JSON string. Escape
     * sequences such as \n, \t, \r, and \" are added for JSON formatting.
     *
     * @param attr The string attribute.
     * @return The formatted string enclosed in double quotes.
     */
    @Override
    public String toString(Object attr) {
        String roh = simpleReplace(attr.toString(), "\"", "\\\"");
        roh = simpleReplace(roh, "\r", "\\r");
        roh = simpleReplace(roh, "\t", "\\t");
        roh = simpleReplace(roh, "\b", "\\b");
        roh = simpleReplace(roh, "\f", "\\f");
        return '"' + simpleReplace(roh, "\n", "\\n") + '"';
    }

    /**
     * Converts a string array into a list of String objects.
     *
     * @param ob The string array to convert.
     * @return A list of String values.
     */
    @Override
    public List<?> asList(Object ob) {
        String[] arr = (String[]) ob;
        return Arrays.asList(arr);
    }
}
