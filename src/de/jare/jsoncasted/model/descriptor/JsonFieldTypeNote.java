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

}
