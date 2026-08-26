/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io;

import de.jare.jsoncasted.io.writer.strategy.PrintStrategy;
import de.jare.jsoncasted.io.writer.walker.ItemWriteWalker;
import de.jare.jsoncasted.item.JsonItem;
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
 * The JsonItemWriter class provides utility methods for serializing JsonItem objects into JSON format. It supports
 * writing to strings, files, and output streams with configurable character encoding.
 *
 * @author Janusch Rentenatus
 */
public class JsonItemWriter {

    /**
     * Serializes an JsonItem to a JSON string using the specified character encoding.
     *
     * @param ob The JsonItem to serialize.
     * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}, for example "UTF-8".
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException
     */
    public static String writeToString(JsonItem ob, String charsetName) throws JsonParseException, IOException, JsonWriteException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out);
        return new String(out.toByteArray(), java.nio.charset.Charset.forName(charsetName));
    }

    /**
     * Serializes an JsonItem to a JSON string using the default character encoding.
     *
     * @param ob The JsonItem to serialize.
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException
     */
    public static String writeToString(JsonItem ob) throws JsonParseException, IOException, JsonWriteException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out);
        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Serializes an JsonItem and writes it to a file.
     *
     * @param ob The JsonItem to serialize.
     * @param file The target file to write the JSON output.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException
     */
    public static void write(JsonItem ob, File file) throws JsonParseException, IOException, JsonWriteException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonItemWriter.class.getName()).log(Level.SEVERE, "Failed to create output file for JSON writing", ex);
        }
    }

    /**
     * Serializes an JsonItem to an output stream with debug level.
     *
     * @param ob The JsonItem to serialize.
     * @param out The output stream to write the JSON output.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     */
    public static void write(JsonItem ob, OutputStream out) throws IOException, JsonWriteException, JsonParseException {
        final PrintWriter prn = new PrintWriter(out);
        final PrintStrategy strategy = new PrintStrategy(prn);
        new ItemWriteWalker(strategy, "").writeType(ob);
        prn.flush();
    }

}
