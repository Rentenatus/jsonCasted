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
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JsonEnumByNameBuilder class handles the conversion of JSON string values
 * into Java enumerations by using a lookup method (getByName). It supports
 * building individual enum instances, lists, and arrays.
 *
 * @author Janusch Rentenatus
 */
public class JsonEnumByNameBuilder implements JsonModellClassBuilder, SimpleStringSplitter {

    private final Class<?> enumClazz;
    private java.lang.reflect.Method getByNameMethod;

    /**
     * Constructs a JsonEnumByNameBuilder instance using the specified enum
     * class.
     *
     * @param enumClazz The enumeration class to use for conversion.
     */
    public JsonEnumByNameBuilder(Class<?> enumClazz) {
        this.enumClazz = enumClazz;
        this.getByNameMethod = null;
    }

    /**
     * Returns the enumeration class type.
     *
     * @return The Class<?> representing the enum.
     */
    @Override
    public Class<?> getSingularClass() {
        return enumClazz;
    }

    /**
     * Builds an enum instance from a JSON item.
     *
     * @param jClass The JSON class.
     * @param jsonItem The JSON item containing the enum name.
     * @return The enum instance corresponding to the JSON string.
     * @throws JsonBuildException If the enum cannot be retrieved.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) throws JsonBuildException {
        return buildFromString(jsonItem);
    }

    /**
     * Builds a list of enum instances from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the list.
     * @return A list of enum instances.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public ArrayList<Object> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList<Object> list = new ArrayList<>(size);
        while (listIterator.hasNext()) {
            JsonItem next = listIterator.next();
            list.add(buildFromString(next));
        }
        return list;
    }

    /**
     * Converts a JSON string value into an enum instance using the getByName
     * getByNameMethod.
     *
     * @param jsonItem The JSON item containing the enum name.
     * @return The corresponding enum instance, or null if the lookup fails.
     * @throws JsonBuildException If the enum class does not contain
     * getByName(String).
     */
    private Object buildFromString(JsonItem jsonItem) throws JsonBuildException {
        if (enumClazz == null) {
            return null;
        }
        String rawValue = simpleReplace(jsonItem.getStringValue(), "\\\"", "\"");
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        try {
            if (getByNameMethod == null) {
                getByNameMethod = enumClazz.getMethod("getByName", String.class);
            }
            final Object enumObject = getByNameMethod.invoke(null, rawValue);
            if (enumObject == null) {
                final String msg = enumClazz.getSimpleName() + "." + rawValue + " not found.";
                Logger.getGlobal().log(Level.WARNING, msg, new NullPointerException(msg));
            }
            return enumObject;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new JsonBuildException("Method getByName(String) in Enum " + enumClazz.getSimpleName() + " not found.");
        }
    }

    /**
     * Builds an array of enum instances from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of enum instances.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public Object[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildList(jType, listIterator, size).toArray();
    }

    /**
     * Converts an enum instance into a properly formatted JSON string.
     *
     * @param attr The enum instance.
     * @return The JSON-compatible string representation.
     */
    @Override
    public String toString(Object attr) {
        return '"' + attr.toString() + '"';
    }

    /**
     * Converts an array of enum instances into a list.
     *
     * @param ob The enum array to convert.
     * @return A list of enum instances.
     */
    @Override
    public List<?> asCollection(Object ob) {
        Object[] arr = (Object[]) ob;
        return Arrays.asList(arr);
    }
}
