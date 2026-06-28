/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.item.JsonField;

/**
 * Functional interface for validators that validate JsonField instances.
 *
 * @author Janusch Rentenatus
 */
@FunctionalInterface
public interface FieldValidator {

    /**
     * Validates the given JsonField.
     *
     * @param field the field to validate
     * @param context the validation context for reporting diagnostics
     */
    void validate(JsonField field, ValidationContext context);
}
