/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModellClassBuilder;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JsonReflectBuilder class is responsible for constructing Java objects
 * from JSON data using reflection. It dynamically invokes constructors and
 * setter methods to populate object fields.
 *
 * @author Janusch Rentenatus
 */
public class JsonReflectBuilder implements JsonModellClassBuilder {

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            double.class, Double.class,
            float.class, Float.class,
            int.class, Integer.class,
            long.class, Long.class,
            short.class, Short.class
    );

    private Class<?> singular;

    /**
     * Default constructor for JsonReflectBuilder.
     */
    public JsonReflectBuilder() {
    }

    /**
     * Constructs a JsonReflectBuilder instance for a specific class.
     *
     * @param singular The target class for reflection-based object creation.
     */
    public JsonReflectBuilder(Class<?> singular) {
        this.singular = singular;
    }

    /**
     * Returns the singular class type associated with this builder.
     *
     * @return The class type.
     */
    @Override
    public Class<?> getSingularClass() {
        return singular;
    }

    /**
     * Builds an object from a JSON item using reflection.
     *
     * @param jClass The JSON class definition.
     * @param jsonItem The JSON item containing field values.
     * @return The constructed object.
     * @throws JsonBuildException If object creation fails.
     */
    @Override
    public Object build(JsonClass jClass, JsonItem jsonItem) throws JsonBuildException {
        if (singular == null) {
            singular = jClass.createOrGetSing();
            if (singular == null) {
                return null;
            }
        }
        Object ob = createInstance(jClass, jsonItem);
        Iterator<String> it = jClass.keysForBuildIterator();
        while (it.hasNext()) {
            try {
                JsonField next = jClass.get(it.next());
                if (next.isConstructorParam()) {
                    continue;
                }
                JsonItem para = jsonItem.getParam(next.getfName());
                if (para != null) {
                    Object inst = para.buildInstance();
                    Method getterMeth = null;
                    Method setterMeth = null;
                    boolean suchtNoch = true;
                    for (Method meth : singular.getMethods()) {
                        if (meth.getName().equals(next.getGetter()) && meth.getParameterCount() == 0) {
                            getterMeth = meth;
                            if (setterMeth != null) {
                                break;
                            }
                        }
                        if (meth.getName().equals(next.getSetter()) && meth.getParameterCount() == 1) {
                            setterMeth = meth;
                            try {
                                meth.invoke(ob, inst);
                            } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                                throw new JsonBuildException("Exception invoking " + jClass.getcName() + "." + meth.getName() + "(" + para.getPrintClassName() + " " + next.getfName() + ") :" + ex.getMessage(), ex);
                            }
                            suchtNoch = false;
                            if (getterMeth != null || !next.satisfyValidation()) {
                                break;
                            }
                        }
                    }
                    if (suchtNoch) {
                        throw new JsonBuildException("Method not found: " + jClass.getcName() + "." + next.getSetter() + "(" + para.getPrintClassName() + " " + next.getfName() + ")");
                    }
                    if (getterMeth != null && next.satisfyValidation()) {
                        try {
                            Object target = getterMeth.invoke(ob);
                            if (!next.validate(inst, target)) {
                                throw new JsonBuildException("ValidationException invoking " + jClass.getcName() + "." + setterMeth.getName() + "(" + para.getPrintClassName() + " " + next.getfName()
                                        + "): Expected '" + inst + "', found '" + target + "'");
                            }
                        } catch (ReflectiveOperationException | IllegalArgumentException ex) {
                            throw new JsonBuildException("Exception invoking " + jClass.getcName() + "." + getterMeth.getName() + "() :" + ex.getMessage(), ex);
                        }
                    }
                }
            } catch (SecurityException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
                throw new JsonBuildException(ex.getMessage(), ex);
            }
        }
        return ob;
    }

    /**
     * Creates an instance of the target class using reflection.
     *
     * @param jClass The JSON class definition.
     * @param jsonItem The JSON item containing constructor parameters.
     * @return The instantiated object.
     * @throws JsonBuildException If instantiation fails.
     */
    protected Object createInstance(JsonClass jClass, JsonItem jsonItem) throws JsonBuildException {
        Object ob;
        ArrayList<JsonField> params = new ArrayList<>();
        Iterator<String> it = jClass.keysForBuildIterator();
        while (it.hasNext()) {
            JsonField next = jClass.get(it.next());
            if (next.isConstructorParam()) {
                params.add(next);
            }
        }
        try {
            ob = params.isEmpty() ? useDeclaredConstructor() : useConstructorWith(jClass, params, jsonItem);
        } catch (NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            throw new JsonBuildException("Calling the constructor of "
                    + singular.getSimpleName() + " failed.", ex);
        }
        return ob;
    }

    protected Object useDeclaredConstructor() throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, IllegalArgumentException, NoSuchMethodException {
        return singular.getDeclaredConstructor().newInstance();
    }

    protected Object useConstructorWith(JsonClass jClass, ArrayList<JsonField> params, JsonItem jsonItem) throws SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, IllegalArgumentException, NoSuchMethodException, JsonBuildException {
        ArrayList<Object> paramObjects = calculateParamObjects(params, jsonItem, jClass);

        Constructor<?> constructor = calculateConstructor(params, paramObjects);

        if (constructor == null) {
            throwConstructorException(params);
        }
        return constructor.newInstance(paramObjects.toArray());
    }

    protected ArrayList<Object> calculateParamObjects(ArrayList<JsonField> params, JsonItem jsonItem, JsonClass jClass) throws JsonBuildException {
        ArrayList<Object> paramObjects = new ArrayList<>(params.size());
        for (JsonField next : params) {
            try {
                JsonItem para = jsonItem.getParam(next.getfName());
                if (para == null) {
                    if (jClass.isSkippingNulls()) {
                        paramObjects.add(null);
                        continue;
                    }
                    throw new JsonBuildException("Item '" + next.getfName() + "' not found but is necessary for the constructor of class " + jClass.getcName() + '.');
                }
                paramObjects.add(para.buildInstance());
            } catch (JsonBuildException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
                throw new JsonBuildException(ex.getMessage(), ex);
            }
        }
        return paramObjects;
    }

    protected Constructor<?> calculateConstructor(ArrayList<JsonField> params, ArrayList<Object> paramObjects) throws SecurityException {
        Constructor<?> constructor = null;
        for (Constructor<?> cons : singular.getConstructors()) {
            if (constructor != null) {
                break;
            }
            if (cons.getParameterCount() != params.size()) {
                continue;
            }
            boolean okay = true;
            Class<?>[] types = cons.getParameterTypes();
            for (int i = 0; okay && i < types.length; i++) {
                okay = types[i].isInstance(paramObjects.get(i));
                if (!okay && primitiveWrapperMap.get(types[i]) != null) {
                    okay = primitiveWrapperMap.get(types[i]).isInstance(paramObjects.get(i));
                }
            }
            if (okay) {
                constructor = cons;
            }
        }
        return constructor;
    }

    protected void throwConstructorException(ArrayList<JsonField> params) throws JsonBuildException {
        if (params.isEmpty()) {
            throw new JsonBuildException("Constructor of " + singular.getSimpleName() + " without params not found.");
        }
        StringBuffer paramsString = new StringBuffer();
        Iterator<JsonField> it = params.iterator();
        while (it.hasNext()) {
            paramsString.append(it.next().getfName());
            if (!it.hasNext()) {
                break;
            }
            paramsString.append(", ");
        }
        throw new JsonBuildException("Constructor of " + singular.getSimpleName() + " with " + params.size() + " params (" + paramsString + ") not found.");
    }

    /**
     * Builds a list of objects from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return A list of constructed objects.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public List buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        ArrayList list = new ArrayList(size);
        while (listIterator.hasNext()) {
            JsonItem next = listIterator.next();
            Object elem = next.buildInstance();
            list.add(elem);
        }
        return list;
    }

    /**
     * Builds an array of objects from a JSON array.
     *
     * @param jType The JSON type for conversion.
     * @param listIterator Iterator over JSON items.
     * @param size The expected size of the array.
     * @return An array of constructed objects.
     * @throws JsonBuildException If an error occurs during conversion.
     */
    @Override
    public Object[] buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
        return buildList(jType, listIterator, size).toArray();
    }

    /**
     * Not supported for this builder.
     *
     * @param attr Object
     * @return none
     * @throws UnsupportedOperationException
     */
    @Override
    public String toString(Object attr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Determines whether the JSON type represents a primitive value. Since
     * objects created via reflection are inherently complex structures, this
     * method always returns false.
     *
     * @return false, indicating that reflection-based objects are not
     * primitive.
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public List<?> asList(Object ob) {
        return Arrays.asList((Object[]) ob);
    }
}
