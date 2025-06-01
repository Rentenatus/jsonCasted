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
import java.util.Iterator;
import java.util.Set;

/**
 * The JsonValue class represents a primitive or single-value JSON element. It
 * stores a basic value, such as a string or number, and provides methods for
 * retrieval.
 *
 * @author Janusch Rentenatus
 */
public class JsonValue implements JsonItem {

    private final String value;
    private final JsonClass jClass;

    /**
     * Constructs a JsonValue instance with an associated class type.
     *
     * @param value The raw JSON value as a string.
     * @param aClass The JSON class used for instance creation.
     */
    public JsonValue(String value, JsonClass aClass) {
        this.jClass = aClass;
        this.value = value;
    }

    /**
     * Retrieves the string representation of the JSON value.
     *
     * @return The stored value as a string.
     */
    @Override
    public String getStringValue() {
        return value;
    }

    /**
     * Returns the name of the JSON value's class.
     *
     * @return The class name if available; otherwise "null".
     */
    @Override
    public String getPrintClassName() {
        return jClass == null ? "null" : jClass.getcName();
    }

    /**
     * Retrieves a JSON property by its key. Since this class represents a
     * single value, no key-based retrieval is available.
     *
     * @param key The property name.
     * @return null, as JSON values do not store key-value mappings.
     */
    @Override
    public JsonItem getParam(String key) {
        return null;
    }

    /**
     * Retrieves the set of all keys available in the JSON object. Since this
     * class represents a single value, it does not maintain keys.
     *
     * @return null, as JSON values do not have parameter names.
     */
    @Override
    public Set<String> getParamSet() {
        return null;
    }

    /**
     * Determines if this JSON item represents an array.
     *
     * @return false, as this class represents a primitive value.
     */
    @Override
    public boolean isList() {
        return false;
    }

    /**
     * Returns an iterator over the object's single element. Since a primitive
     * value cannot be iterated over, this iterator always returns false for
     * hasNext().
     *
     * @return An iterator with no elements.
     */
    @Override
    public Iterator<JsonItem> listIterator() {
        return new Iterator<JsonItem>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public JsonItem next() {
                return null;
            }
        };
    }

    /**
     * Retrieves the number of elements within the JSON item. Since a primitive
     * value does not contain multiple elements, this always returns 0.
     *
     * @return 0, indicating a single value.
     */
    @Override
    public int listSize() {
        return 0;
    }

    /**
     * Builds an instance of an object based on the JSON value.
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
