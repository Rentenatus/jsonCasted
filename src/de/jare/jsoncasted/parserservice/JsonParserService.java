/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonResource;
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

    public static JsonResource parse(String s) throws JsonParseException, IOException {
        return parse(new StringReader(s), JsonResource.empty());
    }

    public static JsonResource parse(File file) throws JsonParseException, IOException {
        FileReader in;
        try {
            in = new FileReader(file);
            return parse(in, file.getPath());
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JsonResource parse(final URL url1) throws JsonParseException, IOException {
        BufferedReader in;
        try {
            final InputStreamReader inputStreamReader = new InputStreamReader(url1.openStream());
            in = new BufferedReader(inputStreamReader);
            return parse(in, url1.toString());
        } catch (FileNotFoundException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JsonResource parse(StringReader reader, String filename) throws IOException, JsonParseException {
        return parse((Reader) reader, JsonResource.forFile(filename));
    }

    public static JsonResource parse(FileReader reader, String filename) throws IOException, JsonParseException {
        return parse((Reader) reader, JsonResource.forFile(filename));
    }

    public static JsonResource parse(BufferedReader reader, String filename) throws IOException, JsonParseException {
        return parse((Reader) reader, JsonResource.forFile(filename));
    }

    public static JsonResource parse(Reader reader, JsonResource container) throws IOException, JsonParseException, JsonParseException {
        ParseStreamReader psr = new ParseStreamReader(reader, JsonDebugLevel.SIMPLE);
        return RootParser.parse(psr, container);
    }

}
