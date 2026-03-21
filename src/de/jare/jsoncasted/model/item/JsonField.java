/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonValidationMethod;

/**
 * The JsonField class represents a single field in a JSON object. It defines
 * the field name, type, collection behavior, accessor methods, and validation
 * rules.
 *
 * @author Janusch Rentenatus
 */
public class JsonField {

    private final String fName;
    private final JsonType jType;
    private final boolean asList;
    private final boolean asArray;
    private final String getter;
    private final String setter;
    private JsonValidationMethod validationMethod;

    /**
     * Constructs a JsonField instance with collection type and validation
     * method.
     *
     * @param fName The name of the field.
     * @param jType The JSON type of the field.
     * @param colType The collection type (LIST, ARRAY, or NONE).
     * @param getter The getter method name.
     * @param setter The setter method name.
     * @param validationMethod The validation method for the field.
     */
    public JsonField(String fName, JsonType jType, JsonCollectionType colType, String getter, String setter, JsonValidationMethod validationMethod) {
        this.fName = fName;
        this.jType = jType;
        if (jType == null) {
            throw new NullPointerException("JsonType for '" + fName + "' is null.");
        }
        this.asList = colType == JsonCollectionType.LIST;
        this.asArray = colType == JsonCollectionType.ARRAY;
        this.getter = getter;
        this.setter = setter;
        this.validationMethod = validationMethod;
    }

    /**
     * Constructs a JsonField instance without collection type.
     *
     * @param fName The name of the field.
     * @param jType The JSON type of the field.
     * @param getter The getter method name.
     * @param setter The setter method name.
     * @param validationMethod The validation method for the field.
     */
    public JsonField(String fName, JsonType jType, String getter, String setter, JsonValidationMethod validationMethod) {
        this(fName, jType, JsonCollectionType.NONE, getter, setter, validationMethod);
    }

    /**
     * Constructs a JsonField instance.
     *
     * @param parent JsonType for calculating of getter and setter method
     * prefix.
     * @param fName The name of the field.
     * @param jType The JSON type of the field.
     * @param type JsonCollectionType
     * @param getterSetterNorm word root of getter and setter method name.
     * @param validationMethod The validation method for the field.
     */
    public JsonField(JsonType parent, String fName, JsonType jType, JsonCollectionType type, String getterSetterNorm, JsonValidationMethod validationMethod) {
        this.fName = fName;
        this.jType = jType;
        if (jType == null) {
            throw new NullPointerException("JsonType for '" + fName + "' is null.");
        }
        this.asList = type == JsonCollectionType.LIST;
        this.asArray = type == JsonCollectionType.ARRAY;
        this.getter = parent.getterPre(jType) + getterSetterNorm;
        this.setter = parent.setterPre(jType) + getterSetterNorm;
        this.validationMethod = validationMethod;
    }

    /**
     * Constructs a JsonField instance.
     *
     * @param parent JsonType for calculating of getter and setter method
     * prefix.
     * @param fName The name of the field.
     * @param jType The JSON type of the field.
     * @param getterSetterNorm word root of getter and setter method name.
     * @param validationMethod The validation method for the field.
     */
    public JsonField(JsonType parent, String fName, JsonType jType, String getterSetterNorm, JsonValidationMethod validationMethod) {
        this(parent, fName, jType, JsonCollectionType.NONE, getterSetterNorm, validationMethod);
    }

    /**
     * Constructs a JsonField instance.
     *
     * @param parent JsonType for calculating of getter and setter method
     * prefix.
     * @param fName The name of the field.
     * @param jType The JSON type of the field.
     * @param type JsonCollectionType
     * @param validationMethod The validation method for the field.
     */
    public JsonField(JsonType parent, String fName, JsonType jType, JsonCollectionType type, JsonValidationMethod validationMethod) {
        String getterSetterNorm = Character.toUpperCase(fName.charAt(0)) + fName.substring(1);
        this.fName = fName;
        this.jType = jType;
        if (jType == null) {
            throw new NullPointerException("JsonType for '" + fName + "' is null.");
        }
        this.asList = type == JsonCollectionType.LIST;
        this.asArray = type == JsonCollectionType.ARRAY;
        this.getter = parent.getterPre(jType) + getterSetterNorm;
        this.setter = parent.setterPre(jType) + getterSetterNorm;
        this.validationMethod = validationMethod;
    }

    /**
     * Constructs a JsonField instance with standard Java method naming
     * conventions.
     *
     * @param parent
     * @param fName
     * @param jType
     * @param validationMethod
     */
    public JsonField(JsonType parent, String fName, JsonType jType, JsonValidationMethod validationMethod) {
        this(parent, fName, jType, JsonCollectionType.NONE, validationMethod);
    }

    /**
     * Determines whether the field is a constructor parameter.
     *
     * @return false, since standard fields are not constructor parameters.
     */
    public boolean isConstructorParam() {
        return false;
    }

    /**
     * Returns the field name.
     *
     * @return The field name.
     */
    public String getfName() {
        return fName;
    }

    /**
     * Checks whether the field is a list.
     *
     * @return true if the field represents a list, false otherwise.
     */
    public boolean isAsList() {
        return asList;
    }

    /**
     * Checks whether the field is a list or an array.
     *
     * @return true if the field is a collection (list or array), false
     * otherwise.
     */
    public boolean isAsListOrArray() {
        return asList || asArray;
    }

    /**
     * Checks whether the field is an array.
     *
     * @return true if the field represents an array, false otherwise.
     */
    public boolean isAsArray() {
        return asArray;
    }

    /**
     * Retrieves the JSON type of the field.
     *
     * @return The JSON type.
     */
    public JsonType getjType() {
        return jType;
    }

    /**
     * Retrieves the getter method name.
     *
     * @return The getter method name.
     */
    public String getGetter() {
        return getter;
    }

    /**
     * Retrieves the setter method name.
     *
     * @return The setter method name.
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Checks whether the field satisfies the validation method.
     *
     * @return true if validation is satisfied, false otherwise.
     */
    public boolean satisfyValidation() {
        return validationMethod != null && validationMethod.satisfyValidation();
    }

    /**
     * Validates the field value against the provided target.
     *
     * @param xFromJson The value extracted from JSON.
     * @param target The target object.
     * @return true if validation passes, false otherwise.
     */
    public boolean validate(Object xFromJson, Object target) {
        if (validationMethod == null) {
            return true;
        }
        return validationMethod.validate(xFromJson, target);
    }
}
