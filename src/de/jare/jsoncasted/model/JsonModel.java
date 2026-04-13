/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.builder.*;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private final HashMap<String, JsonInter> interfaces;
    private final String mName;
    private JsonModelDescriptor descriptor;

    /**
     * Constructs a JsonModel instance with a specified model name.
     *
     * @param mName The name of the JSON model.
     */
    public JsonModel(String mName) {
        this.mName = mName;
        this.descriptor = null;
        this.classes = new HashMap<>();
        this.interfaces = new HashMap<>();
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
        return classes.get(key.trim());
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
     * Retrieves a JSON interface by its name.
     *
     * @param key The name of the JSON interface.
     * @return The corresponding JsonInter, or null if not found.
     */
    public JsonInter getJsonInter(String key) {
        return interfaces.get(key.trim());
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
        add(new JsonClass("String", JsonNodeType.STRING, new JsonStringBuilder()));
        add(new JsonClass("Integer", JsonNodeType.LONG, new JsonIntegerObjBuilder()));
        add(new JsonClass("Long", JsonNodeType.LONG, new JsonLongObjBuilder()));
        add(new JsonClass("Float", JsonNodeType.NUMBER, new JsonFloatObjBuilder()));
        add(new JsonClass("Double", JsonNodeType.NUMBER, new JsonDoubleObjBuilder()));
        add(new JsonClass("Boolean", JsonNodeType.BOOLEAN, new JsonBooleanObjBuilder()));
        add(new JsonClass("int", JsonNodeType.LONG, new JsonIntBuilder()));
        add(new JsonClass("long", JsonNodeType.LONG, new JsonLongBuilder()));
        add(new JsonClass("float", JsonNodeType.NUMBER, new JsonFloatBuilder()));
        add(new JsonClass("double", JsonNodeType.NUMBER, new JsonDoubleBuilder()));
        add(new JsonClass("boolean", JsonNodeType.BOOLEAN, new JsonBooleanBuilder()));
    }

    // Methods for dynamically creating JSON class definitions
    public JsonClass newJsonClass(Class<?> clazz, JsonModellClassBuilder builder) {
        JsonClass ret = new JsonClass(clazz.getTypeName(), builder);
        add(ret);
        return ret;
    }

    public JsonClass newJsonClass(Class<?> clazz, JsonNodeType nodeType, JsonModellClassBuilder builder) {
        JsonClass ret = new JsonClass(clazz.getTypeName(), nodeType, builder);
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
        final JsonInter ret = new JsonInter(clazz.getTypeName(), new JsonReflectBuilder(clazz), jClass);
        interfaces.put(ret.getcName(), ret);
        return ret;
    }

    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, JsonClass itemClass, JsonCollectionType colType) {
        JsonMap ret = new JsonMap(clazz.getTypeName() + "<" + itemClass.getcName() + ">"
                + (colType == JsonCollectionType.NONE ? "" : "[]"), clazz, itemClass, colType);
        add(ret);
        return ret;
    }

    public JsonMap newJsonMap(Class<? extends JsonInstance<?>> clazz, boolean skippingNulls, JsonClass itemClass, JsonCollectionType colType) {
        JsonMap ret = new JsonMap(clazz.getTypeName() + "<" + itemClass.getcName() + ">"
                + (colType == JsonCollectionType.NONE ? "" : "[]"), skippingNulls, clazz, itemClass, colType);
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

    private List<JsonClass> getClassesList() {
        List<JsonClass> ordered = new ArrayList<>(classes.size());
        for (JsonClass jc : classes.values()) {
            if (!jc.getcName().contains(".")) {
                ordered.add(jc);
            }
        }
        for (JsonClass jc : classes.values()) {
            if (jc.getcName().contains(".")) {
                ordered.add(jc);
            }
        }
        return ordered;
    }

    private List<JsonInter> getOrderedInterfacesList() {
        List<JsonInter> ordered = new ArrayList<>(interfaces.values());
        ordered.sort(Comparator.comparing(JsonInter::getcName));
        return ordered;
    }

    public JsonModelDescriptor describe() {
        JsonModelDescriptor context = new JsonModelDescriptor(mName);
        List<JsonClass> orderedClasses = getClassesList();
        List<JsonInter> orderedInterfaces = getOrderedInterfacesList();

        for (JsonClass jsonClass : orderedClasses) {
            context.addType(jsonClass.describeHead(context));
        }

        for (JsonInter jsonInter : orderedInterfaces) {
            for (JsonClass next : jsonInter) {
                if (!context.containsType(next.getcName())) {
                    context.addType(next.describeHead(context));
                    orderedClasses.add(next);
                }
            }
            context.addType(jsonInter.describeHead(context));
        }

        for (JsonClass jsonClass : orderedClasses) {
            jsonClass.describeDependencies(context);
        }

        descriptor = context;
        return context;
    }

    public JsonModelDescriptor getOrCreateDescriptor() {
        if (descriptor != null) {
            return descriptor;
        }
        return describe();
    }

}
