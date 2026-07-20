/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonDefinitions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 */
public class DefinitionsContext {

    private final JsonModel model;

    // Atomic counter for generating unique IDs within this context.
    private final AtomicLong idCounter = new AtomicLong(0);

    // The object definitions pre-baked during the first pass are stored here.
    private final Map<JsonDefinitions, Object> woodDefinitions = new HashMap<>();

    // All objects found that are candidates for a reference ID are collected here.
    // Unless they are already in the pre-baked map.
    private final Set<Object> candidates = new HashSet<>();

    // All objects found that are going to be serialized are collected here. They
    // will be removed from the set of candidates.
    private final Set<Object> findings = new HashSet<>();

    public DefinitionsContext(de.jare.jsoncasted.model.JsonModel model) {
        this.model = model;
    }

    public JsonModel getModel() {
        return model;
    }

    /**
     * Checks if an object is already in findings.
     *
     * @param ob the object to check
     * @return true if the object is in findings
     */
    public boolean isInFindings(Object ob) {
        return findings.contains(ob);
    }

    /**
     * Checks if an object is already in candidates.
     *
     * @param ob the object to check
     * @return true if the object is in candidates
     */
    public boolean isInCandidates(Object ob) {
        return candidates.contains(ob);
    }

    /**
     * Adds an object to findings.
     *
     * @param ob the object to add
     * @return true if the object was added (was not already present)
     */
    public boolean addToFindings(Object ob) {
        return findings.add(ob);
    }

    /**
     * Adds an object to candidates.
     *
     * @param ob the object to add
     * @return true if the object was added (was not already present)
     */
    public boolean addToCandidates(Object ob) {
        return candidates.add(ob);
    }

    /**
     * Gets the next unique ID from the atomic counter.
     *
     * @return the next ID value
     */
    public long nextId() {
        return idCounter.getAndIncrement();
    }

    /**
     * Gets the current value of the ID counter without incrementing.
     *
     * @return the current ID counter value
     */
    public long getCurrentId() {
        return idCounter.get();
    }

    /**
     * Resets the ID counter to zero.
     */
    public void resetIdCounter() {
        idCounter.set(0);
    }
}
