/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.parser.inner.RootParser;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.tools.JsonMapFacade;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonParser {

    public static JsonItem parse(String s, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        StringReader in = new StringReader(s);
        return parse(in, definition, root, debbugLevel);
    }

    public static JsonItem parse(File file, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        FileReader in;
        try {
            in = new FileReader(file);
            return parse(in, definition, root, debbugLevel);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JsonItem parse(final URL url1, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        BufferedReader in;
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(url1.openStream());
            in = new BufferedReader(inputStreamReader);
            return parse(in, definition, root, debbugLevel);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JsonItem parse(Reader in, JsonItemDefinition definition, JsonClass root, JsonDebugLevel debbugLevel) throws IOException, JsonParseException {
        ParseStreamReader psr = new ParseStreamReader(in, debbugLevel);
        return new RootParser(definition, root).parse(psr);
    }

    public static JsonItem parse(File file, JsonItemDefinition definition, Class<?> aClass, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        JsonClass jClass = definition.getModel().getJsonClass(aClass);
        return parse(file, definition, jClass, debbugLevel);
    }

    public static JsonItem parse(String s, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return parse(s, definition, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return parse(file, definition, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(final URL url1, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return parse(url1, definition, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(Reader in, JsonItemDefinition definition, JsonClass root) throws IOException, JsonParseException {
        return parse(in, definition, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(File file, JsonItemDefinition definition, Class<?> aClass) throws JsonParseException, IOException {
        return parse(file, definition, aClass, JsonDebugLevel.SIMPLE);
    }

    // New convenience: parse JSON to a Map<String,Object>
    public static Map<String, Object> parseToMap(String s, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return JsonMapFacade.parseToMap(s, definition, root);
    }

    public static Map<String, Object> parseToMap(File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return JsonMapFacade.parseToMap(file, definition, root);
    }

    public static Map<String, Object> parseToMap(Reader in, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return JsonMapFacade.parseToMap(in, definition, root);
    }

    public static Map<String, Object> parseToMap(URL url1, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return JsonMapFacade.parseToMap(url1, definition, root);
    }

    public static JsonItem parse(Map<String, Object> root, JsonItemDefinition definition, Class<?> aClass) throws JsonParseException {
        return parse(root, definition, aClass, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(Map<String, Object> root, JsonItemDefinition definition, Class<?> aClass, JsonDebugLevel debbugLevel) throws JsonParseException {
        if (definition == null) {
            throw new IllegalArgumentException("JsonItemDefinition must not be null");
        }
        JsonClass jClass = aClass == null ? null : definition.getModel().getJsonClass(aClass);
        try {
            de.jare.jsoncasted.parser.inner.MapParseStreamReader mpsr = new de.jare.jsoncasted.parser.inner.MapParseStreamReader(root, debbugLevel);
            return new RootParser(definition, jClass).parse(mpsr);
        } catch (IOException ex) {
            throw new JsonParseException("Unexpected IO error while parsing map: " + ex.getMessage());
        }
    }

    // Overloads accepting JsonClass directly (tests use JsonClass)
    public static JsonItem parse(Map<String, Object> root, JsonItemDefinition definition, JsonClass rootClass) throws JsonParseException {
        return parse(root, definition, rootClass, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(Map<String, Object> root, JsonItemDefinition definition, JsonClass rootClass, JsonDebugLevel debbugLevel) throws JsonParseException {
        if (definition == null) {
            throw new IllegalArgumentException("JsonItemDefinition must not be null");
        }
        try {
            de.jare.jsoncasted.parser.inner.MapParseStreamReader mpsr = new de.jare.jsoncasted.parser.inner.MapParseStreamReader(root, debbugLevel);
            return new RootParser(definition, rootClass).parse(mpsr);
        } catch (IOException ex) {
            throw new JsonParseException("Unexpected IO error while parsing map: " + ex.getMessage());
        }
    }

}
