/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.record;

import de.jare.jsoncasted.io.JsonWoodIdLevel;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import de.jare.jsoncasted.model.JsonModel;

/**
 *
 * @author Janusch Renteantus
 */
public class DefinitionsContext {

    private final JsonModel model;
    private final String modelFile;

    // Wood id policy of this write session: NEVER ignores the collection sharing policies, PREFIXED moves object
    // identities into the angle bracket notation. Analog to the casting level of the write call.
    private JsonWoodIdLevel woodIdLevel = JsonWoodIdLevel.NECESSARY;

    // Atomic counter for generating unique IDs within this context.
    private final AtomicLong idCounter;

    // All objects found that are candidates for a reference ID or found that are going to be serialized are collected here. They
    private final Map<Object, DefinitionsContextObjectRecord> recordMap;

    // Occurrences of list instances under sharing enabled fields, counted by the pre-scan.
    private final Map<Object, Integer> listOccurrenceCount = new IdentityHashMap<>();

    // Local ids of shared lists; a list enters this map when its definition is written.
    private final Map<Object, Long> sharedListIds = new IdentityHashMap<>();

    // Content groups of the equals policy: the component identity key of every equals noted list, and the
    // occurrence count plus the assigned local id per key.
    private final Map<Object, String> equalsKeyByList = new IdentityHashMap<>();
    private final Map<String, Integer> equalsOccurrenceCount = new java.util.HashMap<>();
    private final Map<String, Long> equalsGroupIds = new java.util.HashMap<>();

    public DefinitionsContext(JsonModel model, String modelFile) {
        this.model = model;
        this.modelFile = modelFile;
        this.recordMap = new IdentityHashMap<>();
        this.idCounter = new AtomicLong(1);
    }

    public DefinitionsContext(JsonModel model, long startId) {
        this.model = model;
        this.modelFile = null;
        this.recordMap = new IdentityHashMap<>();
        this.idCounter = new AtomicLong(startId);
    }

    public JsonModel getModel() {
        return model;
    }

    public String getModelFile() {
        return modelFile;
    }

    /**
     * Returns the wood id policy of this write session.
     *
     * @return the JsonWoodIdLevel, never null (defaults to NECESSARY).
     */
    public JsonWoodIdLevel getWoodIdLevel() {
        return woodIdLevel;
    }

    /**
     * Sets the wood id policy of this write session. The entry points of the object writer call this with the level
     * of the item definition before the walk starts.
     *
     * @param woodIdLevel the JsonWoodIdLevel to use, null is ignored.
     */
    public void setWoodIdLevel(JsonWoodIdLevel woodIdLevel) {
        if (woodIdLevel != null) {
            this.woodIdLevel = woodIdLevel;
        }
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
     * Checks if an object is marked as assignable.
     *
     * @param ob the object to check
     * @return true if the object is assignable
     */
    public boolean isInAssignable(Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return false;
        }
        return record.isAssignable();
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

    /**
     * Adds an object as assignable to a container field.
     *
     * @param jType the JSON type of the object
     * @param ob the object to add
     * @return the record for the object
     */
    public DefinitionsContextObjectRecord addToAssignable(JsonType jType, Object ob) {
        DefinitionsContextObjectRecord record = getOrCreate(jType, ob);
        record.asAssignable();
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

    public DefinitionsContextObjectRecord moveToAssignable(Object ob, JsonType parentType, Object parent) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return null;
        }
        record.asAssignable();
        record.setParent(parentType, parent);
        // parent:
        if (parent == null) {
            return record;
        }
        record = getOrCreate(parentType, parent);
        record.asContainer();
        return record;
    }

    public DefinitionsContextObjectRecord moveToAssignable(Object ob) {
        DefinitionsContextObjectRecord record = recordMap.get(ob);
        if (record == null) {
            return null;
        }
        record.asAssignable();
        // parent:
        JsonType parentType = record.getParentType();
        Object parent = record.getParent();
        if (parent == null) {
            return record;
        }
        record = getOrCreate(parentType, parent);
        record.asContainer();
        return record;
    }

