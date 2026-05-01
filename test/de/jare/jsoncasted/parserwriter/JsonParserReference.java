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
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parser.inner.RootParserBak;
import de.jare.jsoncasted.parserservice.ParseStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reference implementation of the JSON parser using the legacy Bak (backup) parser classes.
 * This class provides parsing methods for various input sources using the deprecated
 * parser pipeline.
 *
 * @author Janusch Rentenatus
 * @deprecated Replaced by JsonNode-based parsing pipeline.
 */
@Deprecated
public class JsonParserReference {

    /**
     * Parses JSON from a string using the legacy parser.
     *
     * @param s The JSON string to parse.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(String s, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
          StringReader in = new StringReader(s);
          return parse(in, definition, root, debugLevel);
      }

    /**
     * Parses JSON from a file using the legacy parser.
     *
     * @param file The file to parse.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem, or null if file not found.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
      public static JsonItem parse(File file, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
          FileReader in;
          try {
              in = new FileReader(file);
              return parse(in, definition, root, debugLevel);
          } catch (FileNotFoundException ex) {
              Logger.getGlobal().log(Level.SEVERE, null, ex);
          }
          return null;
      }

    /**
     * Parses JSON from a URL using the legacy parser.
     *
     * @param url1 The URL to parse.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem, or null if file not found.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
      public static JsonItem parse(final URL url1, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
          BufferedReader in;
          try {
              final InputStreamReader inputStreamReader = new InputStreamReader(url1.openStream());
              in = new BufferedReader(inputStreamReader);
              return parse(in, definition, root, debugLevel);
          } catch (FileNotFoundException ex) {
              Logger.getGlobal().log(Level.SEVERE, null, ex);
          }
          return null;
      }

    /**
     * Parses JSON from a Reader using the legacy parser.
     *
     * @param in The Reader to parse from.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem.
     * @throws IOException If I/O errors occur.
     * @throws JsonParseException If parsing fails.
     */
      public static JsonItem parse(Reader in, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debugLevel) throws IOException, JsonParseException {
          ParseStreamReader psr = new ParseStreamReader(in, debugLevel);
          return new RootParserBak(definition, root).parse(psr);
      }

    /**
     * Parses JSON from a file using the legacy parser.
     * Uses the class name from the specified Class object.
     *
     * @param file The file to parse.
     * @param definition The JSON item definition.
     * @param aClass The Class whose name will be used as the root.
     * @param debugLevel The debug level for controlling debug output.
     * @return The parsed JsonItem, or null if file not found.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
      public static JsonItem parse(File file, JsonItemDefinition definition, Class<?> aClass, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
          JsonClass jClass = definition.getModel().getJsonClass(aClass);
          return parse(file, definition, jClass, debugLevel);
      }

    /**
     * Parses JSON from a string using the legacy parser with default debug level.
     *
     * @param s The JSON string to parse.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(String s, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
          return parse(s, definition, root, JsonDebugLevel.SIMPLE);
      }

    /**
     * Parses JSON from a file using the legacy parser with default debug level.
     *
     * @param file The file to parse.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
          return parse(file, definition, root, JsonDebugLevel.SIMPLE);
      }

    /**
     * Parses JSON from a URL using the legacy parser with default debug level.
     *
     * @param url1 The URL to parse.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(final URL url1, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
          return parse(url1, definition, root, JsonDebugLevel.SIMPLE);
      }

    /**
     * Parses JSON from a Reader using the legacy parser with default debug level.
     *
     * @param in The Reader to parse from.
     * @param definition The JSON item definition.
     * @param root The root JSON class.
     * @return The parsed JsonItem.
     * @throws IOException If I/O errors occur.
     * @throws JsonParseException If parsing fails.
     */
    public static JsonItem parse(Reader in, JsonItemDefinition definition, JsonClass root) throws IOException, JsonParseException {
          return parse(in, definition, root, JsonDebugLevel.SIMPLE);
      }

    /**
     * Parses JSON from a file using the legacy parser with default debug level.
     * Uses the class name from the specified Class object.
     *
     * @param file The file to parse.
     * @param definition The JSON item definition.
     * @param aClass The Class whose name will be used as the root.
     * @return The parsed JsonItem.
     * @throws JsonParseException If parsing fails.
     * @throws IOException If I/O errors occur.
     */
    public static JsonItem parse(File file, JsonItemDefinition definition, Class<?> aClass) throws JsonParseException, IOException {
          return parse(file, definition, aClass, JsonDebugLevel.SIMPLE);
      }

  }
