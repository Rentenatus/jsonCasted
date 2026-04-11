package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.FieldKind;
import java.util.Objects;

/**
 * Beschreibt ein Feld bzw. Property eines JSON-mapbaren Typs.
 *
 * constructorParam: - besitzt typischerweise nur einen Getter - wird beim
 * Instanziieren über den Konstruktor gesetzt
 *
 * field: - besitzt Getter und Setter - wird nach der Konstruktion gesetzt
 *
 * @author Janusch Rentenatus
 */
public class JsonFieldDescriptor {

    private final String fieldName;
    private final String typeName;
    private final JsonCollectionType collectionType;
    private final boolean required;
    private final boolean constructorParam;
    private String getter;
    private String setter;
    private FieldKind kind;

    public JsonFieldDescriptor(String jsonName, String typeName) {
        this(jsonName, typeName, null, false, false, null, null);
    }

    public JsonFieldDescriptor(String fieldName,
            String typeName,
            JsonCollectionType collectionType,
            boolean required,
            boolean constructorParam,
            String getter,
            String setter) {
        this.fieldName = Objects.requireNonNull(fieldName, "jsonName");
        this.typeName = Objects.requireNonNull(typeName, "typeName");
        this.collectionType = collectionType;
        this.required = required;
        this.constructorParam = constructorParam;
        this.getter = getter;
        this.setter = setter;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTypeName() {
        return typeName;
    }

    public JsonCollectionType getCollectionType() {
        return collectionType;
    }

    public boolean isNotCollection() {
        return collectionType == JsonCollectionType.NONE;
    }

    public boolean isAsListOrArray() {
        return collectionType == JsonCollectionType.ARRAY || collectionType == JsonCollectionType.LIST;
    }

    public boolean isAsList() {
        return collectionType == JsonCollectionType.LIST;
    }

    public boolean isAsArray() {
        return collectionType == JsonCollectionType.ARRAY;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isConstructorParam() {
        return constructorParam;
    }

    public boolean isSetterField() {
        return !constructorParam;
    }

    public void validate() {
        if (fieldName.isBlank()) {
            throw new IllegalStateException("jsonName is blank");
        }
        if (typeName.isBlank()) {
            throw new IllegalStateException("typeName is blank");
        }
        if (constructorParam && setter != null) {
            throw new IllegalStateException("Constructor param must not have a setter: " + fieldName);
        }
        if (!constructorParam && getter != null && setter == null) {
            throw new IllegalStateException("Setter field without setter: " + fieldName);
        }
    }

    public FieldKind getKind() {
        return kind;
    }

    public void setKind(FieldKind kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "JsonFieldDescriptor["
                + "jsonName=" + fieldName
                + ", typeName=" + typeName
                + ", collectionType=" + collectionType
                + ", required=" + required
                + ", kind=" + kind.getName()
                + ", constructorParam=" + constructorParam
                + ", getter=" + getter
                + ", setter=" + setter
                + "]";
    }

}
