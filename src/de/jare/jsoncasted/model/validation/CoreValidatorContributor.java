/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.validation.defdefault.DuplicateChildNameValidator;
import de.jare.jsoncasted.model.validation.defdefault.EmptyDefinitionsNameValidator;
import de.jare.jsoncasted.model.validation.modeldefault.DuplicateClassNameValidator;
import de.jare.jsoncasted.model.validation.fielddefault.EmptyFieldNameValidator;
import de.jare.jsoncasted.model.validation.typedefault.EmptyTypeNameValidator;
import de.jare.jsoncasted.model.validation.fielddefault.FieldTypeMissingValidator;
import de.jare.jsoncasted.model.validation.fielddefault.ReferenceFieldMissingTypeValidator;
import de.jare.jsoncasted.model.validation.fielddefault.UnregisteredReferenceTypeValidator;

/**
 * Default contributor that registers core validation rules for the JsonModel. These rules check for common issues like:
 * <ul>
 * <li>Empty or duplicate type names</li>
 * <li>Fields without types</li>
 * <li>Reference fields without target types</li>
 * <li>Empty field names</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class CoreValidatorContributor implements ValidatorContributor {

    @Override
    public void contribute(ValidatorRegistry registry) {
        // Model-level validators
        registry.addModelValidator(new DuplicateClassNameValidator());

        // Type-level validators
        registry.addTypeValidator(new EmptyTypeNameValidator());

        // Field-level validators
        registry.addFieldValidator(new FieldTypeMissingValidator());
        registry.addFieldValidator(new ReferenceFieldMissingTypeValidator());
        registry.addFieldValidator(new EmptyFieldNameValidator());

        // Definitions-level validators
        registry.addDefinitionsValidator(new EmptyDefinitionsNameValidator());
        registry.addDefinitionsValidator(new DuplicateChildNameValidator());

        // Field-level validators related to definitions
        registry.addFieldValidator(new UnregisteredReferenceTypeValidator());
    }
}
