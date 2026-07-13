/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

/**
 * Exception thrown when JSON writing fails.
 *
 * <p>
 * This exception is used to signal errors during JSON serialization, including: 
 * missing getter methods, type mismatches, and invalid object structures.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonWriteException extends Exception {

    /**
     * Constructs a JsonWriteException with a specified error message.
     *
     * @param message the error message describing the writing failure.
     */
    public JsonWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a JsonWriteException with a specified error message and line number.
     *
     * @param lineNumber line number
     * @param message the error message describing the writing failure.
     */
    public JsonWriteException(int lineNumber, String message) {
        super("[" + lineNumber + "] " + message);
    }

    /**
     * Constructs a JsonWriteException with a specified error message and cause.
     *
     * @param message the error message describing the writing failure.
     * @param cause the underlying cause of the exception.
     */
    public JsonWriteException(String message, Throwable cause) {
        super(message, cause);
    }

}
