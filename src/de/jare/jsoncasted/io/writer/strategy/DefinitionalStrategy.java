/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer.strategy;

import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.record.DefinitionsContextObjectRecord;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Janusch Rentenatus
 */
public class DefinitionalStrategy implements WriteStrategy {

    private final DefinitionsContext definitionsContext;
    private final Set<DefinitionsContextObjectRecord> assignableRecords = new LinkedHashSet<>();

    public DefinitionalStrategy(DefinitionsContext definitionsContext) {
        this.definitionsContext = definitionsContext;
    }

    /**
     * Gets the definitions context used by this ObjectDefinitional.
     *
     * @return the definitions context
     */
    public DefinitionsContext getDefinitionsContext() {
        return definitionsContext;
    }

    /**
     * Checks if an object should be written as a link reference.
     *
     * @param ob the object to check
     * @return true if the object should be written as a link
     */
    public boolean shouldWriteAsLink(Object ob) {
        return definitionsContext.shouldWriteAsLink(ob);
    }

    /**
     * Gets the repository key for an object.
     *
     * @param ob the object to get the repository key for
     * @return the repository key, or null if no record exists
     */
    public String getRepositoryKey(Object ob) {
        return definitionsContext.getRepositoryKey(ob);
    }

    /**
     * Checks if there are any definitions to write.
     *
     * @return true if there are definitions
     */
    public boolean hasDefinitions() {
        return definitionsContext.hasDefinitions();
    }

    /**
     * Gets all definition records.
     *
     * @return list of definition records
     */
    public List<DefinitionsContextObjectRecord> getDefinitionRecords() {
        return definitionsContext.getDefinitionRecords();
    }

    @Override
    public void writePath(WriteNodePath intentPath) {
        //NoOp
    }

    @Override
    public void writeStartObject(JsonClass jClass, Object ob, JsonField parentField, Object definitionalParent, boolean needsCast, boolean needsClassDef, WriteNodePath intentPath) {
        // If already assigned, skip - object is already processed
        if (definitionsContext.isInAssigned(ob)) {
            return;
        }

        // If in definitional/container field
        if (definitionalParent != null && parentField != null && parentField.getKind().isDefinitional()) {
            // possibly ASSIGNABLE   
            JsonType parentType = parentField.getjType();
            DefinitionsContextObjectRecord record = definitionsContext.getOrCreate(jClass, ob);
            if (!assignableRecords.contains(record)) {
                record.setParent(parentType, definitionalParent);
                assignableRecords.add(record);
            }
        }

        if (definitionsContext.isInCandidates(ob)) {
            // Already seen as candidate -> move to FINDING
            definitionsContext.moveToFindings(ob);
        } else if (!definitionsContext.isInFindings(ob)
                && !definitionsContext.isInAssigned(ob)
                && !definitionsContext.isInAssignable(ob)) {
            // First time seeing this object -> CANDIDATE
            definitionsContext.addToCandidates(jClass, ob);
        }
    }

    @Override
    public void writeEndFile() {
        for (DefinitionsContextObjectRecord record : assignableRecords) {
            if (record.isFinding() || record.isCandidate()) {
                Object ob = record.getObject();
                definitionsContext.moveToAssignable(ob);
            }
        }
    }

    @Override
    public void writeStartArray(JsonType jTypeOrNull, Object ob, boolean isPrimitive, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeEndObject(JsonClass jClass, Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeEndArray(Object ob, boolean isPrimitive, boolean isFollowing, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public boolean skipProcess(final JsonType jType, final Object ob) {
        if (ob == null) {
            return true;
        }
        return definitionsContext.isInAssigned(ob);
    }

    /**
     * Writes a link reference for an object that should not be inlined. This writes the _woodLink property with the
     * object's repository key.
     *
     * @param jClass the JSON class of the object
     * @param ob the object to write as link
     * @param iString the indentation path
     */
    public void writeLink(JsonClass jClass, Object ob, WriteNodePath iString) {
        String repoKey = getRepositoryKey(ob);
        if (repoKey != null) {
            // Write as _woodLink reference
            // Note: This is a placeholder - actual writing is done by the strategy
        }
    }

    @Override
    public void writeHasFieldKeys(JsonClass jClass, Object ob, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeAttrName(JsonClass jClass, boolean isFollowing, String fName, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeAttrNull(WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writePrimitive(JsonType jTypePrim, Object attr, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeArraySeparator(boolean primitive, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeNodeValue(Object object, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
