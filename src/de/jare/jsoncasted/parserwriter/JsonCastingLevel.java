/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

/**
 * The `JsonCastingLevel` enum defines different levels of JSON type casting.
 * Each level determines when interface resolution and class identification should occur.
 *
 * @author Janusch Rentenatus
 */
public enum JsonCastingLevel {

    /**
     * Never cast types – compatible with main JSON but cannot resolve interfaces.
     */
    NEVER(0),

    /**
     * Cast types only when interfaces mask different underlying classes.
     */
    NECESSARY(5),

    /**
     * Always cast types, even if the data structure already provides a clear distinction.
     */
    ALWAYS(10);

    private final int key;

    /**
     * Constructor for `JsonCastingLevel`.
     *
     * @param key Identifier key for the level.
     */
    JsonCastingLevel(int key) {
        this.key = key;
    }

    /**
     * Returns the key associated with the casting level.
     *
     * @return The key of the casting level.
     */
    public int getKey() {
        return key;
    }
}
