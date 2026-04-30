/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

public class ValueStringSubSub extends ValueStringSub implements ValueInterface {

    private final Boolean frage;

    public ValueStringSubSub(String text, Boolean frage) {
        super(text);
        this.frage = frage;
    }

    public Boolean getFrage() {
        return frage;
    }

    @Override
    public String getText() {
        return super.getText() + " = " + String.valueOf(frage);
    }
}
