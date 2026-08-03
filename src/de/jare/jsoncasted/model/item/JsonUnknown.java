/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.BuilderService;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModellClassBuilder;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.JsonTypeVisibility;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * The JsonInter class represents an wildcard object definition in a JSON structure. It implements JsonType, allowing
 * multiple class associations.
 *
 *
 *
 * @author Janusch Rentenatus
 */
public class JsonUnknown implements JsonInter {

    private final String cName;

    /**
     * Constructs a JsonInter instance with the specified interface name and builder.
     *
     * @param cName The name of the interface.
     */
    public JsonUnknown(String cName) {
        super();
        this.cName = cName;
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
     * Returns the node type for this interface. Interfaces are always represented as OBJECT nodes in JSON.
     *
     * @return JsonNodeType.OBJECT always.
     */
    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.OBJECT;
    }

    /**
     * Returns the direct JSON class. Since this represents an interface, it does not have a direct class.
     *
     * @return null, as interfaces do not directly represent a singular class.
     */
    @Override
    public JsonClass getDirectClass() {
        return null;
    }

    /**
     * Determines if this JSON type represents a primitive value.
     *
     * @return false, as interfaces cannot be primitive types.
     */
    @Override
    public boolean isBoxOrPrimitive() {
        return false;
    }

    @Override
    public boolean isReflective() {
        return true;
    }

    /**
     * Returns the visibility of this type.
     *
     * @return
     */
    @Override
    public JsonTypeVisibility getVisibility() {
        return JsonTypeVisibility.PROTECTED;
    }

    /**
     * Sets the visibility of this type.
     *
     * @return itself
     */
    @Override
    public JsonUnknown asPublic() {
        throw new IllegalStateException();
    }

    /**
     * Sets the visibility of this type.
     *
     * @return itself
     */
    @Override
    public JsonUnknown asProtected() {
        return this;
    }

    @Override
    public boolean contains(JsonType check) {
        return true;
    }

    /**
     * Converts the provided attribute into its string representation.
     *
     * @param attr The attribute to convert.
     * @return The string representation of the attribute.
     */
    @Override
    public String toString(Object attr) {
        return String.valueOf(attr);
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
    public Object build(BuilderService builderService, Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException {
        if (size == 0) {
            return null;
        }
        ArrayList<Object> list = new ArrayList<>();
        while (listIterator.hasNext()) {
            final JsonItem next = listIterator.next();
            list.add(next.buildInstance(builderService));
        }
        if (asList) {
            return list;
        }
        return list.toArray();
    }

    /**
     * Converts an object into a list representation.
     *
     * @param ob The object to convert.
     * @return A list representation of the object.
     */
    @Override
    public Collection<?> asList(Object ob) {
        throw new IllegalStateException();
    }

    /**
     * Determines if casting is required based on the specified level.
     *
     * @param level The casting level.
     * @return true if casting is required, false otherwise.
     */
    @Override
    public boolean needCast(JsonCastingLevel level) {
        return JsonCastingLevel.ALWAYS_CAST == level || JsonCastingLevel.NECESSARY_CAST == level;
    }

    @Override
    public boolean needClassDef(JsonCastingLevel level) {
        return !needCast(level);
    }

    @Override
    public Iterable<JsonClass> iterable() {
        return Collections.emptyList();
    }

    @Override
    public JsonTypeDescriptor describeHeadInterface(JsonModelDescriptor context) {
        return new JsonTypeDescriptor(cName).withNodeType(JsonNodeType.OBJECT);
    }

}
