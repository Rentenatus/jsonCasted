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
import de.jare.jsoncasted.model.JsonEnumTemplate;
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
 * Represents a JSON class definition within the jsonCasted model system.
 *
 * <p>A JsonClass describes a concrete Java class with its fields, type metadata,
 * and casting rules. It serves as a central component in the model pipeline,
 * connecting the JSON structure with actual Java object instantiation.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Managing field definitions and their types</li>
 *   <li>Handling inheritance through parent class references</li>
 *   <li>Supporting enum value arrays for enum types</li>
 *   <li>Providing build methods for object construction</li>
 *   <li>Generating type descriptors for model introspection</li>
 * </ul>
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
    private JsonEnumTemplate[] valuesArray;

    /**
     * Constructs a JsonClass with the specified class name and builder.
     * Uses OBJECT as the default node type.
     *
     * @param cName The canonical name of the class.
     * @param builder The builder responsible for instance creation.
     */
    public JsonClass(String cName, JsonModellClassBuilder builder) {
        this(cName, JsonNodeType.OBJECT, builder);
    }

    /**
     * Constructs a JsonClass with the specified class name, node type, and builder.
     *
     * @param cName The canonical name of the class.
     * @param nodeType The JSON node type for this class.
     * @param builder The builder responsible for instance creation.
     */
    public JsonClass(String cName, JsonNodeType nodeType, JsonModellClassBuilder builder) {
        this.cName = cName;
        this.nodeType = nodeType;
        this.builder = builder;
        fields = new HashMap<>();
        keys = new ArrayList<>();
        skippingNulls = false;
        parent = null;
    }

    /**
     * Constructs a JsonClass with the specified class name and builder, with skipping nulls configuration.
     * Uses OBJECT as the default node type.
     *
     * @param cName The canonical name of the class.
     * @param skippingNulls If true, null values will be skipped during serialization.
     * @param builder The builder responsible for instance creation.
     */
    public JsonClass(String cName, boolean skippingNulls, JsonModellClassBuilder builder) {
        this(cName, JsonNodeType.OBJECT, skippingNulls, builder);
    }

    /**
     * Constructs a JsonClass with full configuration.
     *
     * @param cName The canonical name of the class.
     * @param nodeType The JSON node type for this class.
     * @param skippingNulls If true, null values will be skipped during serialization.
     * @param builder The builder responsible for instance creation.
     */
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

    /**
     * Returns whether this class skips null values during serialization.
     *
     * @return true if null values are skipped, false otherwise.
     */
    public boolean isSkippingNulls() {
        return skippingNulls;
    }

    /**
     * Sets whether this class should skip null values during serialization.
     *
     * @param skippingNulls If true, null values will be skipped.
     */
    public void setSkippingNulls(boolean skippingNulls) {
        this.skippingNulls = skippingNulls;
    }

    /**
     * Returns this JsonClass as the direct class representation.
     *
     * <p>When no curly braces are found for an object during parsing,
     * the field is provided directly. In this case, it can only be a primitive type.</p>
     *
     * @return this JsonClass instance.
     */
    @Override
    public JsonClass getDirectClass() {
        return this;
    }

    /**
     * Returns the parent class for inheritance purposes.
     *
     * @return The parent JsonClass, or null if this class has no parent.
     */
    public JsonClass getParent() {
        return parent;
    }

    /**
     * Checks if this class is a subtype of the given type.
     * This includes checking the type itself, direct equality, and the parent hierarchy.
     *
     * @param check The type to check against.
     * @return true if this class is a subtype of check, false otherwise.
     */
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

    /**
     * Builds an object instance from a JSON item using the configured builder.
     *
     * @param jsonItem The JSON item to build from.
     * @param builderService The builder service for managing object construction.
     * @return The constructed object, or null if builder is not configured.
     * @throws JsonBuildException If object construction fails.
     */
    public Object build(JsonItem jsonItem, BuilderService builderService) throws JsonBuildException {
        return builder == null ? null : builder.build(this, jsonItem, builderService);
    }

    @Override
    public Object build(BuilderService builderService, Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException {
        return builder == null ? null : (asList
                ? builder.buildList(this, builderService, listIterator, size)
                : builder.buildArray(this, builderService, listIterator, size));
    }

    /**
     * Retrieves an attribute value from an object using reflection.
     * Invokes the getter method matching the field's getter name on the object.
     *
     * @param next The field whose value to retrieve.
     * @param ob The object to retrieve the attribute from.
     * @return The attribute value, or null if the getter cannot be invoked.
     */
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
     * Sets the array of enum templates for this class.
     * Used for enum types that need name-based resolution.
     *
     * @param valuesArray The array of enum templates.
     */
    public void setValuesArray(JsonEnumTemplate[] valuesArray) {
        this.valuesArray = valuesArray;
    }

    @Override
    public JsonEnumTemplate[] getValuesArray() {
        return valuesArray;
    }

    /**
     * Adds a field to this class and places it at the end of the field order.
     *
     * <p>If a field with the same name already exists, it is removed first to maintain
     * uniqueness. The field is then added to the keys list to preserve order.</p>
     *
     * @param jField the field to add.
     */
    public void add(JsonField jField) {
        String key = jField.getfName();
        if (fields.containsKey(key)) {
            keys.remove(key);
        }
        keys.add(key);
        fields.put(key, jField);
    }

    /**
     * Adds a field to this class with the specified name and type.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType) {
        final JsonField jsonField = new JsonField(this, fName, jType, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with the specified name, type, and getter/setter normalization.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param getterSetterNorm The normalized getter/setter name root.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, String getterSetterNorm) {
        final JsonField jsonField = new JsonField(this, fName, jType, getterSetterNorm, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with the specified name, type, and collection type.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param colType The collection type (LIST, ARRAY, or NONE).
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, JsonCollectionType colType) {
        final JsonField jsonField = new JsonField(this, fName, jType, colType, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with the specified name, type, and explicit getter/setter method names.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param getter The getter method name.
     * @param setter The setter method name.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, String getter, String setter) {
        final JsonField jsonField = new JsonField(fName, jType, getter, setter, JsonValidationMethod.NONE);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with the specified name, type, and validation method.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param jsonValidationMethod The validation method for the field.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(this, fName, jType, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with the specified name, type, getter/setter normalization, and validation method.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param getterSetterNorm The normalized getter/setter name root.
     * @param jsonValidationMethod The validation method for the field.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, String getterSetterNorm, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(this, fName, jType, getterSetterNorm, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with the specified name, type, collection type, and validation method.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param colType The collection type (LIST, ARRAY, or NONE).
     * @param jsonValidationMethod The validation method for the field.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, JsonCollectionType colType, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(this, fName, jType, colType, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a field to this class with full configuration.
     *
     * @param fName The field name.
     * @param jType The JSON type of the field.
     * @param getter The getter method name.
     * @param setter The setter method name.
     * @param jsonValidationMethod The validation method for the field.
     * @return The newly created and added JsonField.
     */
    public JsonField addField(String fName, JsonType jType, String getter, String setter, JsonValidationMethod jsonValidationMethod) {
        final JsonField jsonField = new JsonField(fName, jType, getter, setter, jsonValidationMethod);
        add(jsonField);
        return jsonField;
    }

    /**
     * Adds a constructor parameter field to this class.
     *
     * @param paramName The parameter name.
     * @param jType The JSON type of the parameter.
     * @return The newly created and added JsonCParam.
     */
    public JsonCParam addCParam(String paramName, JsonType jType) {
        final JsonCParam jsonCParam = new JsonCParam(this, paramName, jType);
        add(jsonCParam);
        return jsonCParam;
    }

    /**
     * Adds a constructor parameter field to this class with collection type.
     *
     * @param paramName The parameter name.
     * @param jType The JSON type of the parameter.
     * @param colType The collection type (LIST, ARRAY, or NONE).
     * @return The newly created and added JsonCParam.
     */
    public JsonCParam addCParam(String paramName, JsonType jType, JsonCollectionType colType) {
        final JsonCParam jsonCParam = new JsonCParam(this, paramName, jType, colType);
        add(jsonCParam);
        return jsonCParam;
    }

    /**
     * Adds a constructor parameter field to this class with explicit getter.
     *
     * @param paramName The parameter name.
     * @param jType The JSON type of the parameter.
     * @param getter The getter method name.
     * @return The newly created and added JsonCParam.
     */
    public JsonCParam addCParam(String paramName, JsonType jType, String getter) {
        final JsonCParam jsonCParam = new JsonCParam(paramName, jType, getter, null);
        add(jsonCParam);
        return jsonCParam;
    }

    /**
     * Adds a constructor parameter field to this class with collection type and explicit getter.
     *
     * @param paramName The parameter name.
     * @param jType The JSON type of the parameter.
     * @param getter The getter method name.
     * @param colType The collection type (LIST, ARRAY, or NONE).
     * @return The newly created and added JsonCParam.
     */
    public JsonCParam addCParam(String paramName, JsonType jType, String getter, JsonCollectionType colType) {
        final JsonCParam jsonCParam = new JsonCParam(paramName, jType, colType, getter, null);
        add(jsonCParam);
        return jsonCParam;
    }

    /**
     * Retrieves a field by its name.
     *
     * @param key the field name.
     * @return the JsonField with the given name, or {@code null} if not found.
     */
    public JsonField get(String key) {
        return fields.get(key);
    }

    /**
     * Removes a field from this class.
     *
     * @param jField the field to remove.
     * @return the removed field, or {@code null} if not found.
     */
    public JsonField remove(JsonField jField) {
        String key = jField.getfName();
        keys.remove(key);
        return fields.remove(key);
    }

    /**
     * Removes a field by its name.
     *
     * @param key the field name to remove.
     * @return the removed field, or {@code null} if not found.
     */
    public JsonField remove(String key) {
        keys.remove(key);
        return fields.remove(key);
    }

    /**
     * Returns an iterator over all fields without preserving order.
     *
     * @return an iterator over the fields.
     */
    public Iterator<JsonField> fieldsIterator() {
        return fields.values().iterator();
    }

    /**
     * Returns an iterator over field names in order.
     *
     * @return an iterator over field names.
     */
    public Iterator<String> keysForBuildIterator() {
        return keys.iterator();
    }

    /**
     * Returns an iterator over field names in order for writing.
     *
     * @param ob the object being processed.
     * @return an iterator over field names.
     */
    public Iterator<String> keysForWriteIterator(Object ob) {
        return keys.iterator();
    }

    /**
     * Checks if this class has any field keys defined.
     *
     * @param ob the object being processed.
     * @return {@code true} if there are field keys, {@code false} otherwise.
     */
    public boolean hasFieldKeys(Object ob) {
        return !keys.isEmpty();
    }

    /**
     * Creates or retrieves the singular class for this JsonClass.
     * Attempts to get the singular class from the builder, or falls back to
     * loading the class by name using Class.forName.
     *
     * @return The Class object for this JsonClass.
     * @throws JsonBuildException If the class cannot be loaded.
     */
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

    /**
     * Adds all fields from a parent class to this class, establishing inheritance.
     * This method recursively processes the parent hierarchy.
     *
     * @param parent The parent JsonClass to inherit fields from.
     * @throws IllegalArgumentException If the parent would create a circular dependency
     *         or if the class attempts to be its own parent.
     */
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

    /**
     * Creates a type descriptor header for this class for model introspection.
     * Includes the class name, node type, permitted values (for enums), and skipping nulls configuration.
     *
     * @param modelDescriptor The model descriptor context.
     * @return A JsonTypeDescriptor for this class.
     */
    public JsonTypeDescriptor describeHead(JsonModelDescriptor modelDescriptor) {
        return new JsonTypeDescriptor(cName)
                .withNodeType(nodeType)
                .withPermittedValues(builder.permittedValues(valuesArray))
                .withSkippingNulls(skippingNulls);
    }

    /**
     * Describes all dependencies of this class for model introspection.
     * This includes parent class relationships, field types, and their dependencies.
     *
     * @param context The model descriptor context to add dependencies to.
     */
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

            // Ensure dependency if this field references a JsonClass
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
