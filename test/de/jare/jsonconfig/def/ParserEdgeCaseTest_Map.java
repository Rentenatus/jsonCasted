/* Copyright (C) 2022 Janusch Rentenatus & Thomas Weber */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.tools.JsonMapFacade;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import org.testng.annotations.Test;

public class ParserEdgeCaseTest_Map {

    @Test(expectedExceptions = {JsonParseException.class})
    public void testInvalidJsonThrows_Map() throws Exception {
        String bad = "{ \"key\": \"value }"; // unterminated string
        JsonConfigDefinition def = JsonConfigDefinition.getInstance();
        // should throw JsonParseException due to malformed JSON
        JsonMapFacade.parseToMap(bad, def, def.getConfigRoot());
    }

    @Test(expectedExceptions = {JsonParseException.class})
    public void testUnterminatedStringThrows_Map() throws Exception {
        String bad = "\"unterminated"; // just a string start
        JsonConfigDefinition def = JsonConfigDefinition.getInstance();
        JsonMapFacade.parseToMap(bad, def, def.getConfigRoot());
    }
}
