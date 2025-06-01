/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModellClassBuilder;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The JsonInter class represents an interface definition in a JSON structure.
 * It extends ArrayList<JsonClass> and implements JsonType, allowing multiple
 * class associations.
 *
 * @author Janusch Rentenatus
 */
public class JsonInter extends ArrayList<JsonClass> implements JsonType {

    private final JsonModellClassBuilder builder;
    private String cName;

    /**
     * Constructs a JsonInter instance with the specified interface name and
     * builder.
     *
     * @param cName The name of the interface.
     * @param builder The builder responsible for instance creation.
     */
    public JsonInter(String cName, JsonModellClassBuilder builder) {
        super();
        this.builder = builder;
        this.cName = cName;
    }

    /**
     * Constructs a JsonInter instance with a predefined set of associated
     * classes.
     *
     * @param cName The name of the interface.
     * @param builder The builder responsible for instance creation.
     * @param jClass The associated JSON classes.
     */
    public JsonInter(String cName, JsonModellClassBuilder builder, JsonClass... jClass) {
        super(jClass.length);
        for (JsonClass jc : jClass) {
            add(jc);
        }
        this.builder = builder;
        this.cName = cName;
    }

    /**
     * Adds multiple JSON classes to the interface definition.
     *
     * @param jClass The JSON classes to associate with the interface.
     */
    public void addAll(JsonClass... jClass) {
        for (JsonClass jc : jClass) {
            add(jc);
        }
    }

    /**
     * Retrieves the name of the interface.
     *
     * @return The interface name.
     */
    @Override
    public String getcName() {
        return cName;
    }

    /**
     * Returns the direct JSON class. Since this represents an interface, it
     * does not have a direct class.
     *
     * @return null, as interfaces do not directly represent a singular class.
     */
    @Override
    public JsonClass getDirectClass() {
        return null;
    }

    /**
     * Checks whether a given JSON class is associated with this interface.
     *
     * @param check The class to check.
     * @return true if the class is included, false otherwise.
     */
    @Override
    public boolean contains(JsonClass check) {
        if (check == null) {
            return false;
        }
        return super.contains(check);
    }

    /**
     * Determines if this JSON type represents a primitive value.
     *
     * @return false, as interfaces cannot be primitive types.
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * Converts the provided attribute into its string representation.
     *
     * @param attr The attribute to convert.
     * @return The string representation of the attribute.
     */
    @Override
    public String toString(Object attr) {
        return builder == null ? String.valueOf(attr) : builder.toString(attr);
    }

    /**
     * Builds an instance or a collection from the JSON structure.
     *
     * @param listIterator Iterator over JSON items.
     * @param asList Indicates whether the output should be a list.
     * @param size The expected size of the output collection.
     * @return The constructed object or collection.
     * @throws JsonBuildException If instance creation fails.
     */
    @Override
    public Object build(Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException {
        return builder == null ? null : (asList
                ? builder.buildList(this, listIterator, size)
                : builder.buildArray(this, listIterator, size));
    }

    /**
     * Converts an object into a list representation.
     *
     * @param ob The object to convert.
     * @return A list representation of the object.
     */
    @Override
    public List<?> asList(Object ob) {
        return builder == null ? new ArrayList<>() : builder.asList(ob);
    }

    /**
     * Determines if casting is required based on the specified level.
     *
     * @param level The casting level.
     * @return true if casting is required, false otherwise.
     */
    @Override
    public boolean needCast(JsonCastingLevel level) {
        return JsonCastingLevel.NEVER != level;
    }
}
