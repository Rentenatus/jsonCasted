/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of JSON collection types for field definitions.
 *
 * <p>
 * This enum specifies how a field should be treated when it contains multiple values:</p>
 * <ul>
 * <li>{@link #NONE} - The field is a single value, not a collection</li>
 * <li>{@link #ARRAY} - The field is a JSON array (square brackets in JSON)</li>
 * <li>{@link #LIST} - The field is a Java List (can be serialized as JSON array)</li>
 * </ul>
 *
 * <p>
 * Utility methods are provided to retrieve an enumerator by literal string, by name, or by integer value. A public
 * unmodifiable list of all values is also available via {@link #VALUES}.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public enum JsonCollectionType implements JsonEnumTemplate {

    /**
     * No collection - the field represents a single value.
     */
    NONE(0, "NONE", "none"),
    /**
     * Array collection - the field is a JSON array.
     */
    ARRAY(1, "ARRAY", "array"),
    /**
     * List collection - the field is a Java List.
     */
    LIST(2, "LIST", "list");

    // --- Integer values for each literal ---
    public static final int NONE_VALUE = 0;
    public static final int ARRAY_VALUE = 1;
    public static final int LIST_VALUE = 2;

    /**
     * Internal array of all enumerators.
     */
    private static final JsonCollectionType[] VALUES_ARRAY = new JsonCollectionType[]{
        NONE, ARRAY, LIST
    };

    /**
     * Public unmodifiable list of all enumerators.
     */
    public static final List<JsonCollectionType> VALUES
            = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the enumerator with the specified literal string.
     *
     * @param literal the literal string
     * @return matching enumerator, or {@code null} if none found
     */
    public static JsonCollectionType get(String literal) {
        for (JsonCollectionType result : VALUES_ARRAY) {
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the enumerator with the specified name.
     *
     * Used by JsonEnumByNameBuilder.
     *
     * @param name the name
     * @return matching enumerator, or {@code null} if none found
     */
    public static JsonCollectionType getByName(String name) {
        for (JsonCollectionType result : VALUES_ARRAY) {
            if (result.getName().equals(name)) {
                return result;
            }
            if (result.getLiteral().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the enumerator with the specified integer value.
     *
     * @param value the integer value
     * @return matching enumerator, or {@code null} if none found
     */
    public static JsonCollectionType get(int value) {
        switch (value) {
            case NONE_VALUE:
                return NONE;
            case ARRAY_VALUE:
                return ARRAY;
            case LIST_VALUE:
                return LIST;
            default:
                return null;
        }
    }

    // --- Internal fields ---
    private final int value;
    private final String name;
    private final String literal;

    /**
     * Private constructor for enum constants.
     */
    private JsonCollectionType(int value, String name, String literal) {
        this.value = value;
        this.name = name;
        this.literal = literal;
    }

    /**
     * @return integer value of the enumerator
     */
    public int getValue() {
        return value;
    }

    /**
     * @return name of the enumerator
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return literal string of the enumerator
     */
    @Override
    public String getLiteral() {
        return literal;
    }

    /**
     * Returns the literal string representation of the enumerator.
     *
     * @return literal string
     */
    @Override
    public String toString() {
        return literal;
    }

    /**
     * Checks if this is not a collection field.
     *
     * @return {@code true} if collection type is NONE.
     */
    public boolean isNotCollection() {
        return this == NONE;
    }

    /**
     * Checks if this is a list or array .
     *
     * @return {@code true} if collection type is ARRAY or LIST.
     */
    public boolean isAsListOrArray() {
        return this == ARRAY || this == LIST;
    }

    /**
     * Checks if this is a list .
     *
     * @return {@code true} if collection type is LIST.
     */
    public boolean isAsList() {
        return this == LIST;
    }

    /**
     * Checks if this is an array .
     *
     * @return {@code true} if collection type is ARRAY.
     */
    public boolean isAsArray() {
        return this == ARRAY;
    }

}
