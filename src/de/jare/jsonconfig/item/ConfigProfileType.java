/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonconfig.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Type of Config Object.
 *
 * @author Janusch Rentenatu
 */
public enum ConfigProfileType {

    NONE(0, "NONE"),
    DEV(3, "DEV"),
    TEST(4, "TEST"),
    PREPROD(5, "PREPROD"),
    PROD(6, "PROD");

    private static final ConfigProfileType[] VALUES_ARRAY = new ConfigProfileType[]{NONE, DEV, TEST, PREPROD, PROD};

    public static final List<ConfigProfileType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    public static ConfigProfileType getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            ConfigProfileType result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    public static ConfigProfileType get(int value) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            ConfigProfileType result = VALUES_ARRAY[i];
            if (result.getValue() == value) {
                return result;
            }
        }
        return null;
    }

    private final int value;

    private final String name;

    private ConfigProfileType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
