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

}
