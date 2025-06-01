/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonconfig.item;

import de.jare.jsoncasted.lang.JsonInstance;

/**
 * Feature Object.
 *
 * @author Janusch Rentenatu
 */
public class ConfigFeature {

    private ConfigLogging featureLogging;
    private String feature;
    private String[] comments;
    private JsonInstance<String> settings;
    private JsonInstance<String[]> labels;
    private JsonInstance<Boolean> enablements;

    public ConfigLogging getFeatureLogging() {
        return featureLogging;
    }

    public void setFeatureLogging(ConfigLogging featureLogging) {
        this.featureLogging = featureLogging;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String[] getComments() {
        return comments;
    }

    public void setComments(String[] comments) {
        this.comments = comments;
    }

    public JsonInstance<String> getSettings() {
        return settings;
    }

    public void setSettings(JsonInstance<String> settings) {
        this.settings = settings;
    }

    public JsonInstance<String[]> getLabels() {
        return labels;
    }

    public void setLabels(JsonInstance<String[]> labels) {
        this.labels = labels;
    }

    public JsonInstance<Boolean> getEnablements() {
        return enablements;
    }

    public void setEnablements(JsonInstance<Boolean> enablements) {
        this.enablements = enablements;
    }

}
