/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.jsoncasted.model.JsonEnumTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of JSON node types supported by the jsonCasted system.
 *
 * <p>
 * Each {@code JsonNodeType} constant defines a distinct JSON data type that can be parsed and processed:
 * </p>
 * <ul>
 * <li>{@link #OBJECT} - JSON object (map/dictionary) with key-value pairs</li>
 * <li>{@link #ARRAY} - JSON array (ordered list of values)</li>
 * <li>{@link #STRING} - string value</li>
 * <li>{@link #NUMBER} - floating-point number (double precision)</li>
 * <li>{@link #LONG} - integer number (long/64-bit)</li>
 * <li>{@link #BOOLEAN} - boolean value (true or false)</li>
 * <li>{@link #NULL} - null value</li>
 * </ul>
 *
 * <p>
 * Utility methods are provided to retrieve an enumerator by literal string, by name, or by integer value. A public
 * unmodifiable list of all values is also available via {@link #VALUES}.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public enum JsonNodeType implements JsonEnumTemplate {

    /**
     * Represents a JSON object (map/dictionary) with key-value pairs.
     */
    OBJECT(0, "OBJECT", "object"),
    /**
     * Represents a JSON array (ordered list of values).
     */
    ARRAY(1, "ARRAY", "array"),
    /**
     * Represents a string value.
     */
    STRING(2, "STRING", "string"),
    /**
     * Represents a floating-point number (double precision).
     */
    NUMBER(3, "NUMBER", "number"),
    /**
     * Represents an integer number (long/64-bit).
     */
    LONG(4, "LONG", "long"),
    /**
     * Represents a boolean value (true or false).
     */
    BOOLEAN(5, "BOOLEAN", "boolean"),
    /**
     * Represents a null value.
     */
    NULL(6, "NULL", "null");

    // --- Integer values for each literal ---
    public static final int OBJECT_VALUE = 0;
    public static final int ARRAY_VALUE = 1;
    public static final int STRING_VALUE = 2;
    public static final int NUMBER_VALUE = 3;
    public static final int LONG_VALUE = 4;
    public static final int BOOLEAN_VALUE = 5;
    public static final int NULL_VALUE = 6;

    /**
     * Internal array of all enumerators.
     */
    private static final JsonNodeType[] VALUES_ARRAY = new JsonNodeType[]{
        OBJECT, ARRAY, STRING, NUMBER, LONG, BOOLEAN, NULL
    };

    /**
     * Public unmodifiable list of all enumerators.
     */
    public static final List<JsonNodeType> VALUES
            = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the enumerator with the specified literal string.
     *
     * @param literal the literal string
     * @return matching enumerator, or {@code null} if none found
     */
    public static JsonNodeType get(String literal) {
        for (JsonNodeType result : VALUES_ARRAY) {
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
    public static JsonNodeType getByName(String name) {
        for (JsonNodeType result : VALUES_ARRAY) {
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
    public static JsonNodeType get(int value) {
        switch (value) {
            case OBJECT_VALUE:
                return OBJECT;
            case ARRAY_VALUE:
                return ARRAY;
            case STRING_VALUE:
                return STRING;
            case NUMBER_VALUE:
                return NUMBER;
            case LONG_VALUE:
                return LONG;
            case BOOLEAN_VALUE:
                return BOOLEAN;
            case NULL_VALUE:
                return NULL;
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
    private JsonNodeType(int value, String literal, String name) {
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
     * Checks if this type indicates a JSON object.
     *
     * @return true if this is OBJECT, false otherwise
     */
    public boolean isObject() {
        return this == OBJECT;
    }

    /**
     * Checks if this type indicates a JSON array.
     *
     * @return true if this is ARRAY, false otherwise
     */
    public boolean isArray() {
        return this == ARRAY;
    }

    /**
     * Checks if this type indicates a string value.
     *
     * @return true if this is STRING, false otherwise
     */
    public boolean isString() {
        return this == STRING;
    }

    /**
     * Checks if this type indicates a floating-point number.
     *
     * @return true if this is NUMBER, false otherwise
     */
    public boolean isNumber() {
        return this == NUMBER;
    }

    /**
     * Checks if this type indicates an integer number.
     *
     * @return true if this is LONG, false otherwise
     */
    public boolean isLong() {
        return this == LONG;
    }

    /**
     * Checks if this type indicates a boolean value.
     *
     * @return true if this is BOOLEAN, false otherwise
     */
    public boolean isBoolean() {
        return this == BOOLEAN;
    }

    /**
     * Checks if this type indicates a null value.
     *
     * @return true if this is NULL, false otherwise
     */
    public boolean isNull() {
        return this == NULL;
    }

} // JsonNodeType
