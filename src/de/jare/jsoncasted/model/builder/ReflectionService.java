/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.builder;

import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonField;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Static service that bundles the reflection lookups shared by the reflective builder and the model validators. The
 * lookup semantics (method name and parameter count, constructor arity, primitive wrapper handling) are exactly the
 * ones {@link JsonReflectBuilder} applies while building objects, so validators report what would fail at build time.
 *
 * @author Janusch Rentenatus
 */
public final class ReflectionService {

    /**
     * Maps primitive types to their wrapper classes, as used by the constructor parameter compatibility check.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_MAP = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            double.class, Double.class,
            float.class, Float.class,
            int.class, Integer.class,
            long.class, Long.class,
            short.class, Short.class
    );

    private ReflectionService() {
    }

    /**
     * Finds the getter method of the given field on the given class. A getter is a public method with the field's
     * getter name and no parameters.
     *
     * @param target the class to search
     * @param field the field whose getter name is used
     * @return the getter method, or null if none exists
     */
    public static Method findGetter(Class<?> target, JsonField field) {
        return findMethod(target, field.getGetter(), 0);
    }

    /**
     * Finds the setter method of the given field on the given class. A setter is a public method with the field's
     * setter name and exactly one parameter.
     *
     * @param target the class to search
     * @param field the field whose setter name is used
     * @return the setter method, or null if none exists
     */
    public static Method findSetter(Class<?> target, JsonField field) {
        return findMethod(target, field.getSetter(), 1);
    }

    /**
     * Finds the first public method with the given name and parameter count.
     *
     * @param target the class to search
     * @param name the method name, may be null
     * @param parameterCount the required parameter count
     * @return the method, or null if none exists or name is null
     */
    public static Method findMethod(Class<?> target, String name, int parameterCount) {
        if (target == null || name == null) {
            return null;
        }
        for (Method meth : target.getMethods()) {
            if (meth.getName().equals(name) && meth.getParameterCount() == parameterCount) {
                return meth;
            }
        }
        return null;
    }

    /**
     * Returns all public constructors of the given class with the given parameter count. This mirrors the arity
     * filter of the reflective constructor search.
     *
     * @param target the class to search
     * @param parameterCount the required parameter count
     * @return list of matching constructors, never null
     */
    public static List<Constructor<?>> findConstructors(Class<?> target, int parameterCount) {
        List<Constructor<?>> ret = new ArrayList<>();
        if (target == null) {
            return ret;
        }
        for (Constructor<?> cons : target.getConstructors()) {
            if (cons.getParameterCount() == parameterCount) {
                ret.add(cons);
            }
        }
        return ret;
    }

    /**
     * Checks whether an instance is compatible with a declared parameter type, applying the same primitive wrapper
     * fallback as the reflective constructor search. A null instance is only compatible when explicitly allowed.
     *
     * @param declaredType the declared parameter type
     * @param instance the instance to check
     * @param nullAllowed whether a null instance is acceptable
     * @return true if the instance is compatible
     */
    public static boolean isInstanceCompatible(Class<?> declaredType, Object instance, boolean nullAllowed) {
        if (instance == null) {
            return nullAllowed;
        }
        if (declaredType.isInstance(instance)) {
            return true;
        }
        final Class<?> wrapper = PRIMITIVE_WRAPPER_MAP.get(declaredType);
        return wrapper != null && wrapper.isInstance(instance);
    }

    /**
     * Returns the wrapper class of a primitive type, or the type itself if it is not primitive.
     *
     * @param type the type to wrap
     * @return the wrapper class or the type itself
     */
    public static Class<?> toWrapper(Class<?> type) {
        return PRIMITIVE_WRAPPER_MAP.getOrDefault(type, type);
    }

    /**
     * Resolves the singular class of the given JSON type. Returns null instead of throwing when the type has no
     * direct class or the class cannot be loaded, so validators can skip such types gracefully.
     *
     * @param type the JSON type
     * @return the singular class, or null if unresolvable
     */
    public static Class<?> resolveSingular(JsonType type) {
        if (type == null || type.getDirectClass() == null) {
            return null;
        }
        try {
            return type.getDirectClass().createOrGetSing();
        } catch (JsonBuildException | RuntimeException ex) {
            return null;
        }
    }
}
