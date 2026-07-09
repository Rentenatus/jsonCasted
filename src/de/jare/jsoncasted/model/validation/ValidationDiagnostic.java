/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

/**
 * Represents a single validation diagnostic message with severity, code, message, and source.
 *
 * @author Janusch Rentenatus
 */
public class ValidationDiagnostic {

    private final Severity severity;
    private final String code;
    private final String message;
    private final Object source;

    /**
     * Creates a new validation diagnostic.
     *
     * @param severity the severity level
     * @param code the diagnostic code
     * @param message the diagnostic message
     * @param source the source object that caused the diagnostic
     */
    public ValidationDiagnostic(Severity severity, String code, String message, Object source) {
        this.severity = severity;
        this.code = code;
        this.message = message;
        this.source = source;
    }

    /**
     * Returns the severity level of this diagnostic.
     *
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns the diagnostic code.
     *
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the diagnostic message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the source object that caused this diagnostic.
     *
     * @return the source object
     */
    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + code + ": " + message + " (source: " + source + ")";
    }
}
