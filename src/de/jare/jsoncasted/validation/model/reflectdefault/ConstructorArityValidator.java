/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.validation.model.reflectdefault;
import de.jare.jsoncasted.validation.core.Diagnostic;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.ReflectionService;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.validation.model.TypeValidator;
import de.jare.jsoncasted.validation.model.ValidationContext;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Validator that checks the constructor situation of a resolvable type against the declared constructor parameters.
 * If constructor parameters are declared, at least one public constructor must exist whose parameter count matches
 * and whose parameter types are compatible with the declared parameter types in the declared order - mirroring the
 * reflective constructor search. If no constructor parameters are declared, the class needs a declared no-arg
 * constructor, as used by the reflective default construction.
 *
 * @author Janusch Rentenatus
 */
public class ConstructorArityValidator implements TypeValidator {

    /**
     * Diagnostic code for a missing constructor with the declared parameter count.
     */
    public static final String CTOR_ARITY_CODE = "reflection.constructor.arity";

    /**
     * Diagnostic code for a constructor with the right parameter count but incompatible parameter types.
     */
    public static final String CTOR_SIGNATURE_CODE = "reflection.constructor.signature";

    /**
     * Diagnostic code for a missing declared no-arg constructor.
     */
    public static final String CTOR_DEFAULT_CODE = "reflection.constructor.default";

    @Override
    public void validate(JsonType type, ValidationContext context) {
        final Class<?> clazz = ReflectionService.resolveSingular(type);
        final JsonClass jClass = type.getDirectClass();
        if (clazz == null || jClass == null) {
            return;
        }
        if (type.isBoxOrPrimitive() || type.getValuesArray() != null) {
            return;
        }
        final List<JsonField> cParams = new ArrayList<>();
        final Iterator<String> keyIt = jClass.keysForBuildIterator();
        while (keyIt.hasNext()) {
            final JsonField next = jClass.getField(keyIt.next());
            if (next.isConstructorParam()) {
                cParams.add(next);
            }
        }
        if (cParams.isEmpty()) {
            try {
                clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException ex) {
                context.error(CTOR_DEFAULT_CODE,
                        "No declared no-arg constructor on class " + clazz.getName()
                        + " although no constructor parameters are declared.", type);
            }
            return;
        }
        final List<Constructor<?>> candidates = ReflectionService.findConstructors(clazz, cParams.size());
        if (candidates.isEmpty()) {
            context.error(CTOR_ARITY_CODE, "No public constructor with " + cParams.size()
                    + " parameters on class " + clazz.getName()
                    + " although " + cParams.size() + " constructor parameters are declared"
                    + " (" + paramNames(cParams) + ").", type);
            return;
        }
        if (!hasSignatureMatch(candidates, cParams)) {
            context.error(CTOR_SIGNATURE_CODE, "No public constructor with " + cParams.size()
                    + " parameters on class " + clazz.getName()
                    + " matches the declared constructor parameter types in order"
                    + " (" + paramNames(cParams) + ").", type);
        }
    }

    /**
     * Checks whether at least one of the arity-matching constructors is compatible with the declared parameter types.
     *
     * @param candidates the constructors with the right parameter count
     * @param cParams the declared constructor parameters in build order
     * @return true if at least one constructor matches
     */
    private boolean hasSignatureMatch(List<Constructor<?>> candidates, List<JsonField> cParams) {
        for (Constructor<?> cons : candidates) {
            if (matchesParameters(cons, cParams)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Statically mirrors the per-position instance check of the reflective constructor search: the constructor
     * parameter type must accept the instance type implied by the declared parameter. Positions whose declared type
     * cannot be resolved are skipped.
     *
     * @param cons the constructor to check
     * @param cParams the declared constructor parameters in build order
     * @return true if all positions are compatible
     */
    private boolean matchesParameters(Constructor<?> cons, List<JsonField> cParams) {
        final Class<?>[] paramTypes = cons.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            final JsonField param = cParams.get(i);
            final Class<?> declared = declaredInstanceType(param);
            if (declared == null || paramTypes[i] == Object.class) {
                continue;
            }
            if (param.isAsArray()) {
                if (!paramTypes[i].isArray()) {
                    return false;
                }
                continue;
            }
            if (!ReflectionService.toWrapper(paramTypes[i]).isAssignableFrom(declared)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the instance type implied by a declared parameter: lists build to java.util.List, arrays require an
     * array parameter, everything else resolves to the singular class of the declared type.
     *
     * @param param the declared constructor parameter
     * @return the implied instance type, or null if not resolvable
     */
    private Class<?> declaredInstanceType(JsonField param) {
        if (param.isAsList()) {
            return List.class;
        }
        return ReflectionService.resolveSingular(param.getjType());
    }

    /**
     * Joins the names of the declared constructor parameters in build order.
     *
     * @param cParams the declared constructor parameters
     * @return the comma-separated names
     */
    private static String paramNames(List<JsonField> cParams) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cParams.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(cParams.get(i).getfName());
        }
        return sb.toString();
    }
}
