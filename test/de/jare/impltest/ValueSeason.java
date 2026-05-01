/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

/**
 * Value class representing a season enum value for testing.
 * Implements ValueInterface to provide text representation.
 *
 * @author Janusch Rentenatus
 */
public class ValueSeason implements ValueInterface {

    private final EnumSeason season;

    /**
     * Constructs a ValueSeason instance with the specified season.
     *
     * @param season The EnumSeason value.
     */
    public ValueSeason(EnumSeason season) {
        this.season = season;
    }

    @Override
    public String getText() {
        return season.getLiteral() + ":'" + season.getName() + "'";
    }

}
