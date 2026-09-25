/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.validation.core;

/**
 * Base interface for validation diagnostics. Defines the properties every diagnostic carries, regardless of which
 * project produces it, so that generic result containers can be shared across projects.
 *
 * @author Janusch Rentenatus
 */
public interface Diagnostic {

    /**
     * Returns the severity level of this diagnostic.
     *
     * @return the severity
     */
    Severity getSeverity();

    /**
     * Returns the diagnostic code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Returns the diagnostic message.
     *
     * @return the message
     */
    String getMessage();

    /**
     * Returns a single-line, human-readable rendering of this diagnostic for pretty printing.
     *
     * @return the rendering, never null
     */
    default String prettyLine() {
        return "[" + getSeverity() + "] " + getCode() + ": " + getMessage();
    }
}
