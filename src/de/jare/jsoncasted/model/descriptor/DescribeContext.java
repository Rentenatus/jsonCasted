package de.jare.jsoncasted.model.descriptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DescribeContext {

    private final JsonModelDescriptor modelDescriptor;
    private final Map<String, JsonTypeDescriptor> describedTypes = new LinkedHashMap<>();

    public DescribeContext(String modelName) {
        this.modelDescriptor = new JsonModelDescriptor(modelName);
    }

    public JsonModelDescriptor getModelDescriptor() {
        return modelDescriptor;
    }

    public boolean isDescribed(String typeName) {
        return describedTypes.containsKey(typeName);
    }

    public JsonTypeDescriptor requireDescribed(String typeName) {
        JsonTypeDescriptor type = describedTypes.get(typeName);
        if (type == null) {
            throw new IllegalStateException("Type not described yet: " + typeName);
        }
        return type;
    }

    public Optional<JsonTypeDescriptor> getDescribed(String typeName) {
        return Optional.ofNullable(describedTypes.get(typeName));
    }

    public void registerDescribed(JsonTypeDescriptor type) {
        Objects.requireNonNull(type, "type");
        if (!describedTypes.containsKey(type.getTypeName())) {
            describedTypes.put(type.getTypeName(), type);
            modelDescriptor.addType(type);
        }
    }

}
