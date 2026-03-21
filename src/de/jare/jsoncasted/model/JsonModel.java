/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.builder.*;
import de.jare.jsoncasted.model.item.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * The JsonModel class serves as a registry for JSON classes and types. It
 * provides methods for managing JSON class definitions, including retrieval,
 * creation, and deletion.
 *
 * @author Janusch Rentenatus
 */
public class JsonModel {

    private final HashMap<String, JsonClass> classes;
    private final String mName;

    /**
     * Constructs a JsonModel instance with a specified model name.
     *
     * @param mName The name of the JSON model.
     */
    public JsonModel(String mName) {
        this.mName = mName;
        classes = new HashMap<>();
    }

    /**
     * Retrieves the model name.
     *
     * @return The name of the JSON model.
     */
    public String getmName() {
        return mName;
    }

    /**
     * Adds a JSON class to the model registry.
     *
     * @param jClass The JSON class to register.
     */
    public void add(JsonClass jClass) {
        classes.put(jClass.getcName(), jClass);
    }

    /**
     * Retrieves a JSON class by its name.
     *
     * @param key The name of the JSON class.
     * @return The corresponding JsonClass, or null if not found.
     */
    public JsonClass getJsonClass(String key) {
        return classes.get(key);
    }

    /**
     * Finds a JSON class whose name ends with the specified string.
     *
     * @param cNameEnding The suffix to match.
     * @return The matching JsonClass, or null if none found.
     */
    public JsonClass getEndsWith(String cNameEnding) {
        for (JsonClass jc : classes.values()) {
            if (jc.getcName().endsWith(cNameEnding)) {
                return jc;
            }
        }
        return null;
    }

    /**
     * Retrieves a JSON class by its corresponding Java class.
     *
     * @param clazz The Java class.
     * @return The matching JsonClass, or null if not found.
     */
    public JsonClass getJsonClass(Class<?> clazz) {
        return classes.get(clazz.getTypeName());
    }

    /**
     * Removes a JSON class from the model registry.
     *
     * @param jClass The JSON class to remove.
     * @return The removed JsonClass, or null if not found.
     */
    public JsonClass remove(JsonClass jClass) {
        return classes.remove(jClass.getcName());
    }

    /**
     * Returns an iterator over the registered JSON classes.
     *
     * @return An iterator over JsonClass objects.
     */
    public Iterator<JsonClass> classesIterator() {
        return classes.values().iterator();
    }

    /**
     * Retrieves the set of registered class names.
     *
     * @return A set of class names.
     */
    public Set<String> classesKeySet() {
        return classes.keySet();
    }

    /**
     * Populates the model with basic data types used in JSON processing.
     */
    public void addBasicModel() {
        add(new JsonClass("String", new JsonStringBuilder()));
        add(new JsonClass("Integer", new JsonIntegerObjBuilder()));
        add(new JsonClass("Long", new JsonLongObjBuilder()));
        add(new JsonClass("Float", new JsonFloatObjBuilder()));
        add(new JsonClass("Double", new JsonDoubleObjBuilder()));
        add(new JsonClass("Boolean", new JsonBooleanObjBuilder()));
        add(new JsonClass("int", new JsonIntBuilder()));
        add(new JsonClass("long", new JsonLongBuilder()));
        add(new JsonClass("float", new JsonFloatBuilder()));
        add(new JsonClass("double", new JsonDoubleBuilder()));
        add(new JsonClass("boolean", new JsonBooleanBuilder()));
    }

    // Methods for dynamically creating JSON class definitions
    public JsonClass newJsonClass(Class<?> clazz, JsonModellClassBuilder builder) {
        JsonClass ret = new JsonClass(clazz.getTypeName(), builder);
        add(ret);
        return ret;
    }

    public JsonClass newJsonReflect(Class<?> clazz) {
        JsonClass ret = new JsonClass(clazz.getTypeName(), new JsonReflectBuilder(clazz));
        add(ret);
        return ret;
    }

    public JsonClass newJsonReflect(Class<?> clazz, boolean skippingNulls) {
        JsonClass ret = new JsonClass(clazz.getTypeName(), skippingNulls, new JsonReflectBuilder(clazz));
        add(ret);
        return ret;
    }

    public JsonClass newJsonReflect(Class<?> clazz, JsonClass parent) {
        JsonClass ret = newJsonReflect(clazz);
        ret.addFromSuperclass(parent);
        return ret;
    }

    public JsonClass newJsonReflect(Class<?> clazz, JsonClass parent, boolean skippingNulls) {
        JsonClass ret = newJsonReflect(clazz, skippingNulls);
        ret.addFromSuperclass(parent);
        return ret;
    }

    public JsonClass newJsonEnumByName(Class<?> clazz) {
        return new JsonClass(clazz.getTypeName(), new JsonEnumByNameBuilder(clazz));
    }

    public JsonInter newJsonInterface(Class<?> clazz, JsonClass... jClass) {
        return new JsonInter(clazz.getTypeName(), new JsonReflectBuilder(clazz), jClass);
    }

    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, JsonClass itemClass, JsonCollectionType type) {
        JsonMap ret = new JsonMap(clazz.getTypeName(), clazz, itemClass, type);
        add(ret);
        return ret;
    }

    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, boolean skippingNulls, JsonClass itemClass, JsonCollectionType type) {
        JsonMap ret = new JsonMap(clazz.getTypeName(), skippingNulls, clazz, itemClass, type);
        add(ret);
        return ret;
    }

    public JsonMap newRawJsonMap(Class<? extends JsonInstance> clazz, JsonClass itemClass) {
        return newJsonMap((Class<? extends JsonInstance<?>>) clazz, itemClass, JsonCollectionType.NONE);
    }

    public JsonMap newRawJsonMap(Class<? extends JsonInstance> clazz, boolean skippingNulls, JsonClass itemClass) {
        return newJsonMap((Class<? extends JsonInstance<?>>) clazz, skippingNulls, itemClass, JsonCollectionType.NONE);
    }

    public JsonMap newRawJsonMap(Class<? extends JsonInstance> clazz, JsonClass itemClass, JsonCollectionType type) {
        return newJsonMap((Class<? extends JsonInstance<?>>) clazz, itemClass, type);
    }

    public JsonMap newRawJsonMap(Class<? extends JsonInstance> clazz, boolean skippingNulls, JsonClass itemClass, JsonCollectionType type) {
        return newJsonMap((Class<? extends JsonInstance<?>>) clazz, skippingNulls, itemClass, type);
    }

}
