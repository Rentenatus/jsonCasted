/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.strategy;

import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.record.DefinitionsContextObjectRecord;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import java.util.List;

/**
 * A write strategy that handles the serialization of _woodDefinitions container.
 * This strategy works alongside the main PrintStrategy to write object definitions
 * in a separate _woodDefinitions section.
 *
 * @author Janusch Rentenatus
 */
public class WoodDefinitionWriteStrategy implements WriteStrategy {

    private final WriteStrategy delegate;
    private final DefinitionsContext definitionsContext;

    /**
     * Creates a new WoodDefinitionWriteStrategy that delegates to the provided strategy.
     *
     * @param delegate the underlying write strategy (e.g., PrintStrategy)
     * @param definitionsContext the definitions context containing objects to write as definitions
     */
    public WoodDefinitionWriteStrategy(WriteStrategy delegate, DefinitionsContext definitionsContext) {
        this.delegate = delegate;
        this.definitionsContext = definitionsContext;
    }

    /**
     * Gets the underlying delegate strategy.
     *
     * @return the delegate strategy
     */
    public WriteStrategy getDelegate() {
        return delegate;
    }

    /**
     * Gets the definitions context.
     *
     * @return the definitions context
     */
    public DefinitionsContext getDefinitionsContext() {
        return definitionsContext;
    }

    /**
     * Checks if there are definitions to write.
     *
     * @return true if there are definitions
     */
    public boolean hasDefinitions() {
        return definitionsContext.hasDefinitions();
    }

    // Delegate all WriteStrategy methods to the underlying strategy

    @Override
    public void writePath(WriteNodePath intentPath) {
        delegate.writePath(intentPath);
    }

    @Override
    public void writeStart(JsonClass jClass, Object ob, JsonField parentField, Object parent, boolean needsCast, boolean needsClassDef, WriteNodePath iString) {
        delegate.writeStart(jClass, ob, parentField, parent, needsCast, needsClassDef, iString);
    }

    @Override
    public void writeStartArray(JsonType jTypeOrNull, Object ob, boolean isPrimitive, WriteNodePath iString) {
        delegate.writeStartArray(jTypeOrNull, ob, isPrimitive, iString);
    }

    @Override
    public void writeEnd(JsonClass jClass, Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString) {
        delegate.writeEnd(jClass, ob, isFollowing, hasFieldKeys, iString);
    }

    @Override
    public void writeEndArray(Object ob, boolean isPrimitive, boolean isFollowing, WriteNodePath iString) {
        delegate.writeEndArray(ob, isPrimitive, isFollowing, iString);
    }

    @Override
    public void writeHasFieldKeys(JsonClass jClass, Object ob, WriteNodePath iString) {
        delegate.writeHasFieldKeys(jClass, ob, iString);
    }

    @Override
    public void writeAttrName(JsonClass jClass, boolean isFollowing, String fName, WriteNodePath iString) {
        delegate.writeAttrName(jClass, isFollowing, fName, iString);
    }

    @Override
    public void writeAttrNull(WriteNodePath iString) {
        delegate.writeAttrNull(iString);
    }

    @Override
    public void writePrimitive(JsonType jTypePrim, Object attr, WriteNodePath iString) {
        delegate.writePrimitive(jTypePrim, attr, iString);
    }

    @Override
    public void writeArraySeparator(boolean primitive, WriteNodePath iString) {
        delegate.writeArraySeparator(primitive, iString);
    }

    @Override
    public boolean skippProzess(JsonType jTypeOrNull, Object ob) {
        return delegate.skippProzess(jTypeOrNull, ob);
    }

    @Override
    public void writeNodeValue(Object object, WriteNodePath iString) {
        delegate.writeNodeValue(object, iString);
    }
}
