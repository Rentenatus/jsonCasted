/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

/**
 * Value class representing a string value for testing.
 * Implements ValueInterface to provide text representation.
 *
 * @author Janusch Rentenatus
 */
public class ValueString implements ValueInterface {

    private final String text;

    /**
     * Constructs a ValueString instance with the specified text value.
     *
     * @param text The string value.
     */
    public ValueString(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

}
