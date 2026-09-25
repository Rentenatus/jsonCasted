/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

import de.jare.debug.JsonDebugLevel;
import de.jare.impltest.lib.ImplTestDefinition2;
import de.jare.impltest.lib.TestBox;
import de.jare.jsoncasted.io.JsonItemWriter;
import de.jare.jsoncasted.io.JsonNodeWriter;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.JsonWriteException;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.io.parserservice.JsonParserService;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the TestBox implementation. Tests parsing and building of complex JSON structures with the
 * ImplTestDefinition.
 *
 * @author Janusch Rentenatus
 */
public class TestBoxNGTest3 {

    /**
     * Default constructor for TestBoxNGTest.
     */
    public TestBoxNGTest3() {
    }

    /**
     * Sets up the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If setup fails.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start TestBoxNGTest3.");
    }

    /**
     * Tears down the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If teardown fails.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End TestBoxNGTest3.");
        System.out.println("===============================================");
    }

    /**
     * Sets up the test method.
     *
     * @throws Exception If setup fails.
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    /**
     * Tears down the test method.
     *
     * @throws Exception If teardown fails.
     */
    @AfterMethod
    public void tearDownMethod() throws Exception {

    }

    /**
     * Tests parsing and building with a valid testbox configuration. Target version using the new parser pipeline.
     *
     * @param configFile The configuration file to parse.
     * @param definition The ImplTestDefinition to use.
     * @return The built TestBox instance.
     */
    private Object testModel(File configFile, ImplTestDefinition2 definition) {
        System.out.println("Target=============================================== File");
        System.out.println(configFile.getAbsolutePath());

        JsonItem obj1 = null;
        JsonNode node = null;
        try {
            final JsonResource res = JsonParserService.parse(configFile, JsonDebugLevel.INFO);
            System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
            System.out.println(res.getExpectedBox());
            System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
            node = res.getRoot();
            final JsonModelDescriptor descriptor = definition.getDescriptor();
            final WoodResolution resolution = JsonParser.parse(res, descriptor, definition.getRootClass().getcName(),
                    JsonDebugLevel.INFO);
            JsonParser.checkCycles(resolution);
            obj1 = resolution.getAnswer();
            for (JsonParseException ex : resolution.getUnmodifiableExceptions()) {
                System.out.println("ERR: " + ex.getMessage());
            }
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(obj1);
        System.out.println("Target=============================================== Print node");
        try {
            JsonNodeWriter.write(node, System.out);
        } catch (IOException | JsonParseException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        System.out.println("Target=============================================== Print item");
        try {
            JsonItemWriter.write(obj1, System.out);
        } catch (IOException | JsonParseException | JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        System.out.println("Target=============================================== Config Class");
        System.out.println(obj1.getClass());

        TestBox root = null;
        try {
            final Object buildInstance1 = JsonBuilder.buildInstance(definition.getModel(), true, obj1);
            System.out.println(buildInstance1.getClass().getName());
            assertNotNull(root = (TestBox) buildInstance1);

        } catch (JsonBuildException ex) {
            // detected that item serialization contains a disallowed cycle.
            return null;
        }
        fail("It was not detected that item serialization contains a disallowed cycle.");
        return root;
    }

    /**
     * Test of getModel method with valid testbox configuration.
     */
    @Test
    public void testModel3() {
        System.out.println("===============================================");
        System.out.println("testModel3");
        System.out.println("===============================================");

        File configFile = new File("./assets/config/testbox_3.json");
        testModel(configFile, ImplTestDefinition2.getInstance());

    }

}
