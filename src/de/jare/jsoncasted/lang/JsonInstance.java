/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.util.HashMap;

/**
 * The JsonInstance class represents a generic JSON object structure. It extends
 * HashMap to store key-value pairs dynamically with type safety.
 *
 * @author Janusch Rentenatus
 * @param <T> The expected type of JSON values.
 */
public class JsonInstance<T> extends HashMap<String, T> {

    /**
     * Stores an object in the JSON structure, casting it to the expected type.
     *
     * @param key The name of the JSON property.
     * @param value The value to store, cast to type T.
     */
    public void putObject(String key, Object value) {
        super.put(key, (T) value);
    }
}
