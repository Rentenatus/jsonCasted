/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonconfig.item;

import java.util.List;

/**
 * Main Json Config Object.
 *
 * @author Janusch Rentenatu
 */
public class ConfigProfile {

    private String[] comments;
    private String profile;
    private ConfigProfileType type;
    private ConfigLogging profileLogging;
    private List<ConfigFeature> features;

    public String[] getComments() {
        return comments;
    }

    public void setComments(String[] comments) {
        this.comments = comments;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public ConfigProfileType getType() {
        return type;
    }

    public void setType(ConfigProfileType type) {
        this.type = type;
    }

    public ConfigLogging getProfileLogging() {
        return profileLogging;
    }

    public void setProfileLogging(ConfigLogging profileLogging) {
        this.profileLogging = profileLogging;
    }

    public List<ConfigFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<ConfigFeature> features) {
        this.features = features;
    }

}
