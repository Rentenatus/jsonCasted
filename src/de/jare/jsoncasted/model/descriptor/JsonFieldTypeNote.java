/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.model.JsonCollectionType;
import java.util.Objects;

/**
 * Base class encapsulating type name and collection type information for JSON fields.
 *
 * @author Janusch Rentenatus
 */
public class JsonFieldTypeNote {

    private final String typeName;
    private final JsonCollectionType collectionType;
    private boolean sameCollection = true;
    private boolean equalsCollection = false;

    /**
     * Constructs a type note with the given type name and collection type.
     *
     * @param typeName the type name (must not be null).
     * @param collectionType the collection type (NONE, ARRAY, or LIST).
     */
    public JsonFieldTypeNote(String typeName, JsonCollectionType collectionType) {
        this.typeName = Objects.requireNonNull(typeName, "typeName");
        this.collectionType = collectionType != null ? collectionType : JsonCollectionType.NONE;
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
        return collectionType.isNotCollection();
    }

    /**
     * Checks if this is a list or array field.
     *
     * @return {@code true} if collection type is ARRAY or LIST.
     */
    public boolean isAsListOrArray() {
        return collectionType.isAsListOrArray();
    }

    /**
     * Checks if this is a list field.
     *
     * @return {@code true} if collection type is LIST.
     */
    public boolean isAsList() {
        return collectionType.isAsList();
    }

    /**
     * Checks if this is an array field.
     *
     * @return {@code true} if collection type is ARRAY.
     */
    public boolean isAsArray() {
        return collectionType.isAsArray();
    }

    @Override
    public String toString() {
        return "JsonFieldTypeNote["
                + "typeName=" + typeName
                + ", collectionType=" + collectionType
                + "]";
    }

    /**
     * Returns the collection sharing policy of this field: the same list instance at several fields is written once
     * as a wood identified definition and referenced at the other places. Default is {@code true}.
     *
     * @return true when the instance identity of the collection value is kept on writing.
     */
    public boolean isSameCollection() {
        return sameCollection;
    }

    /**
     * Sets the collection sharing policy of this field.
     *
     * @param sameCollection whether to keep the instance identity of the collection value.
     */
    public void setSameCollection(boolean sameCollection) {
        this.sameCollection = sameCollection;
    }

    /**
     * Returns the content equality policy of this field: lists that are component-wise identical - same size first,
     * then element by element: primitives by value, objects by instance - share one wood id. Default is
     * {@code false}.
     *
     * @return true when equal collections share one wood id.
     */
    public boolean isEqualsCollection() {
        return equalsCollection;
    }

    /**
     * Sets the content equality policy of this field. It wins over the instance identity policy: identical
     * instances are also component-wise identical.
     *
     * @param equalsCollection whether equal collections share one wood id.
     */
    public void setEqualsCollection(boolean equalsCollection) {
        this.equalsCollection = equalsCollection;
    }

}
