/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.writer.strategy.PrintStrategy;
import de.jare.jsoncasted.io.writer.walker.NodeWriteWalker;
import de.jare.jsoncasted.lang.JsonNode;
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
 * The JsonNodeWriter class provides utility methods for serializing JsonNode objects into JSON format. It supports
 * writing to strings, files, and output streams with configurable character encoding.
 *
 * @author Janusch Rentenatus
 */
public class JsonNodeWriter {

    /**
     * Serializes a JsonNode to a JSON string using the specified character encoding.
     *
     * @param node The JsonNode to serialize.
     * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}, for example "UTF-8".
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(JsonNode node, String charsetName) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(node, out);
        return new String(out.toByteArray(), java.nio.charset.Charset.forName(charsetName));
    }

    /**
     * Serializes a JsonNode to a JSON string using the default character encoding.
     *
     * @param node The JsonNode to serialize.
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(JsonNode node) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(node, out);
        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Serializes a JsonNode and writes it to a file.
     *
     * @param node The JsonNode to serialize.
     * @param file The target file to write the JSON output.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(JsonNode node, File file) throws JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(node, out);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonNodeWriter.class.getName()).log(Level.SEVERE, "Failed to create output file for JSON writing", ex);
        }
    }

    /**
     * Serializes a JsonNode to an output stream.
     *
     * @param node The JsonNode to serialize.
     * @param out The output stream to write the JSON output.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonParseException If parsing fails during serialization.
     */
    public static void write(JsonNode node, OutputStream out) throws IOException, JsonParseException {
        PrintWriter prn = new PrintWriter(out);
        PrintStrategy strategie = new PrintStrategy(prn);
        new NodeWriteWalker(strategie, "", JsonDebugLevel.SIMPLE).writeNode(node);
        prn.flush();

    }

    /**
     * Serializes a JsonNode to an output stream with debug level.
     *
     * @param node The JsonNode to serialize.
     * @param out The output stream to write the JSON output.
     * @param debugLevel The debug level for controlling debug output.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     */
    public static void write(JsonNode node, OutputStream out, JsonDebugLevel debugLevel) throws IOException, JsonWriteException, JsonParseException {
        PrintWriter prn = new PrintWriter(out);
        PrintStrategy strategie = new PrintStrategy(prn);
        new NodeWriteWalker(strategie, "", debugLevel).writeNode(node);
        prn.flush();
    }

    /**
     * Serializes a JsonNode and writes it to a file with debug level.
     *
     * @param node The JsonNode to serialize.
     * @param file The target file to write the JSON output.
     * @param debugLevel The debug level for controlling debug output.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(JsonNode node, File file, JsonDebugLevel debugLevel) throws JsonWriteException, JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(node, out, debugLevel);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonNodeWriter.class.getName()).log(Level.SEVERE, "Failed to create output file for JSON writing", ex);
        }
    }

    /**
     * Serializes a JsonNode to a JSON string with debug level.
     *
     * @param node The JsonNode to serialize.
     * @param debugLevel The debug level for controlling debug output.
     * @return JSON string representation of the object.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(JsonNode node, JsonDebugLevel debugLevel) throws JsonWriteException, JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(node, out, debugLevel);
        return new String(out.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
    }

}
