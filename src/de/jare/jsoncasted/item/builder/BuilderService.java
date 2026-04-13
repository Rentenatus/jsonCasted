package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonInter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuilderService {

    private final JsonModel model;
    private final Map<String, Object> builtObjectsByWoodKey = new HashMap<>();

    public BuilderService(JsonModel model) {
        this.model = model;
    }

    public JsonModel getModel() {
        return model;
    }

    public Object build(JsonItem item) throws JsonBuildException {
        return item == null ? null : item.buildInstance(this);
    }

    public Object getBuiltObject(String woodKey) {
        return woodKey == null ? null : builtObjectsByWoodKey.get(woodKey);
    }

    public void putBuiltObject(String woodKey, Object value) {
        if (woodKey != null && value != null) {
            builtObjectsByWoodKey.put(woodKey, value);
        }
    }

    public Object getOrBuild(JsonObject jsonObject, JsonTypeDescriptor contextClass) throws JsonBuildException {
        String woodKey = jsonObject.getWoodKey();
        if (woodKey != null) {
            Object cached = builtObjectsByWoodKey.get(woodKey);
            if (cached != null) {
                return cached;
            }
        }

        Object built = buildObject(jsonObject, contextClass);
        if (woodKey != null && built != null) {
            builtObjectsByWoodKey.put(woodKey, built);
        }
        return built;
    }

    public Object buildObject(JsonItem jsonValue, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType == null) {
            JsonInter jInter = model.getJsonInter(typeName);
            if (jInter != null) {
                Logger.getGlobal().log(Level.SEVERE, "JsonClass {0} needs a casting.", new Object[]{typeName});
                return null;
            }
            Logger.getGlobal().log(Level.SEVERE, "JsonClass {0} is unknown.", new Object[]{typeName});
            return null;
        }
        return jType.build(jsonValue, this);
    }

    public Object buildList(JsonItem jsonValue, boolean asList, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType == null) {
            JsonInter jInter = model.getJsonInter(typeName);
            if (jInter != null) {
                return jInter.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
            }
            Logger.getGlobal().log(Level.SEVERE, "JsonClass {0} is unknown.", new Object[]{typeName});
            return null;
        }
        return jType.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
    }

    public Object buildValue(JsonItem jsonValue, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType == null) {
            return null;
        }
        return jType.build(jsonValue, this);
    }
}
