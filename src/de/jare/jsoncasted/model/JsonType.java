/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.BuilderService;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import java.util.Collection;
import java.util.Iterator;

/**
 * Interface defining the contract for JSON type representations in the jsonCasted system.
 *
 * <p>JsonType serves as the base interface for all type definitions including classes,
 * interfaces, enums, and primitive types. It provides methods for type checking,
 * object construction, and serialization.</p>
 *
 * <p>Implementations include:</p>
 * <ul>
 *   <li>{@link de.jare.jsoncasted.model.item.JsonClass JsonClass} - concrete class definitions</li>
 *   <li>{@link de.jare.jsoncasted.model.item.JsonInter JsonInter} - interface definitions</li>
 *   <li>{@link de.jare.jsoncasted.model.item.JsonMap JsonMap} - map/collection type definitions</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public interface JsonType {

    /**
     * Returns the direct JsonClass representation for this type.
     *
     * <p>When no curly braces are found for an object during parsing,
     * the field is provided directly. In this case, it can only be a primitive type.</p>
     *
     * @return the JsonClass instance, or {@code null} if this type does not
     *         have a direct class representation.
     */
    public JsonClass getDirectClass();

    /**
     * Checks whether the given type is contained or allowed by this type.
     *
     * @param check the type to check.
     * @return {@code true} if the type is allowed or contained, {@code false} otherwise.
     */
    public boolean contains(JsonType check);

    /**
     * Builds an object or collection from JSON items.
     *
     * @param builderService the builder service for object construction.
     * @param listIterator iterator over JSON items.
     * @param asList if {@code true}, builds a list; otherwise builds an array.
     * @param size the expected size of the collection.
     * @return the constructed object or collection.
     * @throws JsonBuildException if object construction fails.
     */
    public Object build(BuilderService builderService, Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException;

    /**
     * Checks if this type represents a primitive type.
     *
     * @return {@code true} if this is a primitive type, {@code false} otherwise.
     */
    public boolean isPrimitive();

    /**
     * Converts an attribute value to its string representation.
     *
     * @param attr the attribute value to convert.
     * @return the string representation of the attribute.
     */
    public String toString(Object attr);

    /**
     * Returns the canonical name of this type.
     *
     * @return the type name (typically the fully qualified class name).
     */
    public String getcName();

    /**
     * Returns the array of enum templates for this type (if it represents an enum).
     *
     * @return array of enum templates, or {@code null} if this type is not an enum.
     */
    public default JsonEnumTemplate[] getValuesArray() {
        return null;
    }

    /**
     * Returns the JSON node type for this type.
     *
     * @return the JsonNodeType (OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, or NULL).
     */
    public JsonNodeType getNodeType();

    /**
     * Converts an object to a collection representation.
     *
     * @param ob the object to convert.
     * @return a collection representation of the object.
     */
    public Collection<?> asList(Object ob);

    /**
     * Determines if casting is needed for this type at the specified casting level.
     *
     * @param level the casting level to check.
     * @return {@code true} if casting is required, {@code false} otherwise.
     */
    public boolean needCast(JsonCastingLevel level);

    /**
     * Determines if a class definition is needed for this type at the specified casting level.
     *
     * @param level the casting level to check.
     * @return {@code true} if a class definition is required, {@code false} otherwise.
     */
    public boolean needClassDef(JsonCastingLevel level);

    /**
     * Returns the setter prefix for the given type.
     *
     * @param jType the type to get the setter prefix for.
     * @return the setter prefix string.
     */
    public default String setterPre(JsonType jType) {
        return jType.ownSetterPre();
    }

    /**
     * Returns the getter prefix for the given type.
     *
     * @param jType the type to get the getter prefix for.
     * @return the getter prefix string.
     */
    public default String getterPre(JsonType jType) {
        return jType.ownGetterPre();
    }

    /**
     * Returns the default setter prefix for this type.
     *
     * @return the setter prefix (default is "set").
     */
    public default String ownSetterPre() {
        return "set";
    }

    /**
     * Returns the default getter prefix for this type.
     *
     * @return the getter prefix (default is "get").
     */
    public default String ownGetterPre() {
        return "get";
    }
}
