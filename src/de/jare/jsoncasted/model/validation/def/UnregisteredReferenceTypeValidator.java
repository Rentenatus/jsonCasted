/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.def;

import de.jare.jsoncasted.model.FieldKind;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.validation.FieldValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;

/**
 * Validator that checks if a reference field's target type is registered in the model.
 * Reference fields should point to types that exist in the model registry.
 * This is a warning-level diagnostic since the reference might be valid at runtime.
 * 
 * @author Janusch Rentenatus
 */
public class UnregisteredReferenceTypeValidator implements FieldValidator {

    /**
     * Diagnostic code for unregistered reference type warnings.
     */
    public static final String UNREGISTERED_REFERENCE_TYPE_CODE = "field.reference.type.unregistered";

    @Override
    public void validate(JsonField field, ValidationContext context) {
        if (field.getKind() == FieldKind.REFERENCE && field.getjType() != null) {
            JsonModel model = context.getModel();
            String targetTypeName = field.getjType().getcName();
            
            if (targetTypeName != null && 
                model.getJsonClass(targetTypeName) == null && 
                model.getJsonInter(targetTypeName) == null && 
                model.getJsonEnum(targetTypeName) == null) {
                context.warning(UNREGISTERED_REFERENCE_TYPE_CODE, 
                        "Reference field targets unregistered type: " + targetTypeName, 
                        field);
            }
        }
    }
}
