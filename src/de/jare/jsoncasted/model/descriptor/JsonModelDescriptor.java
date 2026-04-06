package de.jare.jsoncasted.model.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Beschreibung eines kompletten JsonModel.
 *
 * Diese Klasse ist zugleich die Registry aller bereits beschriebenen Typen.
 *
 * @author Janusch Rentenatus
 */
public class JsonModelDescriptor {

    private final String modelName;
    private final Map<String, JsonTypeDescriptor> describedTypes = new LinkedHashMap<>();

    public JsonModelDescriptor(String modelName) {
        this.modelName = Objects.requireNonNull(modelName, "modelName");
    }

    // -------------------------------------------------------------------------
    // Basisdaten
    // -------------------------------------------------------------------------
    public String getModelName() {
        return modelName;
    }

    public int size() {
        return describedTypes.size();
    }

    public boolean isEmpty() {
        return describedTypes.isEmpty();
    }

    public boolean isNotEmpty() {
        return !describedTypes.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Query / Lookup
    // -------------------------------------------------------------------------
    public boolean containsType(String typeName) {
        return typeName != null && describedTypes.containsKey(typeName);
    }

    public boolean isDescribed(String typeName) {
        return containsType(typeName);
    }

    public JsonTypeDescriptor getType(String typeName) {
        if (typeName == null) {
            return null;
        }
        return describedTypes.get(typeName);
    }

    public JsonTypeDescriptor requireType(String typeName) {
        JsonTypeDescriptor type = describedTypes.get(typeName);
        if (type == null) {
            throw new IllegalStateException("Unknown descriptor type: " + typeName);
        }
        return type;
    }

    public JsonTypeDescriptor getOrDefault(String typeName, JsonTypeDescriptor fallback) {
        return describedTypes.getOrDefault(typeName, fallback);
    }

    // -------------------------------------------------------------------------
    // Registrierung
    // -------------------------------------------------------------------------
    /**
     * Fügt einen Typ hinzu, falls der Name noch nicht registriert ist. Bereits
     * vorhandene Typen bleiben unverändert.
     *
     * @param type
     * @return
     */
    public JsonModelDescriptor addType(JsonTypeDescriptor type) {
        Objects.requireNonNull(type, "type");
        describedTypes.putIfAbsent(type.getTypeName(), type);
        return this;
    }

    /**
     * Registriert den Typ und liefert den tatsächlich gespeicherten Eintrag
     * zurück.
     *
     * @param type
     * @return
     */
    public JsonTypeDescriptor registerAndGet(JsonTypeDescriptor type) {
        Objects.requireNonNull(type, "type");
        describedTypes.putIfAbsent(type.getTypeName(), type);
        return describedTypes.get(type.getTypeName());
    }

    /**
     * Fügt alle Typen hinzu, bestehende bleiben erhalten.
     *
     * @param types
     * @return
     */
    public JsonModelDescriptor addAll(Collection<JsonTypeDescriptor> types) {
        Objects.requireNonNull(types, "types");
        for (JsonTypeDescriptor type : types) {
            addType(type);
        }
        return this;
    }

    /**
     * Erzeugt einen Typ über die Factory nur dann, wenn er noch nicht
     * existiert.
     *
     * @param typeName
     * @param supplier
     * @return
     */
    public JsonTypeDescriptor computeIfAbsent(String typeName,
            java.util.function.Supplier<JsonTypeDescriptor> supplier) {
        Objects.requireNonNull(typeName, "typeName");
        Objects.requireNonNull(supplier, "supplier");

        JsonTypeDescriptor current = describedTypes.get(typeName);
        if (current != null) {
            return current;
        }

        JsonTypeDescriptor created = Objects.requireNonNull(supplier.get(), "supplier result");
        if (!typeName.equals(created.getTypeName())) {
            throw new IllegalStateException("Type name mismatch: key=" + typeName
                    + ", descriptor=" + created.getTypeName());
        }

        describedTypes.putIfAbsent(typeName, created);
        return describedTypes.get(typeName);
    }

    // -------------------------------------------------------------------------
    // Entfernen / Leeren
    // -------------------------------------------------------------------------
    public JsonTypeDescriptor removeType(String typeName) {
        if (typeName == null) {
            return null;
        }
        return describedTypes.remove(typeName);
    }

    public boolean removeType(JsonTypeDescriptor type) {
        if (type == null) {
            return false;
        }
        return describedTypes.remove(type.getTypeName(), type);
    }

    public void clear() {
        describedTypes.clear();
    }

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------
    public List<JsonTypeDescriptor> getTypes() {
        return Collections.unmodifiableList(new ArrayList<>(describedTypes.values()));
    }

    public Collection<JsonTypeDescriptor> values() {
        return Collections.unmodifiableCollection(describedTypes.values());
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(describedTypes.keySet());
    }

    public Set<Map.Entry<String, JsonTypeDescriptor>> entrySet() {
        return Collections.unmodifiableSet(describedTypes.entrySet());
    }

    public Map<String, JsonTypeDescriptor> getTypeMap() {
        return Collections.unmodifiableMap(describedTypes);
    }

    // -------------------------------------------------------------------------
    // Validierung
    // -------------------------------------------------------------------------
    public void validate() {
        if (modelName.isBlank()) {
            throw new IllegalStateException("modelName is blank");
        }

        for (Map.Entry<String, JsonTypeDescriptor> entry : describedTypes.entrySet()) {
            String key = entry.getKey();
            JsonTypeDescriptor type = entry.getValue();

            if (type == null) {
                throw new IllegalStateException("Descriptor type is null for key: " + key);
            }

            if (!key.equals(type.getTypeName())) {
                throw new IllegalStateException("Descriptor key mismatch: key=" + key
                        + ", typeName=" + type.getTypeName());
            }

            type.validate();
        }
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden
    // -------------------------------------------------------------------------
    public List<String> getTypeNames() {
        return Collections.unmodifiableList(new ArrayList<>(describedTypes.keySet()));
    }

    public boolean containsAllTypeNames(Collection<String> typeNames) {
        if (typeNames == null) {
            return false;
        }
        return describedTypes.keySet().containsAll(typeNames);
    }

    @Override
    public String toString() {
        return "JsonModelDescriptor[modelName=" + modelName
                + ", types=" + describedTypes.size() + "]";
    }
}
