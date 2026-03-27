/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple recursive-descent JSON parser service. Provides overloads for
 * StringReader, FileReader and BufferedReader and returns a JsonNode.
 *
 * @author Janusch Rentenatus
 */
public class JsonParserService {

    public static JsonNode parse(String s) throws JsonParseException, IOException {
        return parse(new StringReader(s));
    }

    public static JsonNode parse(File file) throws JsonParseException, IOException {
        FileReader in;
        try {
            in = new FileReader(file);
            return parse(in);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JsonNode parse(final URL url1) throws JsonParseException, IOException {
        BufferedReader in;
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(url1.openStream());
            in = new BufferedReader(inputStreamReader);
            return parse(in);
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JsonNode parse(StringReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public static JsonNode parse(FileReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public static JsonNode parse(BufferedReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public static JsonNode parse(Reader reader) throws IOException, JsonParseException, JsonParseException {
        ParseStreamReader psr = new ParseStreamReader(reader, JsonDebugLevel.SIMPLE);
        return RootParser.parse(psr);
    }

}
