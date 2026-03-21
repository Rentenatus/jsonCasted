/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonconfig;

import de.jare.jsonconfig.def.JsonConfigDefinition;
import de.jare.jsonconfig.item.ConfigFeature;
import de.jare.jsonconfig.item.ConfigLogging;
import de.jare.jsonconfig.item.ConfigProfile;
import de.jare.jsonconfig.item.ConfigRoot;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.tools.SimpleStringSplitter;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonConfigHelper implements SimpleStringSplitter {

    private ConfigRoot root = null;
    private Map<String, ConfigProfile> profiles;
    private String mainProfile = "main";
    private ConfigLogging mainLogging;

    public JsonConfigHelper(File configFile) {
        root = null;
        profiles = new HashMap<>();
        mainLogging = null;
        JsonConfigDefinition definition = JsonConfigDefinition.getInstance();
        JsonItem obj1 = null;
        try {
            obj1 = JsonParser.parse(configFile, definition, definition.getConfigRoot());
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        try {
            final Object buildInstance1 = obj1.buildInstance();
            System.out.println(buildInstance1.getClass().getName());
            root = (ConfigRoot) buildInstance1;
        } catch (JsonBuildException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        if (root == null) {
            return;
        }
        for (ConfigProfile p : root.getProfiles()) {
            profiles.put(p.getProfile(), p);
        }
        mainLogging = root.getMainLogging();
    }

    public JsonConfigHelper(ConfigRoot root) {
        profiles = new HashMap<>();
        this.root = root;
        for (ConfigProfile p : root.getProfiles()) {
            profiles.put(p.getProfile(), p);
        }
        mainLogging = root.getMainLogging();
    }

    public ConfigProfile getProfile(String profile) {
        if (profiles.containsKey(profile)) {
            return profiles.get(profile);
        }
        return profiles.get(mainProfile);
    }

    public ConfigFeature getFeature(String profile, String feature) {
        ConfigProfile p = getProfile(profile);
        return getFeature(p, feature);
    }

    private ConfigFeature getFeature(ConfigProfile p, String feature) {
        for (ConfigFeature f : p.getFeatures()) {
            if (feature.equals(f.getFeature())) {
                return f;
            }
        }
        return null;
    }

    public ConfigLogging getLogging(String profile) {
        return getLogging(getProfile(profile));
    }

    public ConfigLogging getLogging(ConfigProfile profile) {
        if (profile.getProfileLogging() != null) {
            return profile.getProfileLogging();
        }
        return getMainLogging();
    }

    public ConfigLogging getLogging(String profile, String feature) {
        ConfigProfile p = getProfile(profile);
        ConfigFeature f = getFeature(p, feature);
        return getLogging(p, f);
    }

    public ConfigLogging getLogging(ConfigProfile profile, ConfigFeature feature) {
        if (feature != null && feature.getFeatureLogging() != null) {
            return feature.getFeatureLogging();
        }
        if (profile != null && profile.getProfileLogging() != null) {
            return profile.getProfileLogging();
        }
        return getMainLogging();
    }

    public ConfigLogging getMainLogging() {
        return mainLogging;
    }

    public String getSetting(ConfigFeature feature, String parameter) {
        final JsonInstance<String> settings = feature.getSettings();
        if (settings != null && settings.containsKey(parameter)) {
            return settings.get(parameter);
        }
        return null;
    }

    public String getLabel(ConfigFeature feature, String parameter) {
        final JsonInstance<String[]> labels = feature.getLabels();
        if (labels != null && labels.containsKey(parameter)) {
            return simpleConcat(labels.get(parameter), "");
        }
        return null;
    }

    public Boolean getEnablement(ConfigFeature feature, String parameter) {
        final JsonInstance<Boolean> enablements = feature.getEnablements();
        if (enablements != null && enablements.containsKey(parameter)) {
            return enablements.get(parameter);
        }
        return null;
    }
}
