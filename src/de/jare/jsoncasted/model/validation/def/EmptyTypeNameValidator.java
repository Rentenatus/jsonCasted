/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.def;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.validation.TypeValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;

/**
 * Validator that checks if a type has an empty or blank canonical name.
 * Types without proper names cannot be properly identified or referenced.
 * 
 * @author Janusch Rentenatus
 */
public class EmptyTypeNameValidator implements TypeValidator {

    /**
     * Diagnostic code for empty type name errors.
     */
    public static final String EMPTY_TYPE_NAME_CODE = "type.name.blank";

    @Override
    public void validate(JsonType type, ValidationContext context) {
        if (type.getcName() == null || type.getcName().isBlank()) {
            context.error(EMPTY_TYPE_NAME_CODE, "Type canonical name must not be blank.", type);
        }
    }
}
