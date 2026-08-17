/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Central storage for all JsonItems during parsing and building process. This class maintains a registry of all
 * JsonItems from all resources, enabling lazy resolution of proxy references and support for cyclic dependencies.
 *
 * <p>
 * <b>Thread Safety:</b> This class is NOT thread-safe. Each JsonItemStore instance should be used by a single thread or
 * external synchronization must be provided. In typical usage, a JsonItemStore is created during parsing and used
 * during building in a single-threaded context.</p>
 *
 * <p>
 * <b>Memory Management:</b> The built objects cache uses WeakReference to allow garbage collection of cached objects.
 * Use {@link #cleanupGarbageCollectedEntries()} to periodically remove stale cache entries.</p>
 *
 * @author Janusch Rentenatus
 */
public final class JsonItemStore {

    /**
     * Main storage: maps item IDs to JsonItem instances. ID format: {providerName}::{objectId}
     */
    private final Map<String, JsonItem> itemsById;

    /**
     * Cache for already built objects (for performance optimization). Uses WeakReference to allow garbage collection of
     * cached objects. Maps item IDs to WeakReference<Object> of their built Java objects.
     */
    private final Map<String, WeakReference<Object>> builtObjectsCache;

    /**
     * Creates a new, empty JsonItemStore.
     */
    public JsonItemStore() {
        this.itemsById = new LinkedHashMap<>();
        this.builtObjectsCache = new HashMap<>();
    }

    /**
     * Registers a JsonItem with its ID in the store.
     *
     * @param id The unique identifier for the item (format: providerName::objectId)
     * @param item The JsonItem to be stored
     */
    public void addItem(String id, JsonItem item) {
        if (id != null && item != null) {
            itemsById.put(id, item);
        }
    }

    /**
     * Retrieves a JsonItem by its ID.
     *
     * @param id The unique identifier of the item
     * @return The JsonItem associated with the ID, or null if not found
     */
    public JsonItem getItem(String id) {
        return itemsById.get(id);
    }

    /**
     * Checks if an item with the specified ID exists in the store.
     *
     * @param id The unique identifier to check
     * @return true if an item with the ID exists, false otherwise
     */
    public boolean contains(String id) {
        return itemsById.containsKey(id);
    }

    /**
     * Returns all items stored in this store.
     *
     * @return A collection of all JsonItems
     */
    public Collection<JsonItem> getAllItems() {
        return itemsById.values();
    }

    /**
     * Caches a built object for the specified item ID. Uses WeakReference to allow garbage collection.
     *
     * @param id The item ID
     * @param object The built Java object
     */
    public void cacheBuiltObject(String id, Object object) {
        if (id != null && object != null) {
            builtObjectsCache.put(id, new WeakReference<>(object));
        }
    }

    /**
     * Retrieves a cached built object by item ID. Returns null if not cached or if the cached object has been garbage
     * collected.
     *
     * @param id The item ID
     * @return The cached object, or null if not cached or garbage collected
     */
    public Object getCachedObject(String id) {
        WeakReference<Object> ref = builtObjectsCache.get(id);
        return ref != null ? ref.get() : null;
    }

    /**
     * Checks if a built object is cached for the specified ID. Note: This checks if a cache entry exists, not if the
     * object is still reachable.
     *
     * @param id The item ID
     * @return true if a cache entry exists (even if object was garbage collected)
     */
    public boolean isObjectCached(String id) {
        return builtObjectsCache.containsKey(id);
    }

    /**
     * Checks if a cached object for the specified ID is still reachable. This checks if the object hasn't been garbage
     * collected.
     *
     * @param id The item ID
     * @return true if a cache entry exists AND the object is still reachable
     */
    public boolean isObjectReachable(String id) {
        WeakReference<Object> ref = builtObjectsCache.get(id);
        return ref != null && ref.get() != null;
    }

    /**
     * Clears the built objects cache.
     */
    public void clearCache() {
        builtObjectsCache.clear();
    }

    /**
     * Removes cached objects that have been garbage collected. This can be called periodically to clean up stale
     * references.
     *
     * @return The number of entries removed
     */
    public int cleanupGarbageCollectedEntries() {
        int removed = 0;
        Iterator<Map.Entry<String, WeakReference<Object>>> iterator = builtObjectsCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, WeakReference<Object>> entry = iterator.next();
            if (entry.getValue().get() == null) {
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

    /**
     * Returns the number of items in the store.
     *
     * @return The count of registered items
     */
    public int size() {
        return itemsById.size();
    }

    /**
     * Checks if the store is empty.
     *
     * @return true if no items are registered
     */
    public boolean isEmpty() {
        return itemsById.isEmpty();
    }

    /**
     * Builds a unique item ID from provider name and object ID.
     *
     * @param providerName The name of the provider/resource
     * @param objectId The object identifier
     * @return The formatted item ID (providerName::objectId)
     */
    public static String buildItemId(String providerName, String objectId) {
        if (providerName == null || objectId == null) {
            return null;
        }
        return providerName + "::" + objectId;
    }

    /**
     * Builds a link ID for proxy references. Link IDs and item IDs use the same format.
     *
     * @param providerName The name of the provider/resource
     * @param linkId The link identifier (from _woodLink)
     * @return The formatted link ID
     */
    public static String buildLinkId(String providerName, String linkId) {
        return buildItemId(providerName, linkId);
    }

    /**
     * Extracts the provider name from a full item/link ID.
     *
     * @param fullId The full ID (format: providerName::objectId)
     * @return The provider name, or null if the format is invalid
     */
    public static String extractProviderName(String fullId) {
        if (fullId == null) {
            return null;
        }
        int separatorIndex = fullId.indexOf("::");
        if (separatorIndex <= 0) {
            return null;
        }
        return fullId.substring(0, separatorIndex);
    }

    /**
     * Extracts the object ID from a full item/link ID.
     *
     * @param fullId The full ID (format: providerName::objectId)
     * @return The object ID, or null if the format is invalid
     */
    public static String extractObjectId(String fullId) {
        if (fullId == null) {
            return null;
        }
        int separatorIndex = fullId.indexOf("::");
        if (separatorIndex < 0 || separatorIndex >= fullId.length() - 1) {
            return null;
        }
        return fullId.substring(separatorIndex + 2);
    }

    /**
     * Returns the number of cached objects that are still reachable.
     *
     * @return The count of reachable cached objects
     */
    public int getReachableCacheSize() {
        int count = 0;
        for (WeakReference<Object> ref : builtObjectsCache.values()) {
            if (ref.get() != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return "JsonItemStore{items=" + itemsById.size()
                + ", cached=" + builtObjectsCache.size()
                + ", reachable=" + getReachableCacheSize() + "}";
    }
}
