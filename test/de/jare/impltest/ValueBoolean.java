/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

/**
 * Value class representing a boolean value for testing.
 * Implements ValueInterface to provide text representation.
 *
 * @author Janusch Rentenatus
 */
public class ValueBoolean implements ValueInterface {

    private final Boolean frage;

    /**
     * Constructs a ValueBoolean instance with the specified boolean value.
     *
     * @param frage The boolean value.
     */
    public ValueBoolean(Boolean frage) {
        this.frage = frage;
    }

    /**
     * Returns the boolean value.
     *
     * @return The boolean value.
     */
    public Boolean getFrage() {
        return frage;
    }

    @Override
    public String getText() {
        return String.valueOf(frage);
    }

}
