/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central JSON parser service providing various input methods for parsing JSON content.
 *
 * <p>This service uses a recursive-descent parsing approach and supports multiple input sources:</p>
 * <ul>
 *   <li>String input</li>
 *   <li>File input</li>
 *   <li>URL input</li>
 *   <li>Reader input (StringReader, FileReader, BufferedReader)</li>
 * </ul>
 *
 * <p>Each parse method returns a {@link JsonResource} containing the parsed JSON tree
 * and metadata. The debug level controls the amount of debugging information produced.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonParserService {

    /**
     * Parses JSON content from a string.
     *
     * @param s the JSON string to parse.
     * @param debugLevel the debug level for logging.
     * @return a JsonResource containing the parsed content.
     * @throws JsonParseException if JSON parsing fails.
     * @throws IOException if an I/O error occurs.
     */
    public static JsonResource parse(String s, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        return parse(new StringReader(s), JsonResource.empty(), debugLevel);
    }

    /**
     * Parses JSON content from a file.
     *
     * @param file the file to parse.
     * @param debugLevel the debug level for logging.
     * @return a JsonResource containing the parsed content, or {@code null} if file not found.
     * @throws JsonParseException if JSON parsing fails.
     * @throws IOException if an I/O error occurs.
     */
    public static JsonResource parse(File file, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        FileReader in;
        try {
            in = new FileReader(file);
            return parse(in, file.getPath(), debugLevel);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Parses JSON content from a URL.
     *
     * @param url1 the URL to read JSON from.
     * @param debugLevel the debug level for logging.
     * @return a JsonResource containing the parsed content, or {@code null} if file not found.
     * @throws JsonParseException if JSON parsing fails.
     * @throws IOException if an I/O error occurs.
     */
    public static JsonResource parse(final URL url1, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        BufferedReader in;
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(url1.openStream());
            in = new BufferedReader(inputStreamReader);
            return parse(in, url1.toString(), debugLevel);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Parses JSON content from a StringReader.
     *
     * @param reader the StringReader to read from.
     * @param filename the filename to associate with the parsed content.
     * @param debugLevel the debug level for logging.
     * @return a JsonResource containing the parsed content.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails.
     */
    public static JsonResource parse(StringReader reader, String filename, JsonDebugLevel debugLevel) throws IOException, JsonParseException {
        return parse((Reader) reader, JsonResource.forFile(filename), debugLevel);
    }

    /**
     * Parses JSON content from a FileReader.
     *
     * @param reader the FileReader to read from.
     * @param filename the filename to associate with the parsed content.
     * @param debugLevel the debug level for logging.
     * @return a JsonResource containing the parsed content.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails.
     */
    public static JsonResource parse(FileReader reader, String filename, JsonDebugLevel debugLevel) throws IOException, JsonParseException {
        return parse((Reader) reader, JsonResource.forFile(filename), debugLevel);
    }

    /**
     * Parses JSON content from a BufferedReader.
     *
     * @param reader the BufferedReader to read from.
     * @param filename the filename to associate with the parsed content.
     * @param debugLevel the debug level for logging.
     * @return a JsonResource containing the parsed content.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails.
     */
    public static JsonResource parse(BufferedReader reader, String filename, JsonDebugLevel debugLevel) throws IOException, JsonParseException {
        return parse((Reader) reader, JsonResource.forFile(filename), debugLevel);
    }

    /**
     * Parses JSON content from a generic Reader into the specified container.
     *
     * @param reader the Reader to read from.
     * @param container the JsonResource to populate with parsed content.
     * @param debugLevel the debug level for logging.
     * @return the populated JsonResource.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails.
     */
    public static JsonResource parse(Reader reader, JsonResource container, JsonDebugLevel debugLevel) throws IOException, JsonParseException, JsonParseException {
        ParseStreamReader psr = new ParseStreamReader(reader, debugLevel);
        return RootParser.parse(psr, container, debugLevel);
    }

}
