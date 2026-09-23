/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.reflectdefault;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.ReflectionService;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import de.jare.jsoncasted.model.validation.TypeValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

/**
 * Validator that compares the declared collection type of a field with the return type of the field's getter. The
 * reflective builder builds lists and arrays differently, so a mismatch between the declared LIST/ARRAY flag and the
 * actual Java property type breaks building or writing. Fields typed as maps are skipped because their collection
 * flag describes the value type of the map, not the property type.
 *
 * @author Janusch Rentenatus
 */
public class CollectionTypeMatchValidator implements TypeValidator {

    /**
     * Diagnostic code for a field declared as LIST whose getter returns an array.
     */
    public static final String LIST_MISMATCH_CODE = "reflection.collection.listMismatch";

    /**
     * Diagnostic code for a field declared as ARRAY whose getter returns a collection.
     */
    public static final String ARRAY_MISMATCH_CODE = "reflection.collection.arrayMismatch";

    /**
     * Diagnostic code for a field without collection declaration whose getter returns a collection or array.
     */
    public static final String NONE_MISMATCH_CODE = "reflection.collection.noneMismatch";

    @Override
    public void validate(JsonType type, ValidationContext context) {
        final Class<?> clazz = ReflectionService.resolveSingular(type);
        final JsonClass jClass = type.getDirectClass();
        if (clazz == null || jClass == null) {
            return;
        }
        final Iterator<JsonField> it = jClass.fieldsIterator();
        while (it.hasNext()) {
            final JsonField field = it.next();
            if (field.getjType() instanceof JsonMap) {
                continue;
            }
            final Method getter = ReflectionService.findGetter(clazz, field);
            if (getter == null) {
                continue;
            }
            final Class<?> returnType = getter.getReturnType();
            if (field.isAsList() && returnType.isArray()) {
                context.error(LIST_MISMATCH_CODE,
                        "Field '" + field.getfName() + "' is declared as LIST but its getter returns the array type "
                        + returnType.getTypeName() + " on class " + clazz.getName() + ".", field);
            } else if (field.isAsArray() && Collection.class.isAssignableFrom(returnType)) {
                context.error(ARRAY_MISMATCH_CODE,
                        "Field '" + field.getfName() + "' is declared as ARRAY but its getter returns the collection "
                        + "type " + returnType.getTypeName() + " on class " + clazz.getName() + ".", field);
            } else if (!field.isAsListOrArray()
                    && (returnType.isArray() || Collection.class.isAssignableFrom(returnType))) {
                context.warning(NONE_MISMATCH_CODE,
                        "Field '" + field.getfName() + "' declares no collection type but its getter returns "
                        + returnType.getTypeName() + " on class " + clazz.getName() + ".", field);
            }
        }
    }
}
