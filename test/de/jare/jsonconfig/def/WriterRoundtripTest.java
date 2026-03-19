/* Copyright (C) 2022 Janusch Rentenatus & Thomas Weber */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonWriter;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import java.io.File;
import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class WriterRoundtripTest {

    @Test
    public void testParseAndWriteSmallConfig() {
        File configFile = new File("./assets/config/config1.json");
        JsonConfigDefinition def = JsonConfigDefinition.getInstance();
        JsonItem item = null;
        try {
            item = JsonParser.parse(configFile, def, def.getConfigRoot());
        } catch (JsonParseException | IOException ex) {
            fail(ex.getMessage(), ex);
        }
        assertNotNull(item);
        Object built = null;
        try {
            built = item.buildInstance();
        } catch (JsonBuildException ex) {
            fail(ex.getMessage(), ex);
        }
        assertNotNull(built);
        try {
            String out = JsonWriter.writeToString(built, def, def.getConfigRoot());
            assertNotNull(out);
            assertTrue(out.length() > 0);
        } catch (IOException | JsonParseException ex) {
            fail(ex.getMessage(), ex);
        }
    }
}
