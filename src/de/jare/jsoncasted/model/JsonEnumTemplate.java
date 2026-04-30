/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Template or Interface, how to use Enums.
 *
 * @author Janusch Rentenatus
 */
public interface JsonEnumTemplate {

    // Template for JsonEnumByNameBuilder
    /**
     * The static builder cannot be enforced; this is only a pattern. If
     * present, this static method is invoked via reflection. Returns the
     * enumerator with the specified name.
     *
     * Used by JsonEnumByNameBuilder, if model.newJsonEnumByName(XYZ.class)
     * called.
     *
     * Use better model.newJsonEnumByName(XYZ.class, XYZ.VALUES).
     *
     * @param name the name or literal as string.
     * @return matching enumerator, or {@code null} if none found
     */
    public static JsonEnumTemplate getByName(String name) {
        return null;
    }

    /**
     * The static descriptor cannot be enforced; this is only a pattern. If
     * present, this static method is invoked via reflection. Returns the Map
     * with possible pairs.
     *
     * Used by JsonEnumByNameBuilder, if model.newJsonEnumByName(XYZ.class)
     * called.
     *
     * Use better model.newJsonEnumByName(XYZ.class, XYZ.VALUES).
     *
     * @return a Map literal to name
     */
    public static Map<String, String> getLiteralToName() {
        return getLiteralToName(new JsonEnumTemplate[0]);
    }

    // Interface for JsonEnumByNameBuilder    
    /**
     * @return name of the enumerator
     */
    public String getName();

    /**
     * @return literal string of the enumerator
     */
    public String getLiteral();

    public static Map<String, String> getLiteralToName(JsonEnumTemplate[] valuesArray) {
        LinkedHashMap<String, String> ret = new LinkedHashMap<>();
        for (JsonEnumTemplate val : valuesArray) {
            ret.put(val.getLiteral(), val.getName());
        }
        return ret;
    }
}
