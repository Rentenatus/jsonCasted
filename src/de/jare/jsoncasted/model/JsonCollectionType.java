/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

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
 * @author Janusch Rentenatus
 */
public enum JsonCollectionType {

    /**
     * No collection - the field represents a single value.
     */
    NONE() {
    },
    /**
     * Array collection - the field is a JSON array.
     */
    ARRAY() {
    },
    /**
     * List collection - the field is a Java List.
     */
    LIST() {
    };

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
