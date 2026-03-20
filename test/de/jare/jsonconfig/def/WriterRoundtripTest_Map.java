/* Copyright (C) 2022 Janusch Rentenatus & Thomas Weber */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonWriter;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.tools.JsonMapFacade;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class WriterRoundtripTest_Map {

    @Test
    public void testParseAndWriteSmallConfig_Map() {
        File configFile = new File("./assets/config/config1.json");
        JsonConfigDefinition def = JsonConfigDefinition.getInstance();
        try {
            JsonItem item1 = JsonParser.parse(configFile, def, def.getConfigRoot());
            Object built1 = item1.buildInstance();
            String out1 = JsonWriter.writeToString(built1, def, def.getConfigRoot());

            Map<String, Object> map = JsonMapFacade.parseToMap(configFile, def, def.getConfigRoot());
            JsonItem item2 = JsonParser.parse(map, def, def.getConfigRoot());
            Object built2 = item2.buildInstance();
            String out2 = JsonWriter.writeToString(built2, def, def.getConfigRoot());

            assertEquals(out1, out2, "Outputs must match between file and map parsing");

        } catch (JsonParseException | IOException | JsonBuildException ex) {
            fail(ex.getMessage(), ex);
        }
    }
}
