package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.lang.JsonNodeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Allgemeine Beschreibung eines JSON-mapbaren Typs.
 * Kann sowohl für reflektive als auch für rein beschreibende Typen verwendet werden.
 *
 * @author Janusch Rentenatus
 */
public class JsonTypeDescriptor {

    private final String typeName;
    private final List<JsonFieldDescriptor> constructorParams = new ArrayList<>();
    private final List<JsonFieldDescriptor> fields = new ArrayList<>();

    private String javaClassName;
    private JsonNodeType nodeType;
    private boolean skippingNulls;
    private boolean primitive;
    private boolean reflective;

    public JsonTypeDescriptor(String typeName) {
        this.typeName = Objects.requireNonNull(typeName, "typeName");
    }

    public String getTypeName() {
        return typeName;
    }

    public List<JsonFieldDescriptor> getConstructorParams() {
        return Collections.unmodifiableList(constructorParams);
    }

    public List<JsonFieldDescriptor> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<JsonFieldDescriptor> getAllFields() {
        List<JsonFieldDescriptor> all = new ArrayList<>(constructorParams);
        all.addAll(fields);
        return Collections.unmodifiableList(all);
    }

    public String getJavaClassName() {
        return javaClassName;
    }

    public JsonNodeType getNodeType() {
        return nodeType;
    }

    public boolean isSkippingNulls() {
        return skippingNulls;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isReflective() {
        return reflective;
    }

    public JsonTypeDescriptor addConstructorParam(JsonFieldDescriptor param) {
        constructorParams.add(Objects.requireNonNull(param, "param"));
        return this;
    }

    public JsonTypeDescriptor addField(JsonFieldDescriptor field) {
        fields.add(Objects.requireNonNull(field, "field"));
        return this;
    }

    public JsonTypeDescriptor withJavaClassName(String javaClassName) {
        this.javaClassName = javaClassName;
        this.reflective = javaClassName != null && !javaClassName.isBlank();
        return this;
    }

    public JsonTypeDescriptor withNodeType(JsonNodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public JsonTypeDescriptor withSkippingNulls(boolean skippingNulls) {
        this.skippingNulls = skippingNulls;
        return this;
    }

    public JsonTypeDescriptor withPrimitive(boolean primitive) {
        this.primitive = primitive;
        return this;
    }

    public boolean hasConstructorParams() {
        return !constructorParams.isEmpty();
    }

    public int constructorArity() {
        return constructorParams.size();
    }

    public boolean hasJavaClassName() {
        return javaClassName != null && !javaClassName.isBlank();
    }

    public void validate() {
        if (typeName.isBlank()) {
            throw new IllegalStateException("typeName is blank");
        }
        for (JsonFieldDescriptor param : constructorParams) {
            if (!param.isConstructorParam()) {
                throw new IllegalStateException("Constructor param expected: " + param.getJsonName());
            }
        }
        for (JsonFieldDescriptor field : fields) {
            if (field.isConstructorParam()) {
                throw new IllegalStateException("Setter field expected: " + field.getJsonName());
            }
        }
    }

    @Override
    public String toString() {
        return "JsonTypeDescriptor["
                + "typeName=" + typeName
                + ", javaClassName=" + javaClassName
                + ", nodeType=" + nodeType
                + ", primitive=" + primitive
                + ", reflective=" + reflective
                + ", ctor=" + constructorParams.size()
                + ", fields=" + fields.size()
                + "]";
    }
}