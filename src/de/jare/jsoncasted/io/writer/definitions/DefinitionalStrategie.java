/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.jare.jsoncasted.io.writer.definitions;

import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategie;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;

/**
 *
 * @author Administrator
 */
public class DefinitionalStrategie implements WriteStrategie {

    @Override
    public void writePath(WriteNodePath intentPath) {
        //NoOp
    }

    @Override
    public void writeStart(JsonClass jClass, Object ob, boolean needsCast, boolean needsClassDef, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeStartArray(Object ob, boolean isPrimitive, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeEnd(JsonClass jClass, Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeEndArray(Object ob, boolean isPrimitive, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeHasFieldKeys(JsonClass jClass, Object ob, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeAttrName(JsonClass jClass, Object ob, boolean isFollowing, String fName, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeAttrNull(WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writePrimitive(JsonType jTypePrim, Object attr, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void writeArraySeparator(Object ob, boolean primitive, WriteNodePath iString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
