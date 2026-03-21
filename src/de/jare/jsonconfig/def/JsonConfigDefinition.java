/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonBuildException;
import static de.jare.jsoncasted.model.JsonCollectionType.ARRAY;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonReflectBuilder;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import static de.jare.jsoncasted.parserwriter.JsonValidationMethod.ENDSWITH;
import static de.jare.jsoncasted.parserwriter.JsonValidationMethod.EQUALS;
import de.jare.jsonconfig.item.ConfigFeature;
import de.jare.jsonconfig.item.ConfigLogging;
import de.jare.jsonconfig.item.ConfigProfile;
import de.jare.jsonconfig.item.ConfigProfileType;
import de.jare.jsonconfig.item.ConfigRoot;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonConfigDefinition implements JsonItemDefinition {

    public static final JsonConfigDefinition INSTANCE = new JsonConfigDefinition();

    public static JsonConfigDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass configRoot;

    public JsonConfigDefinition() {
        model = new JsonModel("Seed");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");

        JsonClass profileType = model.newJsonEnumByName(ConfigProfileType.class);

        JsonMap stringMap = model.newRawJsonMap((new JsonInstance<String>()).getClass(), asString);
        JsonMap stringArrayMap = model.newRawJsonMap((new JsonInstance<String[]>()).getClass(), asString, ARRAY);
        JsonMap booleanMap = model.newRawJsonMap((new JsonInstance<Boolean>()).getClass(), asBoolean);

        JsonClass logging = model.newJsonReflect(ConfigLogging.class);

        logging.addField("level", asString);
        logging.addField("path", asString);

        JsonClass feature = model.newJsonClass(ConfigFeature.class, new JsonReflectBuilder(ConfigFeature.class) {
            @Override
            public List<ConfigFeature> buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException {
                ArrayList<ConfigFeature> list = new ArrayList<>(size);
                while (listIterator.hasNext()) {
                    JsonItem next = listIterator.next();
                    Object elem = next.buildInstance();
                    list.add((ConfigFeature) elem);
                }
                return list;
            }
        });

        feature.addField("featureLogging", logging);
        feature.addField("feature", asString, ENDSWITH);
        feature.addField("comments", asString, ARRAY);
        feature.addField("settings", stringMap);
        feature.addField("labels", stringArrayMap);
        feature.addField("enablements", booleanMap);

        JsonClass profile = model.newJsonReflect(ConfigProfile.class);

        profile.addField("profileLogging", logging);
        profile.addField("profile", asString, EQUALS);
        profile.addField("type", profileType);
        profile.addField("comments", asString, ARRAY);
        profile.addField("features", feature, LIST);

        configRoot = model.newJsonReflect(ConfigRoot.class);

        configRoot.addField("mainLogging", logging);
        configRoot.addField("comments", asString, ARRAY);
        configRoot.addField("profiles", profile, LIST);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    public JsonClass getConfigRoot() {
        return configRoot;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
