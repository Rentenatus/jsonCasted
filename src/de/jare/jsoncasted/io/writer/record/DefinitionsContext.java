/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.record;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Janusch Renteantus
 */
public class DefinitionsContext {

    private final JsonModel model;

    // Atomic counter for generating unique IDs within this context.
    private final AtomicLong idCounter = new AtomicLong(0);

    // All objects found that are candidates for a reference ID or found that are going to be serialized are collected here. They
    private final Map<Object, DefinitionsContextObjectRecord> recordMap;

    public DefinitionsContext(de.jare.jsoncasted.model.JsonModel model) {
        this.model = model;
        this.recordMap = new IdentityHashMap<>();
    }

    public JsonModel getModel() {
        return model;
    }

    Map<Object, DefinitionsContextObjectRecord> getRecordMap() {
        return recordMap;
    }

    /**
     * Checks if an object is already in findings.
     *
     * @param ob the object to check
     * @return true if the object is in findings
     */
    public boolean isInFindings(Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return false;
        }
        return record.isFinding();
    }

    /**
     * Checks if an object is already in candidates.
     *
     * @param ob the object to check
     * @return true if the object is in candidates
     */
    public boolean isInCandidates(Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return false;
        }
        return record.isCandidate();
    }

    /**
     * Checks if an object is already in candidates.
     *
     * @param ob the object to check
     * @return true if the object is in candidates
     */
    public boolean isInAssigned(Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return false;
        }
        return record.isAssigned();
    }

    /**
     * Adds an object to findings.
     *
     * @param jType
     * @param ob the object to add
     * @return true if the object was added (was not already present)
     */
    public DefinitionsContextObjectRecord addToFindings(JsonClass jType, Object ob) {
        DefinitionsContextObjectRecord record = getOrCreate(jType, ob);
        record.asFinding();
        return record;
    }

    /**
     * Adds an object to candidates.
     *
     * @param jType
     * @param ob the object to add
     * @return true if the object was added (was not already present)
     */
    public DefinitionsContextObjectRecord addToCandidates(JsonType jType, Object ob) {
        DefinitionsContextObjectRecord record = getOrCreate(jType, ob);
        record.asCandidate();
        return record;
    }

    public DefinitionsContextObjectRecord moveToFindings(Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return null;
        }
        record.asFinding();
        return record;
    }

    public DefinitionsContextObjectRecord moveToAssigned(Object ob, JsonType parentType, Object parent) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return null;
        }
        record.setContainer(parent);
        record = getOrCreate(parentType, parent);
        record.asContainer();
        return record;
    }

    public DefinitionsContextObjectRecord addToAssigned(JsonType jType, Object ob, JsonType parentType, Object parent) {
        DefinitionsContextObjectRecord record = getOrCreate(jType, ob);
        record.setContainer(parent);
        record = getOrCreate(parentType, parent);
        record.asContainer();
        return record;
    }

    public DefinitionsContextObjectRecord getOrCreate(JsonType jType, Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            record = new DefinitionsContextObjectRecord(jType, ob, nextId());
            recordMap.put(ob, record);
        }
        return record;
    }

    /**
     * Removes an object from map.
     *
     * @param ob the object to remove
     * @return {@code true} if candidates contained the specified element
     */
    public DefinitionsContextObjectRecord remove(Object ob) {
        return recordMap.remove(ob);
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
