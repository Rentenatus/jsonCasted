/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.item.JsonClass;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * The JsonObject class represents a JSON object structure. It stores key-value
 * pairs and provides methods for accessing and manipulating properties.
 *
 * @author Janusch Rentenatus
 */
public class JsonObject implements JsonItem {

    private final HashMap<String, JsonItem> map;
    private final JsonClass jClass;

    /**
     * Constructs a JsonObject instance with an associated class type.
     *
     * @param aClass The JSON class used for instance creation.
     */
    public JsonObject(JsonClass aClass) {
        this.jClass = aClass;
        map = new HashMap<>();
    }

    /**
     * Adds a key-value pair to the JSON object.
     *
     * @param key The name of the JSON property.
     * @param value The corresponding JsonItem value.
     */
    public void putParam(String key, JsonItem value) {
        map.put(key, value);
    }

    /**
     * Returns the name of the JSON object's class.
     *
     * @return The class name if available; otherwise "null".
     */
    @Override
    public String getPrintClassName() {
        return jClass == null ? "null" : jClass.getcName();
    }

    /**
     * Retrieves a JSON property by its key.
     *
     * @param key The name of the property.
     * @return The corresponding JsonItem value or null if the key does not
     * exist.
     */
    @Override
    public JsonItem getParam(String key) {
        return map.get(key);
    }

    /**
     * Retrieves the set of all keys available in the JSON object.
     *
     * @return A set of property names.
     */
    @Override
    public Set<String> getParamSet() {
        return map.keySet();
    }

    /**
     * Retrieves the string representation of the JSON object's value. Since
     * this class represents an object structure, it does not return a simple
     * string value.
     *
     * @return null, as objects typically have nested properties.
     */
    @Override
    public String getStringValue() {
        return null;
    }

    /**
     * Determines if this JSON item represents an array.
     *
     * @return false, as this class represents a JSON object.
     */
    @Override
    public boolean isList() {
        return false;
    }

    /**
     * Returns an iterator over the object's single element. A JSON object does
     * not function as a list but can be wrapped for iteration.
     *
     * @return A OneItemIterator containing this object.
     */
    @Override
    public Iterator<JsonItem> listIterator() {
        return new OneItemIterator(this);
    }

    /**
     * Retrieves the number of elements within the JSON object. While an object
     * contains multiple properties, it is counted as a single structured
     * entity.
     *
     * @return 1, representing a single JSON object.
     */
    @Override
    public int listSize() {
        return 1;
    }

    /**
     * Builds an instance of an object based on the JSON structure.
     *
     * @return The constructed object, or null if jClass is undefined.
     * @throws JsonBuildException If instance creation fails.
     */
    @Override
    public Object buildInstance() throws JsonBuildException {
        if (jClass == null) {
            return null;
        }
        return jClass.build(this);
    }
}
