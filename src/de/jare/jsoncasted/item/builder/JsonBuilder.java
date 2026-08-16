/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonItemStore;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;

/**
 * The JsonBuilder class provides functionality for building Java objects from
 * JSON data. It works as a wrapper around the BuilderService to convert JSON
 * items into their corresponding Java object representations.
 *
 * @author Janusch Rentenatus
 */
public class JsonBuilder {

    private final JsonItem rootItem;
    private final JsonItemStore itemStore;
    private BuilderService builderService;

    /**
     * Builds a Java object instance from a JSON item using the specified model.
     * This is a static convenience method that creates a BuilderService internally.
     * 
     * This method now automatically uses the JsonItemStore from the rootItem if available,
     * enabling proxy resolution for the new ItemStore architecture while maintaining
     * backward compatibility.
     *
     * @param model The JSON model containing type definitions and mappings.
     * @param throwClassEx If true, throws exceptions when classes are not found;
     *        if false, uses default handling.
     * @param rootItem The root JSON item to build from.
     * @return The constructed Java object.
     * @throws JsonBuildException If object construction fails.
     */
    public static Object buildInstance(JsonModel model, boolean throwClassEx, JsonItem rootItem) throws JsonBuildException {
        // Use the itemStore from rootItem if available (new architecture)
        JsonItemStore itemStore = rootItem != null ? rootItem.getItemStore() : null;
        BuilderService builderService = new BuilderService(model, throwClassEx, itemStore);
        return builderService.build(rootItem);
    }
    
    /**
     * Builds a Java object instance from a JSON item using the specified model and item store.
     * This version supports the new ItemStore architecture for proxy reference resolution.
     *
     * @param model The JSON model containing type definitions and mappings.
     * @param throwClassEx If true, throws exceptions when classes are not found;
     *        if false, uses default handling.
     * @param rootItem The root JSON item to build from.
     * @param itemStore The JsonItemStore for proxy reference resolution.
     * @return The constructed Java object.
     * @throws JsonBuildException If object construction fails.
     */
    public static Object buildInstance(JsonModel model, boolean throwClassEx, JsonItem rootItem, JsonItemStore itemStore) throws JsonBuildException {
        BuilderService builderService = new BuilderService(model, throwClassEx, itemStore);
        return builderService.build(rootItem);
    }

    /**
     * Constructs a JsonBuilder instance with the specified root JSON item.
     *
     * @param rootItem The root JSON item to build from.
     */
    public JsonBuilder(JsonItem rootItem) {
        this.rootItem = rootItem;
        this.itemStore = null;
        this.builderService = null;
    }
    
    /**
     * Constructs a JsonBuilder instance with the specified root JSON item and item store.
     * This version supports the new ItemStore architecture.
     *
     * @param rootItem The root JSON item to build from.
     * @param itemStore The JsonItemStore for proxy reference resolution.
     */
    public JsonBuilder(JsonItem rootItem, JsonItemStore itemStore) {
        this.rootItem = rootItem;
        this.itemStore = itemStore;
        this.builderService = null;
    }

    /**
     * Builds a Java object instance from the root JSON item using the specified model.
     * 
     * This method now automatically uses the JsonItemStore from the rootItem if the
     * builder was created without an explicit itemStore, enabling backward compatibility
     * with the new ItemStore architecture.
     *
     * @param model The JSON model containing type definitions and mappings.
     * @param throwClassEx If true, throws exceptions when classes are not found;
     *        if false, uses default handling.
     * @return The constructed Java object.
     * @throws JsonBuildException If object construction fails.
     */
    public Object buildInstance(JsonModel model, boolean throwClassEx) throws JsonBuildException {
        // Use explicit itemStore if set, otherwise try to get it from rootItem
        JsonItemStore effectiveStore = itemStore != null ? itemStore : (rootItem != null ? rootItem.getItemStore() : null);
        builderService = new BuilderService(model, throwClassEx, effectiveStore);
        return builderService.build(rootItem);
    }
    
    /**
     * Builds a Java object instance from the root JSON item using the specified model and custom item store.
     * The custom item store overrides the one set in the constructor.
     *
     * @param model The JSON model containing type definitions and mappings.
     * @param throwClassEx If true, throws exceptions when classes are not found;
     *        if false, uses default handling.
     * @param customItemStore The custom JsonItemStore to use (overrides constructor's itemStore).
     * @return The constructed Java object.
     * @throws JsonBuildException If object construction fails.
     */
    public Object buildInstance(JsonModel model, boolean throwClassEx, JsonItemStore customItemStore) throws JsonBuildException {
        builderService = new BuilderService(model, throwClassEx, customItemStore);
        return builderService.build(rootItem);
    }

    /**
     * Returns the root JSON item associated with this builder.
     *
     * @return The root JSON item.
     */
    public JsonItem getRootItem() {
        return rootItem;
    }

    /**
     * Returns the JsonItemStore associated with this builder.
     *
     * @return The JsonItemStore, or null if not set.
     */
    public JsonItemStore getItemStore() {
        return itemStore;
    }

    /**
     * Returns the BuilderService used by this builder.
     *
     * @return The BuilderService instance, or null if not yet initialized.
     */
    public BuilderService getBuilderService() {
        return builderService;
    }

}
