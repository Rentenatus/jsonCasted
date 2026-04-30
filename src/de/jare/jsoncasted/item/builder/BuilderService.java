/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
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
    private final boolean throwClassEx;
    private final Map<String, Object> builtObjectsByWoodKey = new HashMap<>();

    public BuilderService(JsonModel model, boolean throwClassEx) {
        this.model = model;
        this.throwClassEx = throwClassEx;
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
        if (jType != null) {
            return jType.build(jsonValue, this);
        }
        JsonInter jInter = model.getJsonInter(typeName);
        if (jInter != null) {
            buildException("JsonClass " + typeName + " needs a casting.");
            return null;
        }
        buildException("JsonClass " + typeName + " is unknown.");
        return null;
    }

    public Object buildList(JsonItem jsonValue, boolean asList, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType != null) {
            return jType.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
        }
        JsonClass jEnum = model.getJsonEnum(typeName);
        if (jEnum != null) {
            return jEnum.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
        }
        JsonInter jInter = model.getJsonInter(typeName);
        if (jInter != null) {
            return jInter.build(this, jsonValue.listIterator(), asList, jsonValue.listSize());
        }
        buildException("JsonClass " + typeName + " is unknown.");
        return null;
    }

    public Object buildValue(JsonItem jsonValue, JsonTypeDescriptor contextClass) throws JsonBuildException {
        final String typeName = contextClass.getTypeName();
        JsonClass jType = model.getJsonClass(typeName);
        if (jType != null) {
            return jType.build(jsonValue, this);
        }
        JsonClass jEnum = model.getJsonEnum(typeName);
        if (jEnum != null) {
            return jEnum.build(jsonValue, this);
        }
        buildException("JsonClass " + typeName + " is unknown.");
        return null;

    }

    private void buildException(final String msg) throws JsonBuildException {
        Logger.getGlobal().log(Level.SEVERE, msg);
        if (throwClassEx) {
            throw new JsonBuildException(msg);
        }
    }
}
