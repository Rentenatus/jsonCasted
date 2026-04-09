/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonInter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JsonList class represents a JSON array structure. It stores a list of
 * JSON items and provides methods to access and manipulate them.
 *
 * @author Janusch Rentenatus
 */
public class JsonList implements JsonItem {

    private final ArrayList<JsonItem> list;
    private final JsonTypeDescriptor contextClass;
    private final boolean asList;

    /**
     * Constructs a JsonList instance with the specified items and type
     * information.
     *
     * @param list The list of JSON items.
     * @param asList Indicates whether the list is structured as a typical JSON
     * array.
     * @param contextClass The JSON type description used for instance creation.
     */
    public JsonList(ArrayList<JsonItem> list, boolean asList, JsonTypeDescriptor contextClass) {
        this.contextClass = contextClass;
        this.list = list;
        this.asList = asList;
    }

    /**
     * Retrieves the string representation of the JSON array's size.
     *
     * @return A formatted string indicating the number of elements in the list.
     */
    @Override
    public String getStringValue() {
        return "[" + list.size() + "]";
    }

    /**
     * Retrieves the number representation of the JSON item's value.
     *
     * @return null, as lists typically have nested properties.
     */
    @Override
    public Double getNumberValue() {
        return null;
    }

    /**
     * Retrieves the float number representation of the JSON item's value.
     *
     * @return null, as lists typically have nested properties.
     */
    @Override
    public Float getFloatValue() {
        return null;
    }

    /**
     * Retrieves the whole number representation of the JSON item's value.
     *
     * @return null, as lists typically have nested properties.
     */
    @Override
    public Long getLongValue() {
        return null;
    }

    /**
     * Retrieves the boolean representation of the JSON item's value.
     *
     * @return null, as lists typically have nested properties.
     */
    @Override
    public Boolean getBooleanValue() {
        return null;
    }

    /**
     * Determines if this JSON item represents an array.
     *
     * @return true, as this class always represents a list structure.
     */
    @Override
    public boolean isList() {
        return true;
    }

    /**
     * Returns an iterator to traverse the elements of the JSON list.
     *
     * @return Iterator over the contained JsonItem elements.
     */
    @Override
    public Iterator<JsonItem> listIterator() {
        return list.iterator();
    }

    /**
     * Retrieves the number of elements within the JSON list.
     *
     * @return The size of the list.
     */
    @Override
    public int listSize() {
        return list.size();
    }

    /**
     * Returns a string representation of the JSON list's type.
     *
     * @return "List<>" for structured lists, or "[]" for simple arrays.
     */
    @Override
    public String getPrintClassName() {
        return asList ? "List<>" : "[]";
    }

    /**
     * Retrieves a JSON property by key. Since this class represents an array,
     * no key-based retrieval is available.
     *
     * @param key The property name.
     * @return null, as JSON lists do not store key-value mappings.
     */
    @Override
    public JsonItem getParam(String key) {
        return null;
    }

    /**
     * Retrieves the set of all keys available in the JSON object. Since this
     * class represents an array, it does not maintain keys.
     *
     * @return null, as JSON lists do not have parameter names.
     */
    @Override
    public Set<String> getParamSet() {
        return null;
    }

    /**
     * Builds an instance of an object based on the JSON list's structure.
     *
     * @return The constructed object, or null if jType is undefined.
     * @throws JsonBuildException If instance creation fails.
     */
    @Override
    public Object buildInstance(JsonModel model) throws JsonBuildException {
        JsonClass jType = model.getJsonClass(contextClass.getTypeName());
        if (jType == null) {
            JsonInter jInter = model.getJsonInter(contextClass.getTypeName());
            if (jInter != null) {
                return jInter.build(listIterator(), asList, listSize());
            }
            Logger.getGlobal().log(Level.SEVERE, "JsonClass '{0}' is unknown.", new Object[]{contextClass.getTypeName()});
            return null;
        }
        return jType.build(listIterator(), asList, listSize());
    }
}
