/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

/**
 * Value class representing a string value with extended functionality.
 * Extends ValueString and implements ValueInterface.
 *
 * @author Janusch Rentenatus
 */
public class ValueStringSub extends ValueString implements ValueInterface {

    /**
     * Constructs a ValueStringSub instance with the specified text value.
     *
     * @param text The string value.
     */
    public ValueStringSub(String text) {
        super(text);
    }

}
