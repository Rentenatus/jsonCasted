/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

public class ValueSeason implements ValueInterface {

    private final EnumSeason season;

    public ValueSeason(EnumSeason season) {
        this.season = season;
    }

    @Override
    public String getText() {
        return season.getLiteral() + ":'" + season.getName() + "'";
    }

}
