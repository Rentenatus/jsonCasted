/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.io.writer.strategy.DefinitionalStrategy;
import de.jare.jsoncasted.io.writer.strategy.PrintStrategy;
import de.jare.jsoncasted.io.writer.walker.RootObjectWriteWalker;
import de.jare.jsoncasted.io.writer.walker.WoodMetadataInjection;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
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
 * The JsonWriter class provides utility methods for serializing Java objects into JSON format. It supports writing to
 * strings, files, and output streams with configurable character encoding.
 *
 * @author Janusch Rentenatus
 */
public class JsonObjectWriter {

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
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root, String charsetName) throws JsonParseException, IOException, JsonWriteException {
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
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException, JsonWriteException {
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
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static void write(Object ob, File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException, JsonWriteException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, root);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonObjectWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Serializes an object to a JSON string using the specified character encoding.Uses the default root class from the
     * definition.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @param charsetName The name of a supported charset, for example "UTF-8".
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, String charsetName) throws JsonParseException, IOException, JsonWriteException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null);
        return new String(out.toByteArray(), charsetName);
    }

    /**
     * Serializes an object to a JSON string using the default character encoding.Uses the default root class from the
     * definition.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @return JSON string representation of the object.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static String writeToString(Object ob, JsonItemDefinition definition) throws JsonParseException, IOException, JsonWriteException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null);
        return new String(out.toByteArray());
    }

    /**
     * Serializes an object and writes it to a file. Uses the default root class from the definition.
     *
     * @param ob The object to serialize.
     * @param file The target file to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(Object ob, File file, JsonItemDefinition definition) throws JsonParseException, IOException, JsonWriteException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, null);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonObjectWriter.class.getName()).log(Level.SEVERE, null, ex);
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
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static void write(Object ob, OutputStream out, JsonItemDefinition definition, JsonClass root) throws IOException, JsonParseException, JsonWriteException {
        write(ob, out, definition.getModel(), definition.getCastingLevel(), root, JsonDebugLevel.SIMPLE);

    }

    /**
     * Serializes an object to an output stream with debug level.
     *
     * @param ob The object to serialize.
     * @param out The output stream to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @param debugLevel The debug level for controlling debug output.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     */
    public static void write(Object ob, OutputStream out, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws IOException, JsonWriteException, JsonParseException {
        write(ob, out, definition.getModel(), definition.getCastingLevel(), root, debugLevel);
    }

    /**
     * Serializes an object to an output stream.
     *
     * @param ob The object to serialize.
     * @param out The output stream to write the JSON output.
     * @param model
     * @param castingLevel
     * @param root The root JSON class for the object.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws de.jare.jsoncasted.io.JsonWriteException
     */
    public static void write(Object ob, OutputStream out, JsonModel model, JsonCastingLevel castingLevel, JsonClass root) throws IOException, JsonParseException, JsonWriteException {
        write(ob, out, model, castingLevel, root, JsonDebugLevel.SIMPLE);
    }

    /**
     * Serializes an object to an output stream with debug level.
     *
     * @param ob The object to serialize.
     * @param out The output stream to write the JSON output.
     * @param model
     * @param castingLevel
     * @param root The root JSON class for the object.
     * @param debugLevel The debug level for controlling debug output.
     * @throws IOException If an I/O error occurs during writing.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     */
    public static void write(Object ob, OutputStream out, JsonModel model, JsonCastingLevel castingLevel, JsonClass root, JsonDebugLevel debugLevel) throws IOException, JsonWriteException, JsonParseException {
        final PrintWriter prn = new PrintWriter(out);
        final PrintStrategy strategie = new PrintStrategy(prn);
        final DefinitionsContext definitionsContext = new DefinitionsContext(model);
        final WoodMetadataInjection injection = writeInjection(ob, definitionsContext, root, castingLevel, debugLevel);
        final RootObjectWriteWalker walker = new RootObjectWriteWalker(strategie, definitionsContext, root, castingLevel, debugLevel);
        walker.write(ob);
        prn.flush();
    }

    protected static WoodMetadataInjection writeInjection(Object ob, DefinitionsContext definitionsContext, JsonClass root, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        final DefinitionalStrategy strategie = new DefinitionalStrategy(definitionsContext);
        new RootObjectWriteWalker(strategie, definitionsContext, root, castingLevel, debugLevel).write(ob);
        //return new WoodMetadataInjection(       );
        return null;
    }
 
    /**
     * Serializes an object and writes it to a file with debug level.
     *
     * @param ob The object to serialize.
     * @param file The target file to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @param debugLevel The debug level for controlling debug output.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(Object ob, File file, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws JsonWriteException, JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, root, debugLevel);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonObjectWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Serializes an object to a JSON string with debug level.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @param root The root JSON class for the object.
     * @param debugLevel The debug level for controlling debug output.
     * @return JSON string representation of the object.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws JsonWriteException, JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, root, debugLevel);
        return new String(out.toByteArray());
    }

    /**
     * Serializes an object to a JSON string using the default character encoding. Uses the default root class from the
     * definition with debug level.
     *
     * @param ob The object to serialize.
     * @param definition The JSON item definition containing model information.
     * @param debugLevel The debug level for controlling debug output.
     * @return JSON string representation of the object.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonDebugLevel debugLevel) throws JsonWriteException, JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null, debugLevel);
        return new String(out.toByteArray());
    }

    /**
     * Serializes an object and writes it to a file with debug level. Uses the default root class from the definition.
     *
     * @param ob The object to serialize.
     * @param file The target file to write the JSON output.
     * @param definition The JSON item definition containing model information.
     * @param debugLevel The debug level for controlling debug output.
     * @throws JsonWriteException If writing fails due to serialization errors.
     * @throws JsonParseException If parsing fails during serialization.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void write(Object ob, File file, JsonItemDefinition definition, JsonDebugLevel debugLevel) throws JsonWriteException, JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, null, debugLevel);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonObjectWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
