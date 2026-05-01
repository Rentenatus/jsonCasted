/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

/**
 * Defines different levels of JSON type casting behavior.
 *
 * <p>Each level determines when and how interface resolution and class identification
 * should occur during JSON parsing. Higher levels provide more explicit type information
 * at the cost of potentially more verbose JSON.</p>
 *
 * <p>Casting levels control:</p>
 * <ul>
 *   <li>Whether {@code _class} fields are required for polymorphic types</li>
 *   <li>Whether interface types need explicit casting</li>
 *   <li>Whether class definitions are always written during serialization</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public enum JsonCastingLevel {

    /**
     * Never perform type casting.
     * <p>Compatible with standard JSON but cannot resolve interfaces or
     * polymorphic types. Use when type information is determined by context only.</p>
     */
    NEVER(0),

    /**
     * Require class definition fields only when necessary for interface resolution.
     * <p>This is the most lenient level that still supports interface types.</p>
     */
    NECESSARY_CLASS_DEF(4),

    /**
     * Perform casting only when interfaces mask different underlying classes.
     * <p>Casting occurs when the JSON structure alone cannot determine the concrete type.</p>
     */
    NECESSARY_CAST(5),

    /**
     * Always include class definition fields during serialization.
     * <p>Ensures type information is preserved even when it could be inferred.</p>
     */
    ALWAYS_CLASS_DEF(9),

    /**
     * Always perform type casting, even when the structure provides clear distinction.
     * <p>Most explicit level, ensures type information is always available.</p>
     */
    ALWAYS_CAST(10);

    private final int key;

    /**
     * Constructor for JsonCastingLevel.
     *
     * @param key numeric identifier for the level (used for comparison).
     */
    JsonCastingLevel(int key) {
        this.key = key;
    }

    /**
     * Returns the numeric key associated with this casting level.
     *
     * @return the key value.
     */
    public int getKey() {
        return key;
    }
}
