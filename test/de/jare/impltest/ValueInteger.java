/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

/**
 * Value class representing an integer value for testing.
 * Implements ValueInterface to provide text representation.
 *
 * @author Janusch Rentenatus
 */
public class ValueInteger implements ValueInterface {

    private final Integer zahl;

    /**
     * Constructs a ValueInteger instance with the specified integer value.
     *
     * @param zahl The integer value.
     */
    public ValueInteger(Integer zahl) {
        this.zahl = zahl;
    }

    /**
     * Returns the integer value.
     *
     * @return The integer value.
     */
    public Integer getZahl() {
        return zahl;
    }

    @Override
    public String getText() {
        return String.valueOf(zahl);
    }

}
