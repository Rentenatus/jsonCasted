/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategie;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;

/**
 *
 * @author Administrator
 */
public class DefinitionalStrategie implements WriteStrategie {

    private final DefinitionsContext definitionsContext;

    public DefinitionalStrategie(DefinitionsContext definitionsContext) {
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
        if (jClass.isDefinitional()) {
            definitionsContext.addToFindings(ob);
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
    public void writeEndArray(Object ob, boolean isPrimitive, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString) {
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

}
