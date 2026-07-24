/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer.printer;

import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategie;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * The ObjectWriter class handles the serialization of JSON object structures. It converts objects into JSON format
 * while maintaining indentation, type information, and error handling.
 *
 * @author Janusch Rentenatus
 */
public class PrintStrategie implements WriteStrategie {
    
    private final PrintWriter out;

    /**
     * Constructs an ObjectWriter instance with default indentation.
     *
     * @param out
     */
    public PrintStrategie(PrintWriter out) {
        this.out = out;
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param out
     */
    public PrintStrategie(PrintStream out) {
        this.out = new PrintWriter(out);
    }

    /**
     * Writes an object as a JSON structure.
     *
     * @param intentPath
     * @throws NullPointerException If the object has no associated JSON class.
     * @throws ClassCastException If the object does not match the expected JSON type.
     */
    @Override
    public void writePath(WriteNodePath intentPath) throws NullPointerException, ClassCastException {
        out.print(intentPath);
    }
    
    @Override
    public void writeHasFieldKeys(JsonClass jClass, Object ob, WriteNodePath intentPath) {
        out.println();
    }
    
    @Override
    public void writeAttrName(JsonClass jClass, boolean isFollowing, String fName, WriteNodePath intentPath) {
        if (isFollowing) {
            out.print(',');
            out.println();
        }
        out.print(intentPath);
        out.print('"');
        out.print(fName);
        out.print('"');
        out.print(": ");
    }
    
    @Override
    public void writeStart(JsonClass jClass, Object ob, boolean needsCast, boolean needsClassDef, WriteNodePath intentPath) {
        if (needsCast) {
            writeCast(jClass, ob, intentPath);
        }
        out.print('{');
        if (needsClassDef) {
            writeCastDef(jClass, ob, intentPath);
        }
    }
    
    @Override
    public void writeStartArray(Object ob, boolean isPrimitive, WriteNodePath iString) {
        out.print('[');
        if (isPrimitive) {
            out.println();
            out.print(iString);
        }
    }
    
    public void writeCast(final JsonClass jClass, final Object ob, WriteNodePath intentPath) {
        out.print('(');
        out.print(jClass.getcName());
        out.print(')');
    }
    
    public void writeCastDef(final JsonClass jClass, final Object ob, WriteNodePath intentPath) {
        out.println();
        out.print(intentPath);
        out.print("\"_class\": \"");
        out.print(jClass.getcName());
        out.print('\"');
        if (jClass.hasFieldKeys(ob)) {
            out.print(',');
        }
    }
    
    @Override
    public void writeEnd(final JsonClass jClass, final Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath intentPath) {
        if (isFollowing) {
            out.println();
        }
        if (hasFieldKeys) {
            out.print(intentPath);
        }
        out.print('}');
        out.flush();
    }
    
    @Override
    public void writeEndArray(final Object ob, boolean isPrimitive, boolean isFollowing, WriteNodePath iString) {
        if (!isPrimitive) {
            out.println();
            out.print(iString);
        }
        out.print(']');
        out.flush();
    }
    
    @Override
    public void writeAttrNull(WriteNodePath intentPath) {
        out.print(" null");
    }

    /**
     * Writes a primitive JSON value.
     *
     * @param jTypePrim The JSON type of the primitive.
     * @param attr The primitive value to serialize.
     * @param intentPath The indentation string for formatted output.
     */
    @Override
    public void writePrimitive(JsonType jTypePrim, Object attr, WriteNodePath intentPath) {
        out.print(jTypePrim.toString(attr));
    }
    
    @Override
    public void writeArraySeparator(boolean isPrimitive, WriteNodePath iString) {
        out.print(", ");
        if (!isPrimitive) {
            out.println();
            out.print(iString);
        }
    }
    
    @Override
    public void writeNodeValue(Object object, WriteNodePath iString) {
        out.print(String.valueOf(object));
    }
    
}
