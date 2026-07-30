/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer.strategy;

import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;

/**
 *
 *
 * @author Janusch Rentenatus
 */
public class DefinitionalStrategy implements WriteStrategy {

    private final DefinitionsContext definitionsContext;

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

    @Override
    public void writePath(WriteNodePath intentPath) {
        //NoOp
    }

    @Override
    public void writeStart(JsonClass jClass, Object ob, boolean needsCast, boolean needsClassDef, WriteNodePath iString) {
        if (jClass != null && jClass.isDefinitional()) {
            definitionsContext.addToFindings(ob);
        } else if (definitionsContext.isInCandidates(ob)) {
            definitionsContext.addToFindings(ob);
            definitionsContext.removeCandidate(ob);
        } else {
            definitionsContext.addToCandidates(ob);
        }
    }

    @Override
    public void writeStartArray(Object ob, boolean isPrimitive, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeEnd(JsonClass jClass, Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public void writeEndArray(Object ob, boolean isPrimitive, boolean isFollowing, WriteNodePath iString) {
        //NoOp
    }

    @Override
    public boolean skippProzess(final JsonType jType, final Object ob) {
        if (ob == null) {
            return true;
        }
        return definitionsContext.isInFindings(ob) || definitionsContext.isInCandidates(ob);
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
