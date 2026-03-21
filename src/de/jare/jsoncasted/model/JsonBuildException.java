/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

/**
 * The JsonBuildException class represents exceptions occurring during JSON
 * object creation. It provides constructors for error reporting and chaining
 * causes.
 *
 * @author Janusch Rentenatus
 */
public class JsonBuildException extends Exception {

    /**
     * Constructs a JsonBuildException with a specified error message.
     *
     * @param message The error message describing the issue.
     */
    public JsonBuildException(String message) {
        super(message);
    }

    /**
     * Constructs a JsonBuildException with a specified error message and cause.
     *
     * @param message The error message describing the issue.
     * @param cause The underlying cause of the exception.
     */
    public JsonBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
