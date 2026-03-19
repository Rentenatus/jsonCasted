/* Copyright (C) 2022 Janusch Rentenatus & Thomas Weber */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class ParserEdgeCaseTest {

    @Test(expectedExceptions = {JsonParseException.class})
    public void testInvalidJsonThrows() throws Exception {
        String bad = "{ \"key\": \"value }"; // unterminated string
        JsonConfigDefinition def = JsonConfigDefinition.getInstance();
        // should throw JsonParseException due to malformed JSON
        JsonParser.parse(bad, def, def.getConfigRoot());
    }

    @Test(expectedExceptions = {JsonParseException.class})
    public void testUnterminatedStringThrows() throws Exception {
        String bad = "\"unterminated"; // just a string start
        JsonConfigDefinition def = JsonConfigDefinition.getInstance();
        JsonParser.parse(bad, def, def.getConfigRoot());
    }
}
