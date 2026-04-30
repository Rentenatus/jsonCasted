/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

import de.jare.jsoncasted.model.JsonEnumTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Enumeration representing the seasons in the earth model.
 *
 * <p>
 * Each {@code EaSeason} constant defines a season classification:
 * </p>
 * <ul>
 * <li>{@link #NONE} – no season specified</li>
 * <li>{@link #SUMMER} – summer season</li>
 * <li>{@link #AUTUMN} – autumn (fall) season</li>
 * <li>{@link #WINTER} – winter season</li>
 * <li>{@link #SPRING} – spring season</li>
 * </ul>
 *
 * <p>
 * Utility methods are provided to retrieve an enumerator by literal string, by
 * name, or by integer value. A public unmodifiable list of all values is also
 * available via {@link #VALUES}.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public enum EnumSeason implements JsonEnumTemplate {

    /**
     * No season specified.
     */
    NONE(0, "NONE", "None"),
    /**
     * Summer season.
     */
    SUMMER(1, "SUMMER", "Summer"),
    /**
     * Autumn (fall) season.
     */
    AUTUMN(2, "AUTUMN", "Autumn"),
    /**
     * Winter season.
     */
    WINTER(3, "WINTER", "Winter"),
    /**
     * Spring season.
     */
    SPRING(4, "SPRING", "Spring");

    // --- Integer values for each literal ---
    public static final int NONE_VALUE = 0;
    public static final int SUMMER_VALUE = 1;
    public static final int AUTUMN_VALUE = 2;
    public static final int WINTER_VALUE = 3;
    public static final int SPRING_VALUE = 4;

    /**
     * Internal array of all enumerators.
     */
    private static final EnumSeason[] VALUES_ARRAY = new EnumSeason[]{
        NONE, SUMMER, AUTUMN, WINTER, SPRING
    };

    /**
     * Public unmodifiable list of all enumerators.
     */
    public static final List<EnumSeason> VALUES
            = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the enumerator with the specified literal string.
     *
     * @param literal the literal string
     * @return matching enumerator, or {@code null} if none found
     */
    public static EnumSeason get(String literal) {
        for (EnumSeason result : VALUES_ARRAY) {
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the enumerator with the specified name.
     *
     * Used by JsonEnumByNameBuilder .
     *
     * @param name the name
     * @return matching enumerator, or {@code null} if none found
     */
    public static EnumSeason getByName(String name) {
        for (EnumSeason result : VALUES_ARRAY) {
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
    public static EnumSeason get(int value) {
        switch (value) {
            case NONE_VALUE:
                return NONE;
            case SUMMER_VALUE:
                return SUMMER;
            case AUTUMN_VALUE:
                return AUTUMN;
            case WINTER_VALUE:
                return WINTER;
            case SPRING_VALUE:
                return SPRING;
            default:
                return null;
        }
    }

    public static Map<String, String> getLiteralToName() {
        return JsonEnumTemplate.getLiteralToName(VALUES_ARRAY);
    }

    // --- Internal fields ---
    private final int value;
    private final String name;
    private final String literal;

    /**
     * Private constructor for enum constants.
     */
    private EnumSeason(int value, String literal, String name) {
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

} // EaSeason
