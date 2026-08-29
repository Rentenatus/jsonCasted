/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.convertservice;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.JsonValue;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for deep cloning of JsonItem instances with cycle detection. This is used to ensure that CONTAINMENT
 * fields get their own exclusive copy of child objects during parsing.
 *
 * @author Janusch Rentenatus
 */
public class JsonItemCloner {

    private JsonItemCloner() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Deep clones a JsonItem, handling circular references.
     *
     * @param item The JsonItem to clone.
     * @return A deep clone of the item, or null if the input is null.
     */
    public static JsonItem cloneItem(JsonItem item) {
        if (item == null) {
            return null;
        }
        return cloneItem(item, new IdentityHashMap<>());
    }

    /**
     * Internal method for deep cloning with cycle detection.
     *
     * @param item The JsonItem to clone.
     * @param clonedMap Map tracking already cloned items to prevent infinite recursion.
     * @return A deep clone of the item.
     */
    private static JsonItem cloneItem(JsonItem item, Map<JsonItem, JsonItem> clonedMap) {
        if (item == null) {
            return null;
        }

        // Check if already cloned to handle circular references
        if (clonedMap.containsKey(item)) {
            return clonedMap.get(item);
        }

        JsonItem clone;
        if (item instanceof JsonObject) {
            JsonObject original = (JsonObject) item;
            clone = new JsonObject(original.getContextClass(), Long.MIN_VALUE);
            clonedMap.put(item, clone);
            for (String key : original.getParamSet()) {
                JsonItem child = original.getParam(key);
                JsonItem clonedChild = cloneItem(child, clonedMap);
                ((JsonObject) clone).putParam(key, clonedChild);
            }
        } else if (item instanceof JsonList) {
            JsonList original = (JsonList) item;
            java.util.ArrayList<JsonItem> clonedList = new java.util.ArrayList<>();
            clone = new JsonList(clonedList, original.isAsListFlag(), original.getContextClass(), Long.MIN_VALUE);
            clonedMap.put(item, clone);
            Iterator<JsonItem> iterator = original.listIterator();
            while (iterator.hasNext()) {
                clonedList.add(cloneItem(iterator.next(), clonedMap));
            }
        } else if (item instanceof JsonValue) {
            JsonValue original = (JsonValue) item;
            clone = original.cloneDeep();
            clonedMap.put(item, clone);
        } else {
            clone = item;
        }

        return clone;
    }
}
