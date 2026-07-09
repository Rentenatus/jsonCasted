/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.lang.JsonNodeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * General description of a JSON-mappable type.
 *
 * <p>
 * This class can be used for both reflective types (discovered via reflection) and purely descriptive types (defined
 * programmatically). It captures all metadata needed to serialize and deserialize objects of this type.</p>
 *
 * <p>
 * A type descriptor contains:</p>
 * <ul>
 * <li>Type name and node type (OBJECT, ARRAY, STRING, etc.)</li>
 * <li>Parent type for inheritance</li>
 * <li>List of implementors (for interfaces)</li>
 * <li>Constructor parameters and regular fields</li>
 * <li>Permitted values (for enums)</li>
 * <li>Flags for skipping nulls, primitives, recursive types</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class JsonTypeDescriptor {

    private final String typeName;
    private final List<JsonTypeDescriptor> implementors = new ArrayList<>();
    private final List<JsonFieldDescriptor> constructorParams = new ArrayList<>();
    private final List<JsonFieldDescriptor> fields = new ArrayList<>();
    private final Map<String, String> permittedValues = new LinkedHashMap();

    private JsonNodeType nodeType;
    private boolean skippingNulls;
    private boolean primitive;
    private boolean recursive;
    /**
     * A mask for all fields, that contains this class.
     *
     * e.g. "*:de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor[]" for an Araay of this class.
     */

    private JsonFieldDescriptor mappingAllFields;
    private JsonTypeDescriptor parent;

    /**
     * Constructs a type descriptor with the specified type name.
     *
     * @param typeName the name of the type (must not be null).
     */
    public JsonTypeDescriptor(String typeName) {
        this.typeName = Objects.requireNonNull(typeName, "typeName");
        this.mappingAllFields = null;
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
     * Returns the parent type descriptor.
     *
     * @return the parent type, or {@code null} if none.
     */
    public JsonTypeDescriptor getParent() {
        return parent;
    }

    /**
     * Sets the parent type descriptor.
     *
     * @param parent the parent type to set.
     */
    public void setParent(JsonTypeDescriptor parent) {
        this.parent = parent;
    }

    /**
     * Checks if this type contains the specified type in its supertype hierarchy.
     *
     * @param suspectedType the type to check for.
     * @return {@code true} if the type is in the hierarchy.
     */
    public boolean containsSuper(JsonTypeDescriptor suspectedType) {
        if (parent == null) {
            return false;
        }
        if (parent.getTypeName().equals(suspectedType.getTypeName()) && parent.getNodeType() == suspectedType.getNodeType()) {
            return true;
        }
        return parent.containsSuper(suspectedType);
    }

    /**
     * Returns the mapping for all fields (used for map types).
     *
     * @return the field descriptor for mapping all fields, or {@code null} if none.
     */
    public JsonFieldDescriptor getMappingAllFields() {
        return mappingAllFields;
    }

    /**
     * Sets the mapping for all fields.
     *
     * @param mappingAllFields the field descriptor to set.
     */
    public void setMappingAllFields(JsonFieldDescriptor mappingAllFields) {
        this.mappingAllFields = mappingAllFields;
    }

    /**
     * Returns an unmodifiable list of constructor parameters.
     *
     * @return the list of constructor parameter descriptors.
     */
    public List<JsonFieldDescriptor> getConstructorParams() {
        return Collections.unmodifiableList(constructorParams);
    }

    /**
     * Returns an unmodifiable list of implementors (for interface types).
     *
     * @return the list of type descriptors that implement this type.
     */
    public List<JsonTypeDescriptor> getImplementors() {
        return Collections.unmodifiableList(implementors);
    }

    /**
     * Checks if this type contains the specified type (by name).
     *
     * @param check the type descriptor to check.
     * @return {@code true} if this type matches or contains the specified type.
     */
    public boolean contains(JsonTypeDescriptor check) {
        if (this == check || this.getTypeName().equals(check.getTypeName())) {
            return true;
        }
        for (JsonTypeDescriptor implementor : implementors) {
            if (implementor == check || implementor.getTypeName().equals(check.getTypeName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an unmodifiable list of all fields.
     *
     * @return the list of field descriptors.
     */
    public List<JsonFieldDescriptor> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /**
     * Returns the permitted values map (for enum types).
     *
     * @return a map of literal strings to names.
     */
    public Map<String, String> getPermittedValues() {
        return permittedValues;
    }

    /**
     * Returns all fields (constructor params + regular fields).
     *
     * @return unmodifiable list of all field descriptors.
     */
    public List<JsonFieldDescriptor> getAllFields() {
        List<JsonFieldDescriptor> all = new ArrayList<>(constructorParams);
        all.addAll(fields);
        return Collections.unmodifiableList(all);
    }

    /**
     * Finds a field by its name.
     *
     * @param paramName the field name to search for.
     * @return the matching field descriptor, or {@code null} if not found.
     */
    public JsonFieldDescriptor getField(String paramName) {
        for (JsonFieldDescriptor field : constructorParams) {
            if (field.getFieldName().equals(paramName)) {
                return field;
            }
        }
        for (JsonFieldDescriptor field : fields) {
            if (field.getFieldName().equals(paramName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Returns the JSON node type for this type.
     *
     * @return the JsonNodeType.
     */
    public JsonNodeType getNodeType() {
        return nodeType;
    }

    /**
     * Checks if null values should be skipped during serialization.
     *
     * @return {@code true} if nulls are skipped.
     */
    public boolean isSkippingNulls() {
        return skippingNulls;
    }

    /**
     * Checks if this is a primitive type.
     *
     * @return {@code true} if primitive.
     */
    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * Checks if this type will be recursive build.
     *
     * @return {@code true} if recursive.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Adds a constructor parameter and returns this descriptor for chaining.
     *
     * @param param the constructor parameter descriptor to add.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor addConstructorParam(JsonFieldDescriptor param) {
        constructorParams.add(Objects.requireNonNull(param, "param"));
        return this;
    }

    /**
     * Adds an implementor and returns this descriptor for chaining.
     *
     * @param implementor the implementing type descriptor to add.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor addImplementor(JsonTypeDescriptor implementor) {
        implementors.add(Objects.requireNonNull(implementor, "implementor"));
        return this;
    }

    /**
     * Adds a field and returns this descriptor for chaining.
     *
     * @param field the field descriptor to add.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor addField(JsonFieldDescriptor field) {
        fields.add(Objects.requireNonNull(field, "field"));
        return this;
    }

    /**
     * Sets the node type and returns this descriptor for chaining.
     *
     * @param nodeType the JSON node type.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor withNodeType(JsonNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    /**
     * Sets whether nulls should be skipped and returns this descriptor for chaining.
     *
     * @param skippingNulls whether to skip nulls.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor withSkippingNulls(boolean skippingNulls) {
        this.skippingNulls = skippingNulls;
        return this;
    }

    /**
     * Sets whether this is a primitive type and returns this descriptor for chaining.
     *
     * @param primitive whether this is primitive.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor withPrimitive(boolean primitive) {
        this.primitive = primitive;
        return this;
    }

    /**
     * Sets whether this is a recursive for chaining.
     *
     * @param recursive whether this is recursive.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor withRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    /**
     * Adds permitted values from a map and returns this descriptor for chaining.
     *
     * @param withPermittedValues the map of literal to name strings.
     * @return this type descriptor.
     */
    public JsonTypeDescriptor withPermittedValues(Map<String, String> withPermittedValues) {
        if (withPermittedValues == null) {
            return this;
        }
        this.permittedValues.putAll(withPermittedValues);
        return this;
    }

    /**
     * Checks if this type has constructor parameters.
     *
     * @return {@code true} if there are constructor parameters.
     */
    public boolean hasConstructorParams() {
        return !constructorParams.isEmpty();
    }

    /**
     * Returns the number of constructor parameters (arity).
     *
     * @return the constructor parameter count.
     */
    public int constructorArity() {
        return constructorParams.size();
    }

    /**
     * Validates this type descriptor.
     *
     * @throws IllegalStateException if validation fails (blank type name, inconsistent field types).
     */
    public void validate() {
        if (typeName.isBlank()) {
            throw new IllegalStateException("typeName is blank");
        }
        for (JsonFieldDescriptor param : constructorParams) {
            if (!param.isConstructorParam()) {
                throw new IllegalStateException("Constructor param expected: " + param.getFieldName());
            }
        }
        for (JsonFieldDescriptor field : fields) {
            if (field.isConstructorParam()) {
                throw new IllegalStateException("Setter field expected: " + field.getFieldName());
            }
        }
    }

    @Override
    public String toString() {
        return "JsonTypeDescriptor["
                + "typeName=" + typeName
                + ", nodeType=" + nodeType
                + ", primitive=" + primitive
                + ", recursive=" + recursive
                + ", ctor=" + constructorParams.size()
                + ", fields=" + fields.size()
                + "]";
    }

}
