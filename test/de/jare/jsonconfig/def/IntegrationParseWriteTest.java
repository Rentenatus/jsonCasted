/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Simple integration test: parse a config, build instance and write it back.
 */
public class IntegrationParseWriteTest {

    @Test
    public void testParseBuildWriteRoundtrip() {
        File configFile = new File("./assets/config/config1.json");
        JsonConfigDefinition definition = JsonConfigDefinition.getInstance();

        JsonItem item = null;
        try {
            item = JsonParser.parse(configFile, definition, definition.getConfigRoot());
        } catch (JsonParseException | IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(item, "Parsed JsonItem should not be null");

        Object built = null;
        try {
            built = item.buildInstance();
        } catch (JsonBuildException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(built, "Built instance should not be null");

        try {
            String out = JsonWriter.writeToString(built, definition, definition.getConfigRoot());
            assertNotNull(out, "Serialized JSON should not be null");
            System.out.println("Serialized output length: " + out.length());
        } catch (IOException | JsonParseException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
    }
}
