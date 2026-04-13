/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import de.jare.jsoncasted.item.builder.BuilderService;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
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

    private final String strValue;
    private final Double numberValue;
    private final Long longValue;
    private final Boolean boolValue;
    private final JsonTypeDescriptor contextClass;

    /**
     * Constructs a JsonValue instance with an associated class type.
     *
     * @param value The raw JSON value as a string.
     * @param contextClass The JSON class description used for instance
     * creation.
     */
    public JsonValue(String value, JsonTypeDescriptor contextClass) {
        this.contextClass = contextClass;
        this.strValue = value;
        this.numberValue = null;
        this.longValue = null;
        this.boolValue = null;
    }

    /**
     * Constructs a JsonValue instance with an associated class type.
     *
     * @param value The raw JSON value as a Double.
     * @param contextClass The JSON class description used for instance
     * creation.
     */
    public JsonValue(Double value, JsonTypeDescriptor contextClass) {
        this.contextClass = contextClass;
        this.numberValue = value;
        this.longValue = null;
        this.strValue = null;
        this.boolValue = null;
    }

    /**
     * Constructs a JsonValue instance with an associated class type.
     *
     * @param value The raw JSON value as a Boolean.
     * @param contextClass The JSON class description used for instance
     * creation.
     */
    public JsonValue(Boolean value, JsonTypeDescriptor contextClass) {
        this.contextClass = contextClass;
        this.boolValue = value;
        this.numberValue = null;
        this.longValue = null;
        this.strValue = null;
    }

    /**
     * Constructs a JsonValue instance with a Long value and an associated class
     * type.
     *
     * @param value The raw JSON value as a Long.
     * @param contextClass The JSON class description used for instance
     * creation.
     */
    public JsonValue(Long value, JsonTypeDescriptor contextClass) {
        this.contextClass = contextClass;
        this.longValue = value;
        this.numberValue = null;
        this.boolValue = null;
        this.strValue = null;
    }

    /**
     * Constructs a JsonValue instance with an associated class type.
     *
     * @param contextClass The JSON class description used for instance
     * creation.
     */
    public JsonValue(JsonTypeDescriptor contextClass) {
        this.contextClass = contextClass;
        this.boolValue = null;
        this.numberValue = null;
        this.longValue = null;
        this.strValue = null;
    }

    /**
     * Retrieves the string representation of the JSON value.
     *
     * @return The stored value as a String.
     */
    @Override
    public String getStringValue() {
        if (numberValue != null) {
            return numberValue.toString();
        }
        if (longValue != null) {
            return longValue.toString();
        }
        if (boolValue != null) {
            return boolValue.toString();
        }
        return strValue;
    }

    /**
     * Retrieves the number representation of the JSON item's value.
     *
     * @return The stored value as a Double.
     */
    @Override
    public Double getNumberValue() {
        if (numberValue != null) {
            return numberValue;
        }
        if (longValue != null) {
            return longValue.doubleValue();
        }
        if (strValue != null) {
            return Double.valueOf(strValue);
        }
        return null;
    }

    /**
     * Retrieves the number representation of the JSON item's value.
     *
     * @return The stored value as a Float.
     */
    @Override
    public Float getFloatValue() {
        if (longValue != null) {
            return longValue.floatValue();
        }
        if (numberValue != null) {
            return numberValue.floatValue();
        }
        if (strValue != null) {
            return Float.valueOf(strValue);
        }
        return null;
    }

    /**
     * Retrieves the number representation of the JSON item's value.
     *
     * @return The stored value as a Long.
     */
    @Override
    public Long getLongValue() {
        if (longValue != null) {
            return longValue;
        }
        if (numberValue != null) {
            return numberValue.longValue();
        }
        if (strValue != null) {
            return Long.valueOf(strValue);
        }
        return null;
    }

    /**
     * Retrieves the boolean representation of the JSON item's value.
     *
     * @return The stored value as a Boolean.
     */
    @Override
    public Boolean getBooleanValue() {
        if (boolValue != null) {
            return boolValue;
        }
        if (strValue != null) {
            return Boolean.valueOf(strValue);
        }
        return null;
    }

    /**
     * Returns the name of the JSON value's class.
     *
     * @return The class name if available; otherwise "null".
     */
    @Override
    public String getPrintClassName() {
        return contextClass == null ? "null" : contextClass.getTypeName();
    }

    /**
     * Retrieves a JSON property by its key. Since this class represents a
     * single value, no key-based retrieval is available.
     *
     * @param key The property name.
     * @return null, as JSON values do not store key-value mappings.
     */
    @Override
    public JsonItem getParam(String key
    ) {
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
    public Object buildInstance(BuilderService builderService) throws JsonBuildException {
        return builderService.buildValue(this, contextClass);
    }
}
