/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

public class ValueInteger implements ValueInterface {

    private final Integer zahl;

    public ValueInteger(Integer zahl) {
        this.zahl = zahl;
    }

    public Integer getZahl() {
        return zahl;
    }

    @Override
    public String getText() {
        return String.valueOf(zahl);
    }

}
