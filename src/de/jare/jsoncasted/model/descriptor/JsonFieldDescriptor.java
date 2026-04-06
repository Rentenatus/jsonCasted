package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.model.JsonCollectionType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

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

    private final String jsonName;
    private final String typeName;
    private final JsonCollectionType collectionType;
    private final boolean required;
    private final boolean constructorParam;
    private String getter;
    private String setter;

    public JsonFieldDescriptor(String jsonName, String typeName) {
        this(jsonName, typeName, null, false, false, null, null);
    }

    public JsonFieldDescriptor(String jsonName,
            String typeName,
            JsonCollectionType collectionType,
            boolean required,
            boolean constructorParam,
            String getter,
            String setter) {
        this.jsonName = Objects.requireNonNull(jsonName, "jsonName");
        this.typeName = Objects.requireNonNull(typeName, "typeName");
        this.collectionType = collectionType;
        this.required = required;
        this.constructorParam = constructorParam;
        this.getter = getter;
        this.setter = setter;
    }

    public String getJsonName() {
        return jsonName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Optional<JsonCollectionType> getCollectionType() {
        return Optional.ofNullable(collectionType);
    }

    public boolean isCollection() {
        return collectionType != null;
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
        if (jsonName.isBlank()) {
            throw new IllegalStateException("jsonName is blank");
        }
        if (typeName.isBlank()) {
            throw new IllegalStateException("typeName is blank");
        }
        if (constructorParam && setter != null) {
            throw new IllegalStateException("Constructor param must not have a setter: " + jsonName);
        }
        if (!constructorParam && getter != null && setter == null) {
            throw new IllegalStateException("Setter field without setter: " + jsonName);
        }
    }

    @Override
    public String toString() {
        return "JsonFieldDescriptor["
                + "jsonName=" + jsonName
                + ", typeName=" + typeName
                + ", collectionType=" + collectionType
                + ", required=" + required
                + ", constructorParam=" + constructorParam
                + ", getter=" + getter
                + ", setter=" + setter
                + "]";
    }
}
