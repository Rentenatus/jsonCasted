/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.def;

import de.jare.jsoncasted.model.FieldKind;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.validation.FieldValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;

/**
 * Validator that checks if a reference field is missing its target type declaration.
 * Reference fields must declare which type they reference.
 * 
 * @author Janusch Rentenatus
 */
public class ReferenceFieldMissingTypeValidator implements FieldValidator {

    /**
     * Diagnostic code for reference field missing target type errors.
     */
    public static final String REFERENCE_TYPE_MISSING_CODE = "field.reference.type.missing";

    @Override
    public void validate(JsonField field, ValidationContext context) {
        if (field.getKind() == FieldKind.REFERENCE && field.getjType() == null) {
            context.error(REFERENCE_TYPE_MISSING_CODE, "Reference field must declare a target type.", field);
        }
    }
}
