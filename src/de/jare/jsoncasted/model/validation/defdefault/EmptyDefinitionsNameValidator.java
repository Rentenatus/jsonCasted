/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.defdefault;

import de.jare.jsoncasted.model.item.JsonDefinitions;
import de.jare.jsoncasted.model.validation.DefinitionsValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;

/**
 * Validator that checks if a definitions node has an empty or blank name. Definitions without proper names cannot be
 * properly identified in the hierarchy.
 *
 * @author Janusch Rentenatus
 */
public class EmptyDefinitionsNameValidator implements DefinitionsValidator {

    /**
     * Diagnostic code for empty definitions name errors.
     */
    public static final String EMPTY_DEFINITIONS_NAME_CODE = "definitions.name.blank";

    @Override
    public void validate(JsonDefinitions definitions, ValidationContext context) {
        if (definitions.getName() == null || definitions.getName().isBlank()) {
            context.error(EMPTY_DEFINITIONS_NAME_CODE, "Definitions name must not be blank.", definitions);
        }
    }
}
