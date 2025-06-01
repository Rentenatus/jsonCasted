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
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonMapBuilder;
import de.jare.jsoncasted.parserwriter.JsonValidationMethod;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonMap extends JsonClass implements JsonType {

    private final JsonClass itemClass;
    private final JsonCollectionType colType;

    public JsonMap(String cName, Class<? extends JsonInstance<?>> singular, JsonClass itemClass, JsonCollectionType colType) {
        super(cName, new JsonMapBuilder(singular, itemClass));
        this.itemClass = itemClass;
        this.colType = colType;

    }

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
     * Wenn keine geschweifte Klammern fuer ein Objekt gefunden wurden, dann
     * wird das Feld direkt vorgegeben. Eigentlich knn es sich dann nur um ein
     * Primitiv handeln.
     *
     * @return diese JsonClass der Items.
     */
    @Override
    public JsonClass getDirectClass() {
        return this;
    }

    @Override
    public boolean contains(JsonClass check) {
        return (check instanceof JsonMap);
    }

    /**
     * Iteration über die Feldnamen.
     *
     * @param ob Das Objekt.
     * @return Iterator.
     */
    @Override
    public Iterator<String> keysForWriteIterator(Object ob) {
        JsonInstance<?> ji = (JsonInstance<?>) ob;
        return (new ArrayList<>(ji.keySet())).iterator();
    }

    /**
     * Gibt das Feld zum Feldnamen .
     *
     * @param key Name.
     * @return Feld.
     */
    @Override
    public JsonField get(String key) {
        return new JsonField(this, key, itemClass, colType, JsonValidationMethod.NONE);
    }

    /**
     * Gibt es keine Felder.
     *
     * @param ob Das Objekt.
     * @return unmodifiableCollection der Feldnamen.
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

}
