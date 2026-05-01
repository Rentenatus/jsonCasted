/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItem;
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
    private BuilderService builderService;

    /**
     * Builds a Java object instance from a JSON item using the specified model.
     * This is a static convenience method that creates a BuilderService internally.
     *
     * @param model The JSON model containing type definitions and mappings.
     * @param throwClassEx If true, throws exceptions when classes are not found;
     *        if false, uses default handling.
     * @param rootItem The root JSON item to build from.
     * @return The constructed Java object.
     * @throws JsonBuildException If object construction fails.
     */
    public static Object buildInstance(JsonModel model, boolean throwClassEx, JsonItem rootItem) throws JsonBuildException {
        BuilderService builderService = new BuilderService(model, throwClassEx);
        return builderService.build(rootItem);
    }

    /**
     * Constructs a JsonBuilder instance with the specified root JSON item.
     *
     * @param rootItem The root JSON item to build from.
     */
    public JsonBuilder(JsonItem rootItem) {
        this.rootItem = rootItem;
        this.builderService = null;
    }

    /**
     * Builds a Java object instance from the root JSON item using the specified model.
     *
     * @param model The JSON model containing type definitions and mappings.
     * @param throwClassEx If true, throws exceptions when classes are not found;
     *        if false, uses default handling.
     * @return The constructed Java object.
     * @throws JsonBuildException If object construction fails.
     */
    public Object buildInstance(JsonModel model, boolean throwClassEx) throws JsonBuildException {
        builderService = new BuilderService(model, throwClassEx);
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
     * Returns the BuilderService used by this builder.
     *
     * @return The BuilderService instance, or null if not yet initialized.
     */
    public BuilderService getBuilderService() {
        return builderService;
    }

}
