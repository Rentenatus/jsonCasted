/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonMapBuilder;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.parserwriter.JsonValidationMethod;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents a JSON map/collection type that wraps a singular class with collection semantics.
 *
 * <p>A JsonMap extends {@link JsonClass} to provide map-like behavior where keys map to
 * instances of a specified item class. This is used for representing JSON objects
 * that function as maps or dictionaries with homogeneous value types.</p>
 *
 * <p>This class handles special behavior for map types including:</p>
 * <ul>
 *   <li>Dynamic field access by key</li>
 *   <li>Collection type semantics (LIST, ARRAY, or NONE)</li>
 *   <li>Empty map detection</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class JsonMap extends JsonClass implements JsonType {

    private final JsonClass itemClass;
    private final JsonCollectionType colType;

    /**
     * Constructs a JsonMap with the specified parameters.
     *
     * @param cName the canonical name for this map type.
     * @param singular the singular class type for map entries.
     * @param itemClass the JsonClass for map values.
     * @param colType the collection type (LIST, ARRAY, or NONE).
     */
    public JsonMap(String cName, Class<? extends JsonInstance<?>> singular, JsonClass itemClass, JsonCollectionType colType) {
        super(cName, new JsonMapBuilder(singular, itemClass));
        this.itemClass = itemClass;
        this.colType = colType;

    }

    /**
     * Constructs a JsonMap with skipping nulls configuration.
     *
     * @param cName the canonical name for this map type.
     * @param skippingNulls if {@code true}, null values will be skipped during serialization.
     * @param singular the singular class type for map entries.
     * @param itemClass the JsonClass for map values.
     * @param colType the collection type (LIST, ARRAY, or NONE).
     */
    public JsonMap(String cName, boolean skippingNulls, Class<? extends JsonInstance<?>> singular, JsonClass itemClass, JsonCollectionType colType) {
        super(cName, skippingNulls, new JsonMapBuilder(singular, itemClass));
        this.itemClass = itemClass;
        this.colType = colType;
    }

    @Override
    public String setterPre(JsonType jType) {
        return "";
    }

    @Override
    public String getterPre(JsonType jType) {
        return "";
    }

    /**
     * Returns the JsonClass for the map's values.
     *
     * @return the item class.
     */
    public JsonClass getItemClass() {
        return itemClass;
    }

    /**
     * Returns this JsonMap as the direct class representation.
     *
     * @return this JsonMap instance.
     */
    @Override
    public JsonClass getDirectClass() {
        return this;
    }

    @Override
    public boolean contains(JsonType check) {
        return (check instanceof JsonMap);
    }

    /**
     * Returns an iterator over the field names (keys) for writing.
     *
     * @param ob the JsonInstance object.
     * @return an iterator over the key set.
     */
    @Override
    public Iterator<String> keysForWriteIterator(Object ob) {
        JsonInstance<?> ji = (JsonInstance<?>) ob;
        return (new ArrayList<>(ji.keySet())).iterator();
    }

    /**
     * Creates a JsonField for the given key with the map's item class and collection type.
     *
     * @param key the field name/key.
     * @return a new JsonField configured for this map.
     */
    @Override
    public JsonField get(String key) {
        return new JsonField(this, key, itemClass, colType, JsonValidationMethod.NONE);
    }

    /**
     * Checks if the map has any fields (is not empty).
     *
     * @param ob the JsonInstance object.
     * @return {@code true} if the map has entries, {@code false} otherwise.
     */
    @Override
    public boolean hasFieldKeys(Object ob) {
        JsonInstance<?> ji = (JsonInstance<?>) ob;
        return !ji.isEmpty();
    }

    @Override
    public Object getAttr(JsonField next, Object ob) {
        String key = next.getfName();
        JsonInstance<?> ji = (JsonInstance<?>) ob;
        return ji.get(key);
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public String toString(Object attr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JsonTypeDescriptor describeHead(JsonModelDescriptor context) {
        if (context.getType(itemClass.getcName()) == null) {
            context.addType(itemClass.describeHead(context));
        }
        return super.describeHead(context);
    }

    @Override
    public void describeDependencies(JsonModelDescriptor context) {

        JsonTypeDescriptor target = context.requireType(getcName());

        // Ensure dependency if this field references a JsonClass
        JsonClass depClass = itemClass;
        if (depClass != null && !context.isDescribed(depClass.getcName())) {
            JsonTypeDescriptor depHead = depClass.describeHead(context);
            context.addType(depHead);
            depClass.describeDependencies(context);
        }

        JsonFieldDescriptor fd = new JsonFieldDescriptor(
                "*:" + itemClass.getcName() + (colType == JsonCollectionType.NONE ? "" : "[]"),
                itemClass.getcName(), // typeName
                colType, // JsonCollectionType 
                false, // required?
                false, // constructorParam?
                null, // getterName (String) oder null
                null // setterName (String) oder null
        );

        target.setMappingAllFields(fd);
    }

}
