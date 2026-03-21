/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import de.jare.jsoncasted.model.JsonBuildException;
import java.util.Iterator;
import java.util.Set;

/**
 * The JsonItem interface represents a generic JSON structure element. It
 * provides methods to access values, nested elements, and structured data
 * within a JSON representation.
 *
 * @author Janusch Rentenatus
 */
public interface JsonItem {

    /**
     * Retrieves the string representation of the JSON item's value.
     *
     * @return The value as a string.
     */
    public String getStringValue();

    /**
     * Determines if the JSON item represents an array or list structure.
     *
     * @return true if the item is a list, false otherwise.
     */
    public boolean isList();

    /**
     * Returns an iterator to traverse the elements of a JSON array or list.
     *
     * @return Iterator over the contained JsonItem elements.
     */
    public Iterator<JsonItem> listIterator();

    /**
     * Retrieves the number of elements within a JSON array or list.
     *
     * @return The size of the list.
     */
    public int listSize();

    /**
     * Retrieves a JSON property by its key.
     *
     * @param key The name of the property.
     * @return The corresponding JsonItem value or null if the key does not
     * exist.
     */
    public JsonItem getParam(String key);

    /**
     * Retrieves the set of all keys available in the JSON object.
     *
     * @return A set of property names.
     */
    public Set<String> getParamSet();

    /**
     * Builds an instance of an object based on the JSON item's structure.
     *
     * @return The constructed object.
     * @throws JsonBuildException If the instance creation fails.
     */
    public Object buildInstance() throws JsonBuildException;

    /**
     * Returns the class name representation of the JSON item.
     *
     * @return The name of the item's class.
     */
    public String getPrintClassName();

}
