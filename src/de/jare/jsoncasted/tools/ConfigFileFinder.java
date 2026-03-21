/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.tools;

import java.io.File;

/**
 * The ConfigFileFinder interface provides utility methods for locating
 * configuration files. It determines the correct file path based on system
 * properties and expected locations.
 *
 * @author Janusch Rentenatus
 */
public interface ConfigFileFinder {

    /**
     * Calculates the configuration file location using the default system
     * property key "config.path".
     *
     * @param expectedPath The expected base path for the configuration file.
     * @param filename The name of the configuration file.
     * @return A File object pointing to the resolved configuration file
     * location.
     */
    public default File calculateConfigFile(String expectedPath, String filename) {
        return calculateConfigFile("config.path", expectedPath, filename);
    }

    /**
     * Calculates the configuration file location based on a given system
     * property key. If the system property is undefined or empty, it falls back
     * to the expected path.
     *
     * @param propertyKey The system property key used to define the
     * configuration path.
     * @param expectedPath The fallback base path for the configuration file.
     * @param filename The name of the configuration file.
     * @return A File object pointing to the resolved configuration file
     * location.
     */
    public default File calculateConfigFile(String propertyKey, String expectedPath, String filename) {
        String configPath = System.getProperty(propertyKey);
        File ret;

        if (configPath == null || configPath.isEmpty()) {
            if (!expectedPath.endsWith("/") && !expectedPath.endsWith("\\")) {
                configPath = expectedPath + "/";
            } else {
                configPath = expectedPath;
            }
            ret = new File(configPath + filename);

            // If the file does not exist and the path does not contain a drive letter, check the parent directory
            if (!ret.exists() && !configPath.contains(":")) {
                ret = new File("../" + configPath + filename);
            }
        } else {
            if (!configPath.endsWith("/") && !configPath.endsWith("\\")) {
                configPath = configPath + "/";
            }
            ret = new File(configPath + filename);
        }

        return ret;
    }
}
