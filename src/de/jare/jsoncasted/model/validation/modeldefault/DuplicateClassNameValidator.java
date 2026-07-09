/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.modeldefault;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.validation.ModelValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator that checks for duplicate class names in the model. If multiple classes have the same canonical name, an
 * error diagnostic is reported.
 *
 * @author Janusch Rentenatus
 */
public class DuplicateClassNameValidator implements ModelValidator {

    /**
     * Diagnostic code for duplicate class name errors.
     */
    public static final String DUPLICATE_CLASS_NAME_CODE = "class.name.duplicate";

    @Override
    public void validate(JsonModel model, ValidationContext context) {
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
            context.error(DUPLICATE_CLASS_NAME_CODE, "Duplicate class name found: " + duplicateName, model);
        }
    }
}
