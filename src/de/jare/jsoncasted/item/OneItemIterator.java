/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import java.util.Iterator;

/**
 * The OneItemIterator class implements an iterator that provides access to a
 * single JSON item. It allows iteration over a single object and ensures that
 * next() can only be called once.
 *
 * @author Janusch Rentenatus
 */
public class OneItemIterator implements Iterator<JsonItem> {

    private final JsonItem item;
    private boolean hasNext = true;

    /**
     * Constructs a OneItemIterator instance for a given JSON item.
     *
     * @param item The JSON item to be iterated over.
     */
    public OneItemIterator(JsonItem item) {
        this.item = item;
    }

    /**
     * Checks whether there is a next item available. Since this iterator holds
     * only one item, this returns true initially and false thereafter.
     *
     * @return true if the item has not been accessed yet, false after next() is
     * called.
     */
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    /**
     * Retrieves the single JSON item. Once called, the iterator is exhausted
     * and hasNext() returns false.
     *
     * @return The stored JSON item.
     */
    @Override
    public JsonItem next() {
        hasNext = false;
        return item;
    }
}
