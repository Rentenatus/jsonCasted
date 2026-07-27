/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

/**
 *
 * @author Janusch Rentenatus
 */
public enum JsonTypeVisibility {
    PUBLIC, // sichtbar im Repo, potenziell exportierbar
    PROTECTED; // nur für Submodule/Kind‑Modelle

    public static JsonTypeVisibility fromBoolean(boolean isPublic) {
        return isPublic ? PUBLIC : PROTECTED;
    }

    public static boolean isPublic(JsonTypeVisibility visibility) {
        return visibility == PUBLIC;
    }

    public static boolean isProtected(JsonTypeVisibility visibility) {
        return visibility == PROTECTED;
    }

    public static JsonTypeVisibility fromString(String str) {
        if (str == null) {
            return null;
        }
        switch (str.toLowerCase()) {
            case "public":
                return PUBLIC;
            case "protected":
                return PROTECTED;
            default:
                throw new IllegalArgumentException("Unknown visibility: " + str);
        }
    }
}
