/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.BuilderService;
import de.jare.jsoncasted.model.item.JsonClass;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Interface for builders that create Java objects from JSON structures.
 *
 * <p>Implementations of this interface define how to instantiate and populate
 * Java objects from JSON data. Each builder handles a specific type of JSON
 * value (primitives, objects, arrays, lists, etc.).</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Building single objects from JSON items</li>
 *   <li>Building lists from JSON arrays</li>
 *   <li>Building arrays from JSON arrays</li>
 *   <li>Providing type information (singular class, primitives)</li>
 *   <li>Converting values to/from strings and collections</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public interface JsonModellClassBuilder {

    // public static final String NOT_LIST = "This Class is not a list.";

    /**
     * Returns the singular Java class that this builder creates.
     *
     * @return the class type, or {@code null} if not applicable.
     */
    public Class<?> getSingularClass();

    /**
     * Builds a single object from a JSON item.
     *
     * @param jClass the JsonClass defining the target type.
     * @param jsonItem the JSON item to build from.
     * @param builderService the builder service for nested object construction.
     * @return the constructed object.
     * @throws JsonBuildException if object construction fails.
     */
    public Object build(JsonClass jClass, JsonItem jsonItem, BuilderService builderService) throws JsonBuildException;

    /**
     * Builds a List from a JSON array.
     *
     * @param jType the JsonType defining the element type.
     * @param builderService the builder service for nested object construction.
     * @param listIterator iterator over JSON items in the array.
     * @param size the number of elements in the array.
     * @return the constructed List.
     * @throws JsonBuildException if list construction fails.
     */
    public Object buildList(JsonType jType, BuilderService builderService, Iterator<JsonItem> listIterator, int size) throws JsonBuildException;

    /**
     * Builds an array from a JSON array.
     *
     * @param jType the JsonType defining the element type.
     * @param builderService the builder service for nested object construction.
     * @param listIterator iterator over JSON items in the array.
     * @param size the number of elements in the array.
     * @return the constructed array.
     * @throws JsonBuildException if array construction fails.
     */
    public Object buildArray(JsonType jType, BuilderService builderService, Iterator<JsonItem> listIterator, int size) throws JsonBuildException;

    /**
     * Checks if this builder handles primitive types.
     *
     * @return {@code true} if this is a primitive builder, {@code false} otherwise.
     */
    public default boolean isPrimitive() {
        return true;
    }

    /**
     * Converts an attribute value to its string representation.
     *
     * @param attr the attribute value to convert.
     * @return the string representation.
     */
    public String toString(Object attr);

    /**
     * Converts an object to a collection representation.
     *
     * @param ob the object to convert.
     * @return a collection, or {@code null} if not applicable.
     */
    public Collection<?> asCollection(Object ob);

    /**
     * Returns the setter method prefix for this builder.
     *
     * @return the prefix string (default is "set").
     */
    public default String setterPre() {
        return "set";
    }

    /**
     * Returns the getter method prefix for this builder.
     *
     * @return the prefix string (default is "get").
     */
    public default String getterPre() {
        return "get";
    }

    /**
     * Returns permitted values for enum types.
     *
     * @param valuesArray the enum template values.
     * @return a map of literal strings to names, or {@code null} if not applicable.
     */
    public default Map<String, String> permittedValues(JsonEnumTemplate[] valuesArray) {
        return null;
    }
}
