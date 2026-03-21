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
public class ConfigRoot {

    private String[] comments;
    private ConfigLogging mainLogging;
    private List<ConfigProfile> profiles;

    public String[] getComments() {
        return comments;
    }

    public void setComments(String[] comments) {
        this.comments = comments;
    }

    public ConfigLogging getMainLogging() {
        return mainLogging;
    }

    public void setMainLogging(ConfigLogging mainLogging) {
        this.mainLogging = mainLogging;
    }

    public List<ConfigProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ConfigProfile> profiles) {
        this.profiles = profiles;
    }

}
