/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import de.jare.jsoncasted.model.JsonEnumTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Role of a project partition (concept: CONCEPT_ProjectView.md, section 2). The type is the
 * semantic role of a level-1 mount; the UI derives icon, color and behavior from it and never
 * from guessing path conventions.
 *
 * @author Janusch Rentenatus
 */
public enum PartitionType implements JsonEnumTemplate {

    SRC("src"),
    TEST("test"),
    ASSETS("assets"),
    JSON("json"),
    GEN("gen"),
    TARGET("target");

    private static final PartitionType[] VALUES_ARRAY = new PartitionType[]{SRC, TEST, ASSETS, JSON, GEN, TARGET};

    public static final List<PartitionType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    public static PartitionType getByName(String name) {
        if (name == null) {
            return null;
        }
        for (PartitionType result : VALUES_ARRAY) {
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    private final String name;

    private PartitionType(String name) {
        this.name = name;
    }

    @Override
    public String getLiteral() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

}
