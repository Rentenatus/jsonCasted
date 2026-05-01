/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

/**
 * Enumeration of JSON node types supported by the jsonCasted system.
 *
 * <p>Each value represents a distinct JSON data type that can be parsed and processed.</p>
 *
 * @author Janusch Rentenatus
 */
public enum JsonNodeType {

    /**
     * Represents a JSON object (map/dictionary) with key-value pairs.
     */
    OBJECT,

    /**
     * Represents a JSON array (ordered list of values).
     */
    ARRAY,

    /**
     * Represents a string value.
     */
    STRING,

    /**
     * Represents a floating-point number (double precision).
     */
    NUMBER,

    /**
     * Represents an integer number (long/64-bit).
     */
    LONG,

    /**
     * Represents a boolean value (true or false).
     */
    BOOLEAN,

    /**
     * Represents a null value.
     */
    NULL
}
