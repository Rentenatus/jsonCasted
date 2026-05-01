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
 * Description of a complete JsonModel.
 *
 * <p>This class serves as the registry of all described types in a model.
 * It provides methods for adding, looking up, and managing type descriptors,
 * as well as validation of the complete model structure.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Type registration and lookup by name</li>
 *   <li>Support for perceptual type matching (handling simple vs. qualified names)</li>
 *   <li>Validation of type consistency</li>
 *   <li>Unmodifiable views of all registered types</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class JsonModelDescriptor {

    private final String modelName;
    private final Map<String, JsonTypeDescriptor> describedTypes = new LinkedHashMap<>();

    /**
     * Constructs a model descriptor with the specified model name.
     *
     * @param modelName the name of the model (must not be null).
     */
    public JsonModelDescriptor(String modelName) {
        this.modelName = Objects.requireNonNull(modelName, "modelName");
    }

    // -------------------------------------------------------------------------
    // Base data
    // -------------------------------------------------------------------------

    /**
     * Returns the model name.
     *
     * @return the model name.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Returns the number of described types.
     *
     * @return the type count.
     */
    public int size() {
        return describedTypes.size();
    }

    /**
     * Checks if this model has no described types.
     *
     * @return {@code true} if empty.
     */
    public boolean isEmpty() {
        return describedTypes.isEmpty();
    }

    /**
     * Checks if this model has at least one described type.
     *
     * @return {@code true} if not empty.
     */
    public boolean isNotEmpty() {
        return !describedTypes.isEmpty();
    }

    // -------------------------------------------------------------------------
    // Query / Lookup
    // -------------------------------------------------------------------------

    /**
     * Checks if a type with the specified name is registered.
     *
     * @param typeName the type name to check.
     * @return {@code true} if the type is registered.
     */
    public boolean containsType(String typeName) {
        return typeName != null && describedTypes.containsKey(typeName);
    }

    /**
     * Checks if a type with the specified name is described.
     *
     * @param typeName the type name to check.
     * @return {@code true} if the type is described.
     */
    public boolean isDescribed(String typeName) {
        return containsType(typeName);
    }

    /**
     * Returns the type descriptor for the specified type name.
     *
     * @param typeName the type name to look up.
     * @return the type descriptor, or {@code null} if not found.
     */
    public JsonTypeDescriptor getType(String typeName) {
        if (typeName == null) {
            return null;
        }
        return describedTypes.get(typeName);
    }

    /**
     * Returns the type descriptor for the specified type name with perceptual matching.
     *
     * <p>Perceptual matching attempts to find types even if the name doesn't match exactly,
     * for example by matching simple names against fully qualified names.</p>
     *
     * @param typeName the type name to look up.
     * @return the type descriptor, or {@code null} if not found.
     */
    public JsonTypeDescriptor getTypePerceptive(String typeName) {
        if (typeName == null) {
            return null;
        }
        final JsonTypeDescriptor ret = describedTypes.getOrDefault(typeName, null);
        if (ret != null) {
            return ret;
        }
        if (typeName.contains(".")) {
            return null;
        }
        String endingName = "." + typeName;
        for (String key : describedTypes.keySet()) {
            if (key.endsWith(endingName)) {
                return getType(key);
            }
        }
        return null;
    }

    /**
     * Returns the type descriptor for the specified type name.
     *
     * @param typeName the type name to look up.
     * @return the type descriptor.
     * @throws IllegalStateException if the type is not found.
     */
    public JsonTypeDescriptor requireType(String typeName) {
        JsonTypeDescriptor type = describedTypes.get(typeName);
        if (type == null) {
            throw new IllegalStateException("Unknown descriptor type: " + typeName);
        }
        return type;
    }

    /**
     * Returns the type descriptor for the specified type name, or a fallback if not found.
     *
     * @param typeName the type name to look up.
     * @param fallback the fallback descriptor to return if not found.
     * @return the type descriptor or fallback.
     */
    public JsonTypeDescriptor getOrDefault(String typeName, JsonTypeDescriptor fallback) {
        return describedTypes.getOrDefault(typeName, fallback);
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Adds a type if the name is not already registered.
     *
     * @param type the type descriptor to add.
     * @return this model descriptor.
     */
    public JsonModelDescriptor addType(JsonTypeDescriptor type) {
        Objects.requireNonNull(type, "type");
        describedTypes.putIfAbsent(type.getTypeName(), type);
        return this;
    }

    /**
     * Registers the type and returns the actually stored entry.
     *
     * @param type the type descriptor to register.
     * @return the stored type descriptor.
     */
    public JsonTypeDescriptor registerAndGet(JsonTypeDescriptor type) {
        Objects.requireNonNull(type, "type");
        describedTypes.putIfAbsent(type.getTypeName(), type);
        return describedTypes.get(type.getTypeName());
    }

    /**
     * Adds all types, existing ones remain unchanged.
     *
     * @param types the collection of type descriptors to add.
     * @return this model descriptor.
     */
    public JsonModelDescriptor addAll(Collection<JsonTypeDescriptor> types) {
        Objects.requireNonNull(types, "types");
        for (JsonTypeDescriptor type : types) {
            addType(type);
        }
        return this;
    }

    /**
     * Creates a type via the factory only if it doesn't exist yet.
     *
     * @param typeName the name of the type.
     * @param supplier the supplier to create the type descriptor.
     * @return the type descriptor (existing or newly created).
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
    // Remove / Clear
    // -------------------------------------------------------------------------

    /**
     * Removes a type by its name.
     *
     * @param typeName the name of the type to remove.
     * @return the removed type descriptor, or {@code null} if not found.
     */
    public JsonTypeDescriptor removeType(String typeName) {
        if (typeName == null) {
            return null;
        }
        return describedTypes.remove(typeName);
    }

    /**
     * Removes a type descriptor.
     *
     * @param type the type descriptor to remove.
     * @return {@code true} if the type was removed.
     */
    public boolean removeType(JsonTypeDescriptor type) {
        if (type == null) {
            return false;
        }
        return describedTypes.remove(type.getTypeName(), type);
    }

    /**
     * Removes all types.
     */
    public void clear() {
        describedTypes.clear();
    }

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable list of all type descriptors.
     *
     * @return list of all types.
     */
    public List<JsonTypeDescriptor> getTypes() {
        return Collections.unmodifiableList(new ArrayList<>(describedTypes.values()));
    }

    /**
     * Returns an unmodifiable collection of all type descriptors.
     *
     * @return collection of all types.
     */
    public Collection<JsonTypeDescriptor> values() {
        return Collections.unmodifiableCollection(describedTypes.values());
    }

    /**
     * Returns an unmodifiable set of all type names.
     *
     * @return set of type names.
     */
    public Set<String> keySet() {
        return Collections.unmodifiableSet(describedTypes.keySet());
    }

    /**
     * Returns an unmodifiable set of type entries.
     *
     * @return set of entries.
     */
    public Set<Map.Entry<String, JsonTypeDescriptor>> entrySet() {
        return Collections.unmodifiableSet(describedTypes.entrySet());
    }

    /**
     * Returns an unmodifiable map of all types.
     *
     * @return map of type names to descriptors.
     */
    public Map<String, JsonTypeDescriptor> getTypeMap() {
        return Collections.unmodifiableMap(describedTypes);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Validates this model descriptor.
     *
     * @throws IllegalStateException if validation fails.
     */
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
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable list of all type names.
     *
     * @return list of type names.
     */
    public List<String> getTypeNames() {
        return Collections.unmodifiableList(new ArrayList<>(describedTypes.keySet()));
    }

    /**
     * Checks if all specified type names are contained in this model.
     *
     * @param typeNames the collection of type names to check.
     * @return {@code true} if all type names are present.
     */
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
