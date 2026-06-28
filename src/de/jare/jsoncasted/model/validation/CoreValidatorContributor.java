/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.FieldKind;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import java.util.HashSet;
import java.util.Set;

/**
 * Default contributor that registers core validation rules for the JsonModel.
 * These rules check for common issues like:
 * <ul>
 * <li>Empty or duplicate type names</li>
 * <li>Fields without types</li>
 * <li>Inconsistent constructor parameters</li>
 * <li>Reference fields without target types</li>
 * </ul>
 * 
 * @author Janusch Rentenatus
 */
public class CoreValidatorContributor implements ValidatorContributor {

    @Override
    public void contribute(ValidatorRegistry registry) {
        // Model-level validators
        registry.addModelValidator((model, ctx) -> {
            // Check for duplicate class names
            checkDuplicateClassNames(model, ctx);
        });

        // Type-level validators
        registry.addTypeValidator((type, ctx) -> {
            // Check for empty type canonical name
            if (type.getcName() == null || type.getcName().isBlank()) {
                ctx.error("type.name.blank", "Type canonical name must not be blank.", type);
            }
        });

        // Field-level validators
        registry.addFieldValidator((field, ctx) -> {
            // Check for field without type
            if (field.getjType() == null && field.getKind() != FieldKind.ATTRIBUTE && field.getKind() != FieldKind.REFERENCE) {
                ctx.error("field.type.missing", "Field must declare a type.", field);
            }

            // Check for reference field without target type
            if (field.getKind() == FieldKind.REFERENCE && field.getjType() == null) {
                ctx.error("field.reference.type.missing", "Reference field must declare a target type.", field);
            }

            // Check for empty field name
            if (field.getfName() == null || field.getfName().isBlank()) {
                ctx.error("field.name.blank", "Field name must not be blank.", field);
            }
        });
    }

    /**
     * Checks for duplicate class names in the model.
     * 
     * @param model the model to check
     * @param ctx the validation context
     */
    private void checkDuplicateClassNames(JsonModel model, ValidationContext ctx) {
        Set<String> classNames = new HashSet<>();
        Set<String> duplicateNames = new HashSet<>();

        for (String className : model.classesKeySet()) {
            JsonClass jc = model.getJsonClass(className);
            if (jc != null) {
                String name = jc.getcName();
                if (name != null && !name.isBlank()) {
                    if (classNames.contains(name)) {
                        duplicateNames.add(name);
                    } else {
                        classNames.add(name);
                    }
                }
            }
        }

        for (String duplicateName : duplicateNames) {
            ctx.error("class.name.duplicate", "Duplicate class name found: " + duplicateName, model);
        }
    }
}