    public DefinitionsContextObjectRecord addToAssignable(JsonType jType, Object ob, JsonType parentType, Object parent) {
        DefinitionsContextObjectRecord record = getOrCreate(jType, ob);
        record.asAssignable();
        // parent:
        if (parent == null) {
            return record;
        }
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
     * Counts one occurrence of a list instance under a sharing enabled field. The pre-scan calls this for every
     * list field value; a list seen at more than one occurrence is treated as shared.
     *
     * @param list the list instance to count.
     */
    public void noteListOccurrence(Object list) {
        listOccurrenceCount.merge(list, 1, Integer::sum);
    }

    /**
     * Notes one occurrence of a list under an equals enabled field: the list joins its content group - lists with the
     * same component identity share one wood id, no matter whether they are the same instance.
     *
     * @param list the list instance to note.
     */
    public void noteEqualsListOccurrence(Object list) {
        if (!(list instanceof List<?> values)) {
            return;
        }
        final String contentKey = componentIdentityKey(values);
        equalsKeyByList.put(list, contentKey);
        equalsOccurrenceCount.merge(contentKey, 1, Integer::sum);
    }

    /**
     * Builds the component identity key of a list: the size leads the key, then every element - primitives by type
     * and value, objects by instance identity. Two lists carry the same key when they are component-wise identical.
     *
     * @param values the list elements.
     * @return the component identity key, never null.
     */
    private static String componentIdentityKey(List<?> values) {
        final StringBuilder sb = new StringBuilder(values.size()).append(':');
        for (Object element : values) {
            if (element == null) {
                sb.append("null;");
                continue;
            }
            if (element instanceof String || element instanceof Number
                    || element instanceof Boolean || element instanceof Character) {
                sb.append(element.getClass().getName()).append('=').append(element).append(';');
                continue;
            }
            sb.append('#').append(System.identityHashCode(element)).append(';');
        }
        return sb.toString();
    }

    /**
     * Checks whether the pre-scan saw the list as shared: an equals noted list counts per content group, a same
     * noted list counts per instance.
     *
     * @param list the list instance to check.
     * @return true when the list is shared and deserves a wood id.
     */
    public boolean isSharedList(Object list) {
        final String equalsKey = equalsKeyByList.get(list);
        if (equalsKey != null) {
            final Integer count = equalsOccurrenceCount.get(equalsKey);
            return count != null && count > 1;
        }
        final Integer count = listOccurrenceCount.get(list);
        return count != null && count > 1;
    }

    /**
     * Assigns the wood id to a shared list and marks its definition as written. The first writing occurrence calls
     * this; later occurrences read the id and become bare references. An equals noted list takes the id of its
     * content group, so an equal sibling later writes a bare reference to the same id.
     *
     * @param list the shared list instance.
     * @return the assigned local id.
     */
    public long assignSharedListLocalId(Object list) {
        final String equalsKey = equalsKeyByList.get(list);
        if (equalsKey != null) {
            return equalsGroupIds.computeIfAbsent(equalsKey, k -> nextId());
        }
        return sharedListIds.computeIfAbsent(list, k -> nextId());
    }

    /**
     * Returns the wood id of a shared list, or null when its definition is not written yet.
     *
     * @param list the list instance to look up.
     * @return the assigned local id, or null.
     */
    public Long getSharedListLocalId(Object list) {
        final String equalsKey = equalsKeyByList.get(list);
        if (equalsKey != null) {
            return equalsGroupIds.get(equalsKey);
        }
        return sharedListIds.get(list);
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

    public void maxCurrentId(long maxId) {
        idCounter.set(Math.max(idCounter.get(), maxId));
    }

    /**
     * Resets the ID counter to zero.
     */
    public void resetIdCounter() {
        idCounter.set(0);
    }

    public DefinitionsContextObjectRecord getRecord(Object ob) {
        return recordMap.get(ob);
    }

    /**
     * Gets all records that should be written as definitions. These are records that are candidates or findings (not
     * assigned/inlined).
     *
     * @return list of records that should be written as definitions
     */
    public List<DefinitionsContextObjectRecord> getDefinitionRecords() {
        List<DefinitionsContextObjectRecord> definitions = new ArrayList<>();
        for (DefinitionsContextObjectRecord record : recordMap.values()) {
            if (record.needDefinition()) {
                definitions.add(record);
            }
        }
        return definitions;
    }

    /**
     * Checks if there are any objects that should be written as definitions.
     *
     * @return true if there are definition records
     */
    public boolean hasDefinitions() {
        for (DefinitionsContextObjectRecord record : recordMap.values()) {
            if (record.needDefinition()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the repository key for an object, generating it if necessary.
     *
     * @param ob the object to get the repository key for
     * @return the repository key, or null if no record exists
     */
    public String getRepositoryKey(Object ob) {
        DefinitionsContextObjectRecord record = getRecord(ob);
        if (record != null) {
            return record.getOrGenerateRepositoryKey();
        }
        return null;
    }

    /**
     * Checks if an object should be written as a link reference instead of inline. An object should be written as a
     * link if it is assigned (already processed as definition).
     *
     * @param ob the object to check
     * @return true if the object should be written as a link
     */
    public boolean shouldWriteAsLink(Object ob) {
        if (ob == null) {
            return false;
        }
        DefinitionsContextObjectRecord record = getRecord(ob);
        return record != null && record.isAssigned();
    }

}
