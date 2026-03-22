package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;

import java.io.*;
import java.util.*;

/**
 * Simple recursive-descent JSON parser service. Provides overloads for
 * StringReader, FileReader and BufferedReader and returns a JsonNode.
 */
public class JsonParserService {

    public JsonNode parse(StringReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public JsonNode parse(FileReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public JsonNode parse(BufferedReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public JsonNode parse(Reader reader) throws IOException, JsonParseException, JsonParseException {
        ParseStreamReader psr = new ParseStreamReader(reader, JsonDebugLevel.SIMPLE);
        return RootParser.parse(psr);
    }

}
