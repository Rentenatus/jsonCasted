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
 * Validator that checks if a field is missing its type declaration.
 * Fields that are not ATTRIBUTE or REFERENCE kind must have a declared type.
 * 
 * @author Janusch Rentenatus
 */
public class FieldTypeMissingValidator implements FieldValidator {

    /**
     * Diagnostic code for missing field type errors.
     */
    public static final String MISSING_FIELD_TYPE_CODE = "field.type.missing";

    @Override
    public void validate(JsonField field, ValidationContext context) {
        if (field.getjType() == null && 
            field.getKind() != FieldKind.ATTRIBUTE && 
            field.getKind() != FieldKind.REFERENCE) {
            context.error(MISSING_FIELD_TYPE_CODE, "Field must declare a type.", field);
        }
    }
}
