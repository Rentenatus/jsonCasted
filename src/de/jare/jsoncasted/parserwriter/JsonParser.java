/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserservice.JsonParserService;
import de.jare.jsoncasted.pconvertservice.RootConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * The JsonParser class provides static methods for parsing JSON data from
 * various sources (strings, files, URLs) and converting it into JsonItem
 * instances. It serves as a high-level entry point for JSON parsing and
 * conversion to the internal model.
 *
 * @author Janusch Rentenatus
 */
public class JsonParser {

    /**
     * Parses JSON from a string and converts it to a JsonItem.
     *
     * @param s The JSON string to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(s, debugLevel);
        return parse(res, descriptor, root, debugLevel);
    }

    /**
     * Parses JSON from a file and converts it to a JsonItem.
     *
     * @param file The file containing JSON data to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(file, debugLevel);
        return parse(res, descriptor, root, debugLevel);
    }

    /**
     * Parses JSON from a URL and converts it to a JsonItem.
     *
     * @param url The URL pointing to JSON data to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(final URL url, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(url, debugLevel);
        return parse(res, descriptor, root, debugLevel);
    }

    /**
     * Parses a JSON resource and converts it to a JsonItem.
     *
     * @param res The JSON resource to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws JsonParseException If conversion fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(JsonResource res, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        return RootConverter.convert(res, root, descriptor, debugLevel);
    }

    /**
     * Parses JSON from a file and converts it to a JsonItem. Uses the class
     * name from the specified Class object as the root.
     *
     * @param file The file containing JSON data to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param aClass The Class whose name will be used as the root.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        return parse(file, descriptor, aClass.getTypeName(), debugLevel);
    }

    /**
     * Parses JSON from a string and converts it to a JsonItem. Uses the default
     * debug level (SIMPLE).
     *
     * @param s The JSON string to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return parse(s, descriptor, root, JsonDebugLevel.SIMPLE);
    }

    /**
     * Parses JSON from a file and converts it to a JsonItem. Uses the default
     * debug level (SIMPLE).
     *
     * @param file The file containing JSON data to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return parse(file, descriptor, root, JsonDebugLevel.SIMPLE);
    }

    /**
     * Parses JSON from a URL and converts it to a JsonItem. Uses the default
     * debug level (SIMPLE).
     *
     * @param url1 The URL pointing to JSON data to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(final URL url1, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return parse(url1, descriptor, root, JsonDebugLevel.SIMPLE);
    }

    /**
     * Parses JSON from a file and converts it to a JsonItem. Uses the class
     * name from the specified Class object as the root and default debug level.
     *
     * @param file The file containing JSON data to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param aClass The Class whose name will be used as the root.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass) throws JsonParseException, IOException {
        return parse(file, descriptor, aClass, JsonDebugLevel.SIMPLE);
    }

    /**
     * Parses a JSON resource and converts it to a JsonItem. Uses the default
     * debug level (SIMPLE).
     *
     * @param res The JSON resource to parse.
     * @param descriptor The model descriptor containing type definitions.
     * @param root The name of the root class for type resolution.
     * @return The parsed JsonItem.
     * @throws JsonParseException If conversion fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(JsonResource res, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return RootConverter.convert(res, root, descriptor, JsonDebugLevel.SIMPLE);
    }

    /**
     * Parses JSON from a file and converts it to a JsonItem. Uses the
     * definition's descriptor and the specified JsonClass as root.
     *
     * @param file The file containing JSON data to parse.
     * @param definition The item definition containing model and descriptor.
     * @param root The JsonClass to use as the root.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return parse(file, definition.getDescriptor(), root.getcName());
    }

}
