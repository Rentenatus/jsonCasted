/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

/**
 * Exception thrown when JSON parsing fails.
 *
 * <p>This exception is used to signal errors during JSON parsing, including:
 * syntax errors, type mismatches, missing required fields, and invalid JSON structures.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonParseException extends Exception {

    /**
     * Constructs a JsonParseException with a specified error message.
     *
     * @param message the error message describing the parsing failure.
     */
    public JsonParseException(String message) {
        super(message);
    }

    /**
     * Constructs a JsonParseException with a specified error message and cause.
     *
     * @param message the error message describing the parsing failure.
     * @param cause the underlying cause of the exception.
     */
    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
