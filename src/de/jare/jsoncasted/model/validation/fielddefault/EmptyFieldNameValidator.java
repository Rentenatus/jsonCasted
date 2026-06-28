/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.fielddefault;

import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.validation.FieldValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;

/**
 * Validator that checks if a field has an empty or blank name. All fields must have a proper name for identification
 * and serialization.
 *
 * @author Janusch Rentenatus
 */
public class EmptyFieldNameValidator implements FieldValidator {

    /**
     * Diagnostic code for empty field name errors.
     */
    public static final String EMPTY_FIELD_NAME_CODE = "field.name.blank";

    @Override
    public void validate(JsonField field, ValidationContext context) {
        if (field.getfName() == null || field.getfName().isBlank()) {
            context.error(EMPTY_FIELD_NAME_CODE, "Field name must not be blank.", field);
        }
    }
}
