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
import de.jare.jsonconfig.JsonConfigHelper;
import de.jare.jsonconfig.item.ConfigFeature;
import de.jare.jsonconfig.item.ConfigRoot;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jRent
 */
public class JsonConfigFactoryNGTest {

    public JsonConfigFactoryNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start JsonConfigFactoryNGTest.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End JsonConfigFactoryNGTest.");
        System.out.println("===============================================");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {

    }

    /**
     * Test of getModel method, of class JsonConfigDefinition.
     */
    @Test
    public void testModel1() {
        System.out.println("getLangDef");

        File configFile = new File("./assets/config/config1.json");
        testModel(configFile, JsonConfigDefinition.getInstance());
    }

    /**
     * Test of getModel method, of class JsonConfigDefinition.
     */
    @Test
    public void testModelSeed() {
        System.out.println("getLangDef");

        File configFile = new File("./assets/config/seedConfigTemplate.json");
        JsonConfigHelper helper = testModel(configFile, JsonConfigDefinition.getInstance());

        ConfigFeature f = helper.getFeature("main", "ollama");
        assertNotNull(f);

        System.out.println("host1: " + helper.getSetting(f, "host1"));
        System.out.println("host2: " + helper.getSetting(f, "host2"));
        System.out.println("host3: " + helper.getSetting(f, "host3"));
        System.out.println("port: " + helper.getSetting(f, "port"));

        f = helper.getFeature("main", "prompt");
        assertNotNull(f);

        System.out.println("incisive: " + helper.getSetting(f, "incisive"));
        System.out.println("codegen: " + helper.getSetting(f, "codegen"));
        System.out.println("timeoutDelay: " + helper.getSetting(f, "timeoutDelay"));
        System.out.println("rewriteStory: " + helper.getEnablement(f, "rewriteStory"));
        System.out.println("user0: " + helper.getLabel(f, "user0"));

    }

    private JsonConfigHelper testModel(File configFile, JsonConfigDefinition definition) {
        System.out.println("=============================================== File");
        System.out.println(configFile.getAbsolutePath());

        JsonItem obj1 = null;
        try {
            obj1 = JsonParser.parse(configFile, definition, definition.getConfigRoot());
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(obj1);
        System.out.println("=============================================== Config Class");
        System.out.println(obj1.getClass());

        ConfigRoot root = null;
        try {
            final Object buildInstance1 = obj1.buildInstance();
            System.out.println(buildInstance1.getClass().getName());
            assertNotNull(root = (ConfigRoot) buildInstance1);

        } catch (JsonBuildException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println("=============================================== Comment");
        for (String comment : root.getComments()) {
            System.out.println("comment  > " + comment);
        }
        System.out.println("===============================================");
        return new JsonConfigHelper(root);
    }

}
