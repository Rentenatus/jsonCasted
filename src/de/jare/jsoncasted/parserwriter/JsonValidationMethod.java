/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import java.util.function.BiPredicate;

/**
 * The JsonValidationMethod enum defines different validation methods for JSON
 * values. Each method represents a specific validation strategy.
 *
 * @author Janusch Rentenatus
 */
public enum JsonValidationMethod {

    /**
     * No validation – accepts all values.
     */
    NONE(0, (x, y) -> true),
    /**
     * Checks whether two objects are identical or considered equal.
     */
    EQUALS(2, (x, target) -> x == target || x.equals(target)),
    /**
     * Checks whether an object, as a string, appears at the end of a target
     * value.
     */
    ENDSWITH(4, (x, target) -> String.valueOf(target).endsWith(String.valueOf(x)));

    private final int key;
    private BiPredicate<Object, Object> method;

    /**
     * Constructor for JsonValidationMethod.
     *
     * @param key Identifier key for the method.
     * @param method The associated lambda function for validation.
     */
    JsonValidationMethod(int key, BiPredicate<Object, Object> method) {
        this.key = key;
        this.method = method;
    }

    /**
     * Returns the key of the validation method.
     *
     * @return The method's key.
     */
    public int getKey() {
        return key;
    }

    /**
     * Checks whether the method performs active validation.
     *
     * @return true if the method is not NONE.
     */
    public boolean satisfyValidation() {
        return key != NONE.getKey();
    }

    /**
     * Performs validation by applying the method to the two values.
     *
     * @param xFromJson The JSON value.
     * @param target The target value to be checked against.
     * @return true if validation is successful, otherwise false.
     */
    public boolean validate(Object xFromJson, Object target) {
        return method.test(xFromJson, target);
    }
}
