/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.validation.model;
import de.jare.jsoncasted.validation.core.Severity;
import de.jare.jsoncasted.validation.core.Diagnostic;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonField;

/**
 * Represents a single validation diagnostic message with severity, code, message, and source.
 *
 * @author Janusch Rentenatus
 */
public class ValidationDiagnostic implements Diagnostic {

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
    @Override
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Returns the diagnostic code.
     *
     * @return the code
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * Returns the diagnostic message.
     *
     * @return the message
     */
    @Override
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

    /**
     * Returns a single-line rendering with the source resolved to a readable form: types as their canonical name,
     * fields as their field name, other objects via their own toString.
     *
     * @return the rendering, never null
     */
    @Override
    public String prettyLine() {
        return "[" + severity + "] " + code + ": " + message
                + " (source: " + describeSource(source) + ")";
    }

    /**
     * Renders a diagnostic source in a readable form.
     *
     * @param source the source object of a diagnostic, may be null
     * @return the readable rendering, never null
     */
    private static String describeSource(Object source) {
        if (source instanceof JsonType type) {
            return "type '" + type.getcName() + "'";
        }
        if (source instanceof JsonField field) {
            return "field '" + field.getfName() + "'";
        }
        return source == null ? "?" : source.toString();
    }

    @Override
    public String toString() {
        return prettyLine();
    }
}
