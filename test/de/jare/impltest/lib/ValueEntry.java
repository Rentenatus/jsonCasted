/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest.lib;

/**
 * Value class representing a string value for testing. Implements ValueInterface to provide text representation.
 *
 * @author Janusch Rentenatus
 */
public class ValueEntry implements ValueInterface {

    private final String text;
    private final ValueInterface context;
    private ValueInterface item;

    /**
     * Constructs a ValueString instance with the specified text value.
     *
     * @param text The string value.
     * @param context
     */
    public ValueEntry(String text, ValueInterface context) {
        this.text = text;
        this.context = context;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setItem(ValueInterface item) {
        this.item = item;
    }

    public ValueInterface getItem() {
        return item;
    }

    public ValueInterface getContext() {
        return context;
    }

}
