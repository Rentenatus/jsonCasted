/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.record;

import de.jare.jsoncasted.model.JsonType;
import java.util.Objects;

/**
 * A record tracking the state and metadata of an object during JSON serialization.
 *
 * <p>
 * This class maintains the lifecycle state (disposition) of objects within the {@link DefinitionsContext}, including
 * whether they are candidates for serialization, have been detected as findings (e.g., cycles or containment objects),
 * can be assigned to a container field, or have been assigned to a container object. Each record is uniquely associated
 * with a Java object and its corresponding JSON type.
 * </p>
 *
 * <p>
 * The disposition of an object progresses through states: UNKNOWN -> CANDIDATE -> (FINDING | ASSIGNABLE) -> ASSIGNED.
 * ASSIGNABLE indicates that an object resides in a definitional/container field and may be converted to ASSIGNED during
 * writing. Once an object reaches the ASSIGNED state, its disposition cannot be changed.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public class DefinitionsContextObjectRecord {

    /**
     * Represents the current state of an object within the definitions context.
     */
    public enum Disposition {
        /**
         * Initial state when the record is created.
         */
        UNKNOWN,
        /**
         * The object has been identified as a finding (e.g., part of a cycle or containment relationship).
         */
        FINDING,
        /**
         * The object is a candidate for serialization.
         */
        CANDIDATE,
        /**
         * The object can be assigned to a container field. This is an intermediate state indicating that the object
         * resides in a definitional/container field and may be converted to ASSIGNED during writing.
         */
        ASSIGNABLE,
        /**
         * The object has been assigned to a container. This is the final state.
         */
        ASSIGNED
    }
    /**
     * The Java object being tracked.
     */
    private final Object object;
    /**
     * The JSON type associated with the object.
     */
    private final JsonType jType;
    /**
     * Unique identifier for the object within the context. Default is -1 (unassigned).
     */
    private long localId = -1;

    /**
     * Indicates whether this object is itself a container.
     */
    private boolean isContainer = false;

    /**
     * The repository key where the definition should be stored.
     */
    private String repositoryKey = null;

    /**
     * The current disposition of the object.
     */
    private Disposition disposition = Disposition.UNKNOWN;

    /**
     * The JSON type of the parent object.
     */
    private JsonType parentType;

    /**
     * The parent object.
     */
    private Object parent;

    /**
     * Constructs a new record with the given JSON type and object.
     *
     * @param jType the JSON type of the object
     * @param object the Java object to track
     */
    public DefinitionsContextObjectRecord(JsonType jType, Object object) {
        this.jType = jType;
        this.object = object;
    }

    /**
     * Constructs a new record with the given JSON type, object, and local ID.
     *
     * @param jType the JSON type of the object
     * @param object the Java object to track
     * @param localId the unique identifier for the object
     */
    public DefinitionsContextObjectRecord(JsonType jType, Object object, long localId) {
        this.jType = jType;
        this.object = object;
        this.localId = localId;
    }

    /**
     * Returns the JSON type associated with this record.
     *
     * @return the JSON type
     */
    public JsonType getJsonType() {
        return jType;
    }

    /**
     * Returns the tracked Java object.
     *
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Returns the unique local identifier for this object.
     *
     * @return the local ID, or -1 if not assigned
     */
    public long getLocalId() {
        return localId;
    }

    /**
     * Sets the unique local identifier for this object.
     *
     * @param localId the local ID to assign
     */
    public void setLocalId(long localId) {
        this.localId = localId;
    }

    /**
     * Returns the repository key for this object's definition.
     *
     * @return the repository key, or null if not set
     */
    public String getRepositoryKey() {
        return repositoryKey;
    }

    /**
     * Sets the repository key for this object's definition.
     *
     * @param repositoryKey the repository key to assign
     */
    public void setRepositoryKey(String repositoryKey) {
        this.repositoryKey = repositoryKey;
    }

    /**
     * Generates a repository key for this object if not already set. Uses the format "self::" followed by the localId.
     *
     * @return the generated or existing repository key
     */
    public String getOrGenerateRepositoryKey() {
        if (repositoryKey == null && localId >= 0) {
            repositoryKey = "self::" + localId;
        }
        return repositoryKey;
    }

    /**
     * Checks if this object should be written as a definition.
     *
     * @return true if this object should be written as a definition
     */
    public boolean needDefinition() {
        return disposition == Disposition.FINDING;
    }

    /**
     * Returns the current disposition of this object.
     *
     * @return the disposition
     */
    public Disposition getDisposition() {
        return disposition;
    }

    /**
     * Checks if this object is currently a candidate for serialization.
     *
     * @return true if the disposition is CANDIDATE
     */
    public boolean isCandidate() {
        return disposition == Disposition.CANDIDATE;
    }

    /**
     * Checks if this object has been marked as a finding.
     *
     * @return true if the disposition is FINDING
     */
    public boolean isFinding() {
        return disposition == Disposition.FINDING;
    }

    /**
     * Checks if this object has been assigned to a container.
     *
     * @return true if the disposition is ASSIGNED
     */
    public boolean isAssigned() {
        return disposition == Disposition.ASSIGNED;
    }

    /**
     * Checks if this object can be assigned to a container field.
     *
     * @return true if the disposition is ASSIGNABLE
     */
    public boolean isAssignable() {
        return disposition == Disposition.ASSIGNABLE;
    }

    /**
     * Checks if this object is itself a container.
     *
     * @return true if this object is marked as a container
     */
    public boolean isContainer() {
        return isContainer;
    }

    /**
     * Sets the disposition of this object.
     *
     * @param disposition the new disposition
     * @throws IllegalArgumentException if trying to change the disposition from ASSIGNED
     * @throws NullPointerException if disposition is null
     */
    public void setDisposition(Disposition disposition) {
        if (this.disposition == Disposition.ASSIGNED && disposition != Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Objects.requireNonNull(disposition);
    }

    /**
     * Marks this object as a finding.
     *
     * @throws IllegalArgumentException if the object is already assigned
     */
    public void asFinding() {
        if (this.disposition == Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Disposition.FINDING;
    }

    /**
     * Marks this object as a candidate for serialization.
     *
     * @throws IllegalArgumentException if the object is already assigned
     */
    public void asCandidate() {
        if (this.disposition == Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Disposition.CANDIDATE;
    }

    /**
     * Marks this object as a container.
     */
    public void asContainer() {
        this.isContainer = true;
    }

    /**
     * Marks this object as assignable to a container field.
     *
     * @throws IllegalArgumentException if the object is already assigned
     */
    public void asAssignable() {
        if (this.disposition == Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Disposition.ASSIGNABLE;
    }

    /**
     * Sets as ASSIGNED.
     *
     */
    public void asAssigned() {
        this.disposition = Disposition.ASSIGNED;
    }

    /**
     * Returns the JSON type of the parent object.
     *
     * @return the parent JSON type, or null if not set
     */
    public JsonType getParentType() {
        return parentType;
    }

    /**
     * Sets the JSON type of the parent object.
     *
     * @param parentType the parent JSON type to assign
     */
    public void setParentType(JsonType parentType) {
        this.parentType = parentType;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object, or null if not set
     */
    public Object getParent() {
        return parent;
    }

    /**
     * Sets the parent object.
     *
     * @param parent the parent object to assign
     */
    public void setParent(Object parent) {
        this.parent = parent;
    }

    /**
     * Sets the parent object and his JSON type .
     *
     * @param parentType the parent JSON type to assign
     * @param parent the parent object to assign
     */
    public void setParent(JsonType parentType, Object parent) {
        this.parent = parent;
        this.parentType = parentType;
    }
}
