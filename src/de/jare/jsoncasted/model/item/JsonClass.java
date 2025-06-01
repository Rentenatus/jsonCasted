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
import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonModellClassBuilder;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonValidationMethod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonClass implements JsonType {

    private final HashMap<String, JsonField> fields;
    private final ArrayList<String> keys;
    private final String cName;
    private final JsonModellClassBuilder builder;
    private boolean skippingNulls;

    public JsonClass(String cName, JsonModellClassBuilder builder) {
        this.cName = cName;
        this.builder = builder;
        fields = new HashMap<>();
        keys = new ArrayList<>();
        skippingNulls = false;
    }

    public JsonClass(String cName, boolean skippingNulls, JsonModellClassBuilder builder) {
        this.cName = cName;
        this.builder = builder;
        this.skippingNulls = skippingNulls;
        fields = new HashMap<>();
        keys = new ArrayList<>();
    }

    @Override
    public String getcName() {
        return cName;
    }

    @Override
    public String ownSetterPre() {
        return builder.setterPre();
    }

    @Override
    public String ownGetterPre() {
        return builder.getterPre();
    }

    public boolean isSkippingNulls() {
        return skippingNulls;
    }

    public void setSkippingNulls(boolean skippingNulls) {
        this.skippingNulls = skippingNulls;
    }

    /**
     * Wenn keine geschweifte Klammern fuer ein Objekt gefunden wurden, dann
     * wird das Feld direkt vorgegeben. Eigentlich knn es sich dann nur um ein
     * Primitiv handeln.
     *
     * @return diese JsonClass (this).
     */
    @Override
    public JsonClass getDirectClass() {
        return this;
    }

    @Override
    public boolean contains(JsonClass check) {
        return equals(check);
    }

    public Object build(JsonItem jsonItem) throws JsonBuildException {
        return builder == null ? null : builder.build(this, jsonItem);
    }

    @Override
    public Object build(Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException {
        return builder == null ? null : (asList
                ? builder.buildList(this, listIterator, size)
                : builder.buildArray(this, listIterator, size));
    }

    public Object getAttr(JsonField next, Object ob) {
        Object ret = null;
        for (Method meth : ob.getClass().getMethods()) {
            if (meth.getName().equals(next.getGetter()) && meth.getParameterCount() == 0) {
                try {
                    ret = meth.invoke(ob);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getGlobal().log(Level.SEVERE, "Getter", ex);
                }
                break;
            }
        }
        return ret;
    }

    /**
     * Fuegt ein Feld ein und setzt es in der Reihenfolge ans Ende.
     *
     * @param jField das Feld.
     */
    public void add(JsonField jField) {
        String key = jField.getfName();
        if (fields.containsKey(key)) {
            keys.remove(key);
        }
        keys.add(key);
        fields.put(key, jField);
    }

    public void addField(String fName, JsonType jType) {
        add(new JsonField(this, fName, jType, JsonValidationMethod.NONE));
    }

    public void addField(String fName, JsonType jType, String getterSetterNorm) {
        add(new JsonField(this, fName, jType, getterSetterNorm, JsonValidationMethod.NONE));
    }

    public void addField(String fName, JsonType jType, JsonCollectionType type) {
        add(new JsonField(this, fName, jType, type, JsonValidationMethod.NONE));
    }

    public void addField(String fName, JsonType jType, String getter, String setter) {
        add(new JsonField(fName, jType, getter, setter, JsonValidationMethod.NONE));
    }

    public void addField(String fName, JsonType jType, JsonValidationMethod jsonValidationMethod) {
        add(new JsonField(this, fName, jType, jsonValidationMethod));
    }

    public void addField(String fName, JsonType jType, String getterSetterNorm, JsonValidationMethod jsonValidationMethod) {
        add(new JsonField(this, fName, jType, getterSetterNorm, jsonValidationMethod));
    }

    public void addField(String fName, JsonType jType, JsonCollectionType type, JsonValidationMethod jsonValidationMethod) {
        add(new JsonField(this, fName, jType, type, jsonValidationMethod));
    }

    public void addField(String fName, JsonType jType, String getter, String setter, JsonValidationMethod jsonValidationMethod) {
        add(new JsonField(fName, jType, getter, setter, jsonValidationMethod));
    }

    public void addCParam(String paramName, JsonType jType) {
        add(new JsonCParam(this, paramName, jType));
    }

    public void addCParam(String paramName, JsonType jType, JsonCollectionType type) {
        add(new JsonCParam(this, paramName, jType, type));
    }

    public void addCParam(String paramName, JsonType jType, String getter, String setter) {
        add(new JsonCParam(paramName, jType, getter, setter));
    }

    /**
     * Gibt das Feld zum Feldnamen.
     *
     * @param key Name.
     * @return Feld.
     */
    public JsonField get(String key) {
        return fields.get(key);
    }

    /**
     * Loescht das Feld .
     *
     * @param jField das Feld.
     * @return das geloescht Feld oder {@code null}.
     */
    public JsonField remove(JsonField jField) {
        String key = jField.getfName();
        keys.remove(key);
        return fields.remove(key);
    }

    /**
     * Loescht das Feld zum Feldnamen.
     *
     * @param key Name.
     * @return das geloescht Feld oder {@code null}.
     */
    public JsonField remove(String key) {
        keys.remove(key);
        return fields.remove(key);
    }

    /**
     * Iteration über die Felder ohne Einhaltung der Reihenfolge.
     *
     * @return Iterator.
     */
    public Iterator<JsonField> fieldsIterator() {
        return fields.values().iterator();
    }

    /**
     * Iteration über die Feldnamen mit Einhaltung der Reihenfolge.
     *
     * @return Iterator.
     */
    public Iterator<String> keysForBuildIterator() {
        return keys.iterator();
    }

    /**
     * Iteration über die Feldnamen mit Einhaltung der Reihenfolge.
     *
     * @param ob Das Objekt.
     * @return Iterator.
     */
    public Iterator<String> keysForWriteIterator(Object ob) {
        return keys.iterator();
    }

    /**
     * Gibt es keine Felder.
     *
     * @param ob Das Objekt.
     * @return unmodifiableCollection der Feldnamen.
     */
    public boolean hasFieldKeys(Object ob) {
        return !keys.isEmpty();
    }

    public Class<?> createOrGetSing() throws JsonBuildException {
        Class<?> ret = builder == null ? null : builder.getSingularClass();
        if (ret == null) {
            try {
                return Class.forName(cName);
            } catch (ClassNotFoundException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
                throw new JsonBuildException(ex.getMessage(), ex);
            }
        }
        return ret;
    }

    @Override
    public boolean isPrimitive() {
        return builder == null ? true : builder.isPrimitive();
    }

    @Override
    public String toString(Object attr) {
        return builder == null ? String.valueOf(attr) : builder.toString(attr);
    }

    @Override
    public List<?> asList(Object ob) {
        return builder == null ? new ArrayList<>() : builder.asList(ob);
    }

    @Override
    public boolean needCast(JsonCastingLevel level) {
        return JsonCastingLevel.ALWAYS == level;
    }

    public void addFromSuperclass(JsonClass parent) {
        Iterator<JsonField> it = parent.fieldsIterator();
        while (it.hasNext()) {
            JsonField next = it.next();
            add(next);
        }
    }

}
