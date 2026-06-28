/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.FieldKind;
import java.util.Objects;

/**
 * Describes a field or property of a JSON-mappable type.
 *
 * <p>
 * This class captures all metadata about a field including:</p>
 * <ul>
 * <li>Field name and type</li>
 * <li>Collection type (NONE, ARRAY, LIST)</li>
 * <li>Whether the field is required</li>
 * <li>Whether it's a constructor parameter or a setter field</li>
 * <li>Getter and setter method names</li>
 * <li>Field kind (ATTRIBUTE, CONTAINMENT, REFERENCE)</li>
 * </ul>
 *
 * <p>
 * Constructor parameters typically have only a getter and are set during instantiation. Setter fields have both getter
 * and setter and are set after construction.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonFieldDescriptor {

    private final String fieldName;
    private final String typeName;
    private final JsonCollectionType collectionType;
    private final boolean required;
    private final boolean constructorParam;
    private String getter;
    private String setter;
    private FieldKind kind;

    /**
     * Constructs a field descriptor with minimal information.
     *
     * @param jsonName the field name.
     * @param typeName the type name.
     */
    public JsonFieldDescriptor(String jsonName, String typeName) {
        this(jsonName, typeName, null, false, false, null, null);
    }

    /**
     * Constructs a complete field descriptor.
     *
     * @param fieldName the field name (must not be null).
     * @param typeName the type name (must not be null).
     * @param collectionType the collection type (NONE, ARRAY, or LIST).
     * @param required whether the field is required.
     * @param constructorParam whether this is a constructor parameter.
     * @param getter the getter method name.
     * @param setter the setter method name.
     */
    public JsonFieldDescriptor(String fieldName,
            String typeName,
            JsonCollectionType collectionType,
            boolean required,
            boolean constructorParam,
            String getter,
            String setter) {
        this.fieldName = Objects.requireNonNull(fieldName, "jsonName");
        this.typeName = Objects.requireNonNull(typeName, "typeName");
        this.collectionType = collectionType;
        this.required = required;
        this.constructorParam = constructorParam;
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Returns the field name.
     *
     * @return the field name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the type name.
     *
     * @return the type name.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns the collection type.
     *
     * @return the collection type.
     */
    public JsonCollectionType getCollectionType() {
        return collectionType;
    }

    /**
     * Checks if this is not a collection field.
     *
     * @return {@code true} if collection type is NONE.
     */
    public boolean isNotCollection() {
        return collectionType == JsonCollectionType.NONE;
    }

    /**
     * Checks if this is a list or array field.
     *
     * @return {@code true} if collection type is ARRAY or LIST.
     */
    public boolean isAsListOrArray() {
        return collectionType == JsonCollectionType.ARRAY || collectionType == JsonCollectionType.LIST;
    }

    /**
     * Checks if this is a list field.
     *
     * @return {@code true} if collection type is LIST.
     */
    public boolean isAsList() {
        return collectionType == JsonCollectionType.LIST;
    }

    /**
     * Checks if this is an array field.
     *
     * @return {@code true} if collection type is ARRAY.
     */
    public boolean isAsArray() {
        return collectionType == JsonCollectionType.ARRAY;
    }

    /**
     * Checks if this is a required field.
     *
     * @return {@code true} if the field is required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Checks if this is a constructor parameter.
     *
     * @return {@code true} if this is a constructor parameter.
     */
    public boolean isConstructorParam() {
        return constructorParam;
    }

    /**
     * Checks if this is a setter field (not a constructor parameter).
     *
     * @return {@code true} if this is a setter field.
     */
    public boolean isSetterField() {
        return !constructorParam;
    }

    /**
     * Validates this field descriptor.
     *
     * @throws IllegalStateException if validation fails (blank names, inconsistent field types).
     */
    public void validate() {
        if (fieldName.isBlank()) {
            throw new IllegalStateException("jsonName is blank");
        }
        if (typeName.isBlank()) {
            throw new IllegalStateException("typeName is blank");
        }
        if (constructorParam && setter != null) {
            throw new IllegalStateException("Constructor param must not have a setter: " + fieldName);
        }
        if (!constructorParam && getter != null && setter == null) {
            throw new IllegalStateException("Setter field without setter: " + fieldName);
        }
    }

    /**
     * Returns the field kind.
     *
     * @return the FieldKind.
     */
    public FieldKind getKind() {
        return kind;
    }

    /**
     * Sets the field kind.
     *
     * @param kind the FieldKind to set.
     */
    public void setKind(FieldKind kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "JsonFieldDescriptor["
                + "jsonName=" + fieldName
                + ", typeName=" + typeName
                + ", collectionType=" + collectionType
                + ", required=" + required
                + ", kind=" + kind.getName()
                + ", constructorParam=" + constructorParam
                + ", getter=" + getter
                + ", setter=" + setter
                + "]";
    }

}
