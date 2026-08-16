/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import de.jare.jsoncasted.lang.JsonResource;
import java.util.*;

/**
 * Central storage for all JsonItems during parsing and building process.
 * This class maintains a registry of all JsonItems from all resources,
 * enabling lazy resolution of proxy references and support for cyclic dependencies.
 *
 * @author Janusch Rentenatus
 */
public final class JsonItemStore {
    
    /**
     * Main storage: maps item IDs to JsonItem instances.
     * ID format: {providerName}::{objectId}
     */
    private final Map<String, JsonItem> itemsById;
    
    /**
     * Optional: references to the original source resources.
     */
    private final List<JsonResource> sourceResources;
    
    /**
     * Cache for already built objects (for performance optimization).
     * Maps item IDs to their built Java objects.
     */
    private final Map<String, Object> builtObjectsCache;
    
    /**
     * Creates a new, empty JsonItemStore.
     */
    public JsonItemStore() {
        this.itemsById = new LinkedHashMap<>();
        this.sourceResources = new ArrayList<>();
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
     * Adds a source resource to the store's registry.
     *
     * @param resource The JsonResource to be registered
     */
    public void addSourceResource(JsonResource resource) {
        if (resource != null && !sourceResources.contains(resource)) {
            this.sourceResources.add(resource);
        }
    }
    
    /**
     * Returns all registered source resources.
     *
     * @return An unmodifiable list of JsonResources
     */
    public List<JsonResource> getSourceResources() {
        return Collections.unmodifiableList(sourceResources);
    }
    
    /**
     * Caches a built object for the specified item ID.
     *
     * @param id The item ID
     * @param object The built Java object
     */
    public void cacheBuiltObject(String id, Object object) {
        if (id != null && object != null) {
            builtObjectsCache.put(id, object);
        }
    }
    
    /**
     * Retrieves a cached built object by item ID.
     *
     * @param id The item ID
     * @return The cached object, or null if not cached
     */
    public Object getCachedObject(String id) {
        return builtObjectsCache.get(id);
    }
    
    /**
     * Checks if a built object is cached for the specified ID.
     *
     * @param id The item ID
     * @return true if a cached object exists
     */
    public boolean isObjectCached(String id) {
        return builtObjectsCache.containsKey(id);
    }
    
    /**
     * Clears the built objects cache.
     */
    public void clearCache() {
        builtObjectsCache.clear();
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
     * Builds a link ID for proxy references.
     * Link IDs and item IDs use the same format.
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
    
    @Override
    public String toString() {
        return "JsonItemStore{items=" + itemsById.size() + 
               ", resources=" + sourceResources.size() + 
               ", cached=" + builtObjectsCache.size() + "}";
    }
}
