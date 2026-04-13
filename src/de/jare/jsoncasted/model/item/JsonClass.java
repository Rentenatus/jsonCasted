/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.BuilderService;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonModellClassBuilder;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonValidationMethod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    private final JsonNodeType nodeType;
    private JsonClass parent;

    public JsonClass(String cName, JsonModellClassBuilder builder) {
        this(cName, JsonNodeType.OBJECT, builder);
    }

    public JsonClass(String cName, JsonNodeType nodeType, JsonModellClassBuilder builder) {
        this.cName = cName;
        this.nodeType = nodeType;
        this.builder = builder;
        fields = new HashMap<>();
        keys = new ArrayList<>();
        skippingNulls = false;
        parent = null;
    }

    public JsonClass(String cName, boolean skippingNulls, JsonModellClassBuilder builder) {
        this(cName, JsonNodeType.OBJECT, skippingNulls, builder);
    }

    public JsonClass(String cName, JsonNodeType nodeType, boolean skippingNulls, JsonModellClassBuilder builder) {
        this.cName = cName;
        this.nodeType = nodeType;
        this.builder = builder;
        this.skippingNulls = skippingNulls;
        fields = new HashMap<>();
        keys = new ArrayList<>();
        parent = null;
    }

    @Override
    public String getcName() {
        return cName;
    }

    @Override
    public JsonNodeType getNodeType() {
        return nodeType;
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

    public JsonClass getParent() {
        return parent;
    }

    public boolean isSubOf(JsonType check) {
        if (check == null) {
            return false;
        }
        if (check == this) {
            return true;
        }
        if (check.getcName() == null || this.getcName() == null) {
            return false;
        }
        if (check.getcName().equals(this.getcName()) && check.getNodeType() == this.getNodeType()) {
            return true;
        }
        if (parent == null) {
            return false;
        }
        return parent.isSubOf(check);
    }

    @Override
    public boolean contains(JsonType check) {
        if (check == null) {
            return false;
        }
        if (check == this) {
            return true;
        }
        if (check.getcName() == null || this.getcName() == null) {
            return false;
        }
        return check.getcName().equals(this.getcName()) && check.getNodeType() == this.getNodeType();
    }

    public Object build(JsonItem jsonItem, BuilderService builderService) throws JsonBuildException {
        return builder == null ? null : builder.build(this, jsonItem, builderService);
    }

    @Override
    public Object build(BuilderService builderService, Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException {
        return builder == null ? null : (asList
                ? builder.buildList(this, builderService, listIterator, size)
                : builder.buildArray(this, builderService, listIterator, size));
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

    public JsonField addField(String fName, JsonType jType) {
        final JsonField jsonField = new JsonField(this, fName, jType, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, String getterSetterNorm) {
        final JsonField jsonField = new JsonField(this, fName, jType, getterSetterNorm, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, JsonCollectionType colType) {
        final JsonField jsonField = new JsonField(this, fName, jType, colType, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, String getter, String setter) {
        final JsonField jsonField = new JsonField(fName, jType, getter, setter, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(this, fName, jType, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, String getterSetterNorm, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(this, fName, jType, getterSetterNorm, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, JsonCollectionType colType, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(this, fName, jType, colType, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    public JsonField addField(String fName, JsonType jType, String getter, String setter, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(fName, jType, getter, setter, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    public JsonCParam addCParam(String paramName, JsonType jType) {
        final JsonCParam jsonCParam = new JsonCParam(this, paramName, jType);
        add(jsonCParam);
        return jsonCParam;
    }

    public JsonCParam addCParam(String paramName, JsonType jType, JsonCollectionType colType) {
        final JsonCParam jsonCParam = new JsonCParam(this, paramName, jType, colType);
        add(jsonCParam);
        return jsonCParam;
    }

    public JsonCParam addCParam(String paramName, JsonType jType, String getter) {
        final JsonCParam jsonCParam = new JsonCParam(paramName, jType, getter, null);
        add(jsonCParam);
        return jsonCParam;
    }

    public JsonCParam addCParam(String paramName, JsonType jType, String getter, JsonCollectionType colType) {
        final JsonCParam jsonCParam = new JsonCParam(paramName, jType, colType, getter, null);
        add(jsonCParam);
        return jsonCParam;
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
    public Collection<?> asList(Object ob) {
        return builder == null ? new ArrayList<>() : builder.asCollection(ob);
    }

    @Override
    public boolean needCast(JsonCastingLevel level) {
        if (JsonCastingLevel.ALWAYS_CAST == level) {
            return true;
        }
        if (JsonCastingLevel.NECESSARY_CAST != level) {
            return false;
        }
        return parent != null;
    }

    @Override
    public boolean needClassDef(JsonCastingLevel level) {
        if (JsonCastingLevel.ALWAYS_CLASS_DEF == level) {
            return true;
        }
        if (JsonCastingLevel.NECESSARY_CLASS_DEF != level) {
            return false;
        }
        return parent != null;
    }

    public void addFromSuperclass(JsonClass parent) {
        if (parent.getParent() != null) {
            if (parent.getcName().equals(cName)) {
                throw new IllegalArgumentException("A class cannot be its own superclass.");
            }
            addFromSuperclass(parent.getParent());
        }
        Iterator<JsonField> it = parent.fieldsIterator();
        while (it.hasNext()) {
            JsonField next = it.next();
            add(next);
        }
        this.parent = parent;
    }

    public JsonTypeDescriptor describeHead(JsonModelDescriptor modelDescriptor) {
        return new JsonTypeDescriptor(cName)
                .withNodeType(nodeType)
                .withSkippingNulls(skippingNulls);
    }

    public void describeDependencies(JsonModelDescriptor context) {
        JsonTypeDescriptor target = context.requireType(cName);
        if (parent != null) {
            JsonTypeDescriptor parentDescr = context.getOrDefault(parent.getcName(), null);
            target.setParent(parentDescr);
        }

        for (String key : keys) {
            JsonField jf = fields.get(key);
            if (jf == null) {
                continue;
            }

            JsonType jType = jf.getjType();
            String targetTypeName = jType.getcName();

            // Wenn dieses Feld auf eine JsonClass verweist, Abhängigkeit sicherstellen
            JsonClass depClass = jType.getDirectClass();
            if (depClass != null && !context.isDescribed(depClass.getcName())) {
                JsonTypeDescriptor depHead = depClass.describeHead(context);
                context.addType(depHead);
                depClass.describeDependencies(context);
            }

            JsonFieldDescriptor fd = new JsonFieldDescriptor(
                    jf.getfName(), // fieldName
                    targetTypeName, // typeName
                    jf.getCollectionType(), // JsonCollectionType 
                    !skippingNulls, // required?
                    jf.isConstructorParam(), // constructorParam?
                    jf.getGetter(), // getterName (String) oder null
                    jf.getSetter() // setterName (String) oder null
            );
            fd.setKind(jf.getKind());

            if (jf.isConstructorParam()) {
                target.addConstructorParam(fd);
            } else {
                target.addField(fd);
            }
        }
    }

}
