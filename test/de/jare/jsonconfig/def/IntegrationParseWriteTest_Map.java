/* Copyright (C) 2022 Janusch Rentenatus & Thomas Weber */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonWriter;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.tools.JsonMapFacade;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class IntegrationParseWriteTest_Map {

    @Test
    public void testParseBuildWriteRoundtrip_Map() {
        File configFile = new File("./assets/config/config1.json");
        JsonConfigDefinition definition = JsonConfigDefinition.getInstance();

        try {
            // original parse
            JsonItem item1 = JsonParser.parse(configFile, definition, definition.getConfigRoot());
            Object built1 = item1.buildInstance();
            String out1 = JsonWriter.writeToString(built1, definition, definition.getConfigRoot());

            // map-based parse
            Map<String, Object> map = JsonMapFacade.parseToMap(configFile, definition, definition.getConfigRoot());
            JsonItem item2 = JsonParser.parse(map, definition, definition.getConfigRoot());
            Object built2 = item2.buildInstance();
            String out2 = JsonWriter.writeToString(built2, definition, definition.getConfigRoot());

            assertEquals(out1, out2, "Serialized output must be identical for file vs map parse");

        } catch (JsonParseException | IOException | JsonBuildException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
    }
}
