/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.item.JsonDefinitions;

/**
 * Functional interface for validators that validate JsonDefinitions instances.
 * 
 * @author Janusch Rentenatus
 */
@FunctionalInterface
public interface DefinitionsValidator {

    /**
     * Validates the given JsonDefinitions.
     * 
     * @param definitions the definitions to validate
     * @param context the validation context for reporting diagnostics
     */
    void validate(JsonDefinitions definitions, ValidationContext context);
}
