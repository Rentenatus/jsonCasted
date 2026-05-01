/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
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
 * Represents a constructor parameter field in a JSON class definition.
 *
 * <p>A JsonCParam extends {@link JsonField} to represent fields that are passed
 * as constructor parameters when instantiating objects. This is used for classes
 * that require constructor arguments rather than setters for initialization.</p>
 *
 * <p>Constructor parameters are distinguished from regular fields by the
 * {@link #isConstructorParam()} method which always returns {@code true}.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonCParam extends JsonField {

    /**
     * Constructs a constructor parameter with full configuration.
     *
     * @param fName the field name.
     * @param jType the JSON type of the parameter.
     * @param colType the collection type.
     * @param getter the getter method name.
     * @param setter the setter method name.
     */
    public JsonCParam(String fName, JsonType jType, JsonCollectionType colType, String getter, String setter) {
        super(fName, jType, colType, getter, setter, JsonValidationMethod.NONE);
    }

    /**
     * Constructs a constructor parameter without collection type.
     *
     * @param fName the field name.
     * @param jType the JSON type of the parameter.
     * @param getter the getter method name.
     * @param setter the setter method name.
     */
    public JsonCParam(String fName, JsonType jType, String getter, String setter) {
        super(fName, jType, JsonCollectionType.NONE, getter, setter, JsonValidationMethod.NONE);
    }

    /**
     * Constructs a constructor parameter with parent type and collection type.
     *
     * @param parent the parent JSON type for getter/setter prefix calculation.
     * @param fName the field name.
     * @param jType the JSON type of the parameter.
     * @param type the collection type.
     * @param getterSetterNorm the normalized getter/setter name root.
     */
    public JsonCParam(JsonType parent, String fName, JsonType jType, JsonCollectionType type, String getterSetterNorm) {
        super(parent, fName, jType, type, getterSetterNorm, JsonValidationMethod.NONE);
    }

    /**
     * Constructs a constructor parameter with parent type.
     *
     * @param parent the parent JSON type for getter/setter prefix calculation.
     * @param fName the field name.
     * @param jType the JSON type of the parameter.
     * @param getterSetterNorm the normalized getter/setter name root.
     */
    public JsonCParam(JsonType parent, String fName, JsonType jType, String getterSetterNorm) {
        super(parent, fName, jType, JsonCollectionType.NONE, getterSetterNorm, JsonValidationMethod.NONE);
    }

    /**
     * Constructs a constructor parameter with parent type and collection type.
     *
     * @param parent the parent JSON type for getter/setter prefix calculation.
     * @param fName the field name.
     * @param jType the JSON type of the parameter.
     * @param type the collection type.
     */
    public JsonCParam(JsonType parent, String fName, JsonType jType, JsonCollectionType type) {
        super(parent, fName, jType, type, JsonValidationMethod.NONE);

    }

    /**
     * Constructs a constructor parameter with parent type.
     *
     * @param parent the parent JSON type for getter/setter prefix calculation.
     * @param fName the field name.
     * @param jType the JSON type of the parameter.
     */
    public JsonCParam(JsonType parent, String fName, JsonType jType) {
        super(parent, fName, jType, JsonCollectionType.NONE, JsonValidationMethod.NONE);
    }

    /**
     * Always returns {@code true} to indicate this is a constructor parameter.
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isConstructorParam() {
        return true;
    }

}
