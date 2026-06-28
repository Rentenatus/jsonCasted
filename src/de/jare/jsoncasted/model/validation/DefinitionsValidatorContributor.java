/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.validation.def.DuplicateChildNameValidator;
import de.jare.jsoncasted.model.validation.def.EmptyDefinitionsNameValidator;
import de.jare.jsoncasted.model.validation.def.UnregisteredReferenceTypeValidator;

/**
 * Contributor that registers validation rules for JsonDefinitions.
 * These rules check for common issues in the definitions structure like:
 * <ul>
 * <li>Empty definitions names</li>
 * <li>Duplicate child names in the same scope</li>
 * <li>Reference fields targeting unregistered types</li>
 * </ul>
 * 
 * @author Janusch Rentenatus
 */
public class DefinitionsValidatorContributor implements ValidatorContributor {

    @Override
    public void contribute(ValidatorRegistry registry) {
        // Definitions-level validators
        registry.addDefinitionsValidator(new EmptyDefinitionsNameValidator());
        registry.addDefinitionsValidator(new DuplicateChildNameValidator());

        // Field-level validators related to definitions
        registry.addFieldValidator(new UnregisteredReferenceTypeValidator());
    }
}
