/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.JsonModel;

/**
 * Holds the validation context including the current model, result, and provides convenience methods for adding
 * diagnostics.
 *
 * @author Janusch Rentenatus
 */
public class ValidationContext {

    private final JsonModel model;
    private final ValidationResult result;

    /**
     * Creates a new validation context.
     *
     * @param model the model being validated
     * @param result the result object to collect diagnostics
     */
    public ValidationContext(JsonModel model, ValidationResult result) {
        this.model = model;
        this.result = result;
    }

    /**
     * Returns the model being validated.
     *
     * @return the model
     */
    public JsonModel getModel() {
        return model;
    }

    /**
     * Returns the validation result.
     *
     * @return the result
     */
    public ValidationResult getResult() {
        return result;
    }

    /**
     * Adds an info-level diagnostic.
     *
     * @param code the diagnostic code
     * @param message the diagnostic message
     * @param source the source object
     */
    public void info(String code, String message, Object source) {
        result.add(new ValidationDiagnostic(Severity.INFO, code, message, source));
    }

    /**
     * Adds a warning-level diagnostic.
     *
     * @param code the diagnostic code
     * @param message the diagnostic message
     * @param source the source object
     */
    public void warning(String code, String message, Object source) {
        result.add(new ValidationDiagnostic(Severity.WARNING, code, message, source));
    }

    /**
     * Adds an error-level diagnostic.
     *
     * @param code the diagnostic code
     * @param message the diagnostic message
     * @param source the source object
     */
    public void error(String code, String message, Object source) {
        result.add(new ValidationDiagnostic(Severity.ERROR, code, message, source));
    }
}
