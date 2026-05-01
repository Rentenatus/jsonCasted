/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.writer.inner.RootObjectWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JsonWriter class provides utility methods for serializing Java objects
 * into JSON format. It supports writing to strings, files, and output streams
 * with configurable character encoding.
 *
 * @author Janusch Rentenatus
 */
public class JsonWriter {

    /**
     * Serializes an object to a JSON string using the specified character encoding.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}, for example "UTF-8".
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root, String charsetName) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, root);
        return new String(out.toByteArray(), charsetName);
    }

    /**
     * Serializes an object to a JSON string using the default character encoding.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, root);
        return new String(out.toByteArray());
    }

    /**
     * Serializes an object and writes it to a file.
     *
     * @param ob The object to serialize.
     * @param file The target file to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(Object ob, File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, root);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Serializes an object to a JSON string using the specified character encoding.
     * Uses the default root class from the definition.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @param charsetName The name of a supported charset, for example "UTF-8".
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, String charsetName) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null);
        return new String(out.toByteArray(), charsetName);
    }

    /**
     * Serializes an object to a JSON string using the default character encoding.
     * Uses the default root class from the definition.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(Object ob, JsonItemDefinition definition) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null);
        return new String(out.toByteArray());
    }

    /**
     * Serializes an object and writes it to a file.
     * Uses the default root class from the definition.
     *
     * @param ob The object to serialize.
     * @param file The target file to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(Object ob, File file, JsonItemDefinition definition) throws JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Serializes an object to an output stream.
     *
     * @param ob The object to serialize.
     * @param out The output stream to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonParseException If parsing fails during serialization.
     */
    public static void write(Object ob, OutputStream out, JsonItemDefinition definition, JsonClass root) throws IOException, JsonParseException {
        try (PrintWriter prn = new PrintWriter(out)) {
            new RootObjectWriter(definition, root).write(prn, ob);
            prn.flush();
        }
    }

}
