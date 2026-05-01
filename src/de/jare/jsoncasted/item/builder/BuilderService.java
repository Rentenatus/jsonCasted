/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonInter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class responsible for building Java objects from JSON structures.
 *
 * <p>The BuilderService coordinates the object construction process by:</p>
 * <ul>
 *   <li>Managing the JSON model and type registry</li>
 *   <li>Tracking built objects by wood key for reference resolution</li>
 *   <li>Dispatching build requests to appropriate builders based on type information</li>
 *   <li>Handling object caching to prevent duplicate instantiation</li>
 * </ul>
 *
 * <p>This service is the central component in the object building pipeline, connecting
 * parsed JSON structures ({@link JsonItem}) with actual Java object instantiation.</p>
 */
public class BuilderService {

    private final JsonModel model;
    private final boolean throwClassEx;
    private final Map<String, Object> builtObjectsByWoodKey = new HashMap<>();

    /**
     * Constructs a BuilderService with the specified model and exception configuration.
     *
     * @param model the JSON model containing type definitions.
     * @param throwClassEx if {@code true}, throws exceptions when unknown classes are encountered;
     *                   otherwise logs warnings and continues.
     */
    public BuilderService(JsonModel model, boolean throwClassEx) {
        this.model = model;
        this.throwClassEx = throwClassEx;
    }

    /**
     * Returns the JSON model used by this builder service.
     *
     * @return the JSON model.
     */
    public JsonModel getModel() {
        return model;
    }

    /**
     * Builds an object from the given JSON item.
     *
     * @param item the JSON item to build from.
     * @return the constructed object, or {@code null} if the item is null.
     * @throws JsonBuildException if object construction fails and throwClassEx is true.
     */
    public Object build(JsonItem item) throws JsonBuildException {
        return item == null ? null : item.buildInstance(this);
    }

    /**
     * Retrieves a previously built object by its wood key.
     *
     * @param woodKey the wood key identifying the object.
     * @return the cached object, or {@code null} if not found or key is null.
     */
    public Object getBuiltObject(String woodKey) {
        return woodKey == null ? null : builtObjectsByWoodKey.get(woodKey);
    }

    /**
     * Caches a built object with its wood key for future reference resolution.
     *
     * @param woodKey the wood key to associate with the object.
     * @param value the built object to cache.
     */
    public void putBuiltObject(String woodKey, Object value) {
        if (woodKey != null && value != null) {
            builtObjectsByWoodKey.put(woodKey, value);
        }
    }

    /**
     * Builds or retrieves an object from the cache using its wood key.
     *
     * <p>If the object has already been built and cached under the wood key,
     * the cached instance is returned. Otherwise, a new object is built and cached.</p>
     *
     * @param jsonObject the JSON object to build from.
     * @param contextClass the type descriptor for the target class.
     * @return the built or cached object.
     * @throws JsonBuildException if object construction fails and throwClassEx is true.
     */
    public Object getOrBuild(JsonObject jsonObject, JsonTypeDescriptor contextClass) throws JsonBuildException {
        String woodKey = jsonObject.getWoodKey();
        if (woodKey != null) {
            Object cached = builtObjectsByWoodKey.get(woodKey);
            if (cached != null) {
                return cached;
            }
        }

        Object built = buildObject(jsonObject, contextClass);
        if (woodKey != null && built != null) {
            builtObjectsByWoodKey.put(woodKey, built);
        }
        return built;
    }

    /**
     * Builds an object from a JSON value using the specified type descriptor.
     *
     * @param jsonValue the JSON item containing the value.
     * @param contextClass the type descriptor for the target class.
     * @return the constructed object, or {@code null} if the type is not found.
     * @throws JsonBuildException if object construction fails and throwClassEx is true.
     */
    public Object buildObject(JsonItem jsonValue, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType != null) {
            return jType.build(jsonValue, this);
        }
        JsonInter jInter = model.getJsonInter(typeName);
        if (jInter != null) {
            buildException("JsonClass " + typeName + " needs a casting.");
            return null;
        }
        buildException("JsonClass " + typeName + " is unknown.");
        return null;
    }

    /**
     * Builds a list or array from a JSON value using the specified type descriptor.
     *
     * @param jsonValue the JSON item containing the list/array.
     * @param asList if {@code true}, builds a List; otherwise builds an array.
     * @param contextClass the type descriptor for the target class.
     * @return the constructed list/array, or {@code null} if the type is not found.
     * @throws JsonBuildException if object construction fails and throwClassEx is true.
     */
    public Object buildList(JsonItem jsonValue, boolean asList, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType != null) {
            return jType.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
        }
        JsonClass jEnum = model.getJsonEnum(typeName);
        if (jEnum != null) {
            return jEnum.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
        }
        JsonInter jInter = model.getJsonInter(typeName);
        if (jInter != null) {
            return jInter.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
        }
        buildException("JsonClass " + typeName + " is unknown.");
        return null;
    }

    /**
     * Builds a primitive value from a JSON value using the specified type descriptor.
     *
     * @param jsonValue the JSON item containing the primitive value.
     * @param contextClass the type descriptor for the target class.
     * @return the constructed primitive value, or {@code null} if the type is not found.
     * @throws JsonBuildException if object construction fails and throwClassEx is true.
     */
    public Object buildValue(JsonItem jsonValue, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType != null) {
            return jType.build(jsonValue, this);
        }
        JsonClass jEnum = model.getJsonEnum(typeName);
        if (jEnum != null) {
            return jEnum.build(jsonValue, this);
        }
        buildException("JsonClass " + typeName + " is unknown.");
        return null;

    }

    /**
     * Logs a build error and optionally throws an exception.
     *
     * @param msg the error message to log.
     * @throws JsonBuildException if throwClassEx is true.
     */
    private void buildException(final String msg) throws JsonBuildException {
        Logger.getGlobal().log(Level.SEVERE, msg);
        if (throwClassEx) {
            throw new JsonBuildException(msg);
        }
    }
}
