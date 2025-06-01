/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

/**
 * The JsonDebugLevel enum defines different levels of debugging information.
 * Each level determines the severity of the debug messages that should be
 * logged or processed.
 *
 * @author Janusch Rentenatus
 */
public enum JsonDebugLevel {

    /**
     * Provides informational messages.
     */
    INFO(10),
    /**
     * Provides warning messages.
     */
    WARNING(5),
    /**
     * Basic runtime level without additional debug information.
     */
    SIMPLE(0);

    private final int key;

    /**
     * Constructor for JsonDebugLevel.
     *
     * @param key Identifier key for the level.
     */
    JsonDebugLevel(int key) {
        this.key = key;
    }

    /**
     * Returns the key associated with the debug level.
     *
     * @return The debug level key.
     */
    public int getKey() {
        return key;
    }

    /**
     * Checks if the current debug level satisfies the given minimum level.
     *
     * @param min The minimum required debug level.
     * @return true if the current level is equal to or higher than min.
     */
    public boolean satisfy(JsonDebugLevel min) {
        return key >= min.getKey();
    }

    /**
     * Checks if the current debug level includes warnings.
     *
     * @return true if warnings should be displayed.
     */
    public boolean satisfyWarning() {
        return key >= WARNING.getKey();
    }

    /**
     * Checks if the current debug level includes informational messages.
     *
     * @return true if informational messages should be displayed.
     */
    public boolean satisfyInfo() {
        return key >= INFO.getKey();
    }
}
