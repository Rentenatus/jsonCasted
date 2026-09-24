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
import java.util.Iterator;

/**
 * Validator that checks for each field of a resolvable type that the declared getter and setter methods actually
 * exist on the Java class, using the same lookup semantics as the reflective builder. A missing setter fails at
 * build time with a JsonBuildException; a missing getter makes reading and build-time validation fail silently.
 *
 * @author Janusch Rentenatus
 */
public class GetterSetterValidator implements TypeValidator {

    /**
     * Diagnostic code for missing setter methods.
     */
    public static final String SETTER_MISSING_CODE = "reflection.setter.missing";

    /**
     * Diagnostic code for missing getter methods.
     */
    public static final String GETTER_MISSING_CODE = "reflection.getter.missing";

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
            final String setter = field.getSetter();
            if (!field.isConstructorParam()
                    && setter != null && !setter.isBlank() && ReflectionService.findSetter(clazz, field) == null) {
                context.error(SETTER_MISSING_CODE,
                        "Setter '" + setter + "' of field '" + field.getfName()
                        + "' not found on class " + clazz.getName() + ".", field);
            }
            final String getter = field.getGetter();
            if (getter != null && !getter.isBlank() && ReflectionService.findGetter(clazz, field) == null) {
                context.warning(GETTER_MISSING_CODE,
                        "Getter '" + getter + "' of field '" + field.getfName()
                        + "' not found on class " + clazz.getName()
                        + " - reading the attribute and build-time validation will fail silently.", field);
            }
        }
    }
}
