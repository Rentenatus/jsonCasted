/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonDefinitions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class DefinitionsContext {

    private final JsonModel model;

    // The object definitions pre-baked during the first pass are stored here.
    private final Map<JsonDefinitions, Object> woodDefinitions = new HashMap<>();

    // All objects found that are candidates for a reference ID are collected here.
    // Unless they are already in the pre-baked map.
    private final List<Object> candidates = new ArrayList<>();

    // All objects found that are going to be serialized are collected here. They
    // will be removed from the list of candidates.
    private final List<Object> findings = new ArrayList<>();

    public DefinitionsContext(de.jare.jsoncasted.model.JsonModel model) {
        this.model = model;
    }

    public JsonModel getModel() {
        return model;
    }
}
