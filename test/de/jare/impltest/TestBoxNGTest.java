/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

import de.jare.debug.JsonDebugLevel;
import de.jare.impltest.lib.ImplTestDefinition;
import de.jare.impltest.lib.TestBox;
import de.jare.impltest.lib.ValueInterface;
import de.jare.jsoncasted.io.JsonItemWriter;
import de.jare.jsoncasted.io.JsonNodeWriter;
import de.jare.jsoncasted.io.JsonObjectWriter;
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
import static org.testng.Assert.assertNull;
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
public class TestBoxNGTest {

    /**
     * Default constructor for TestBoxNGTest.
     */
    public TestBoxNGTest() {
    }

    /**
     * Sets up the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If setup fails.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start TestBoxNGTest.");
    }

    /**
     * Tears down the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If teardown fails.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End TestBoxNGTest.");
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
    private Object testModel(File configFile, ImplTestDefinition definition) {
        System.out.println("Target=============================================== File");
        System.out.println(configFile.getAbsolutePath());

        JsonItem obj1 = null;
        JsonItem obj2 = null;
        JsonNode node = null;
        try {
            final JsonResource res = JsonParserService.parse(configFile, JsonDebugLevel.INFO);
            System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
            System.out.println(res.getExpectedBox());
            System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
            node = res.getRoot();
            final JsonModelDescriptor descriptor = definition.getDescriptor();
            final WoodResolution resolution = JsonParser.parse(res, descriptor, definition.getTestBox().getcName(), JsonDebugLevel.INFO);
            obj1 = resolution.getAnswer();
            obj2 = resolution.getAnswer("save");
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(obj1);
        assertNotNull(obj2);
        JsonItem obj3 = obj1.cloneDeep();
        assertNotNull(obj3);
        System.out.println("Target=============================================== Print node");
        try {
            JsonNodeWriter.write(node, System.out);
        } catch (IOException | JsonParseException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        System.out.println("Target=============================================== Print items");
        try {
            JsonItemWriter.write(obj1, System.out);
        } catch (IOException | JsonParseException | JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        try {
            JsonItemWriter.write(obj2, System.out);
        } catch (IOException | JsonParseException | JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        System.out.println("Target=============================================== Config Class");
        System.out.println(obj1.getClass());
        System.out.println(obj2.getClass());

        TestBox root1 = null;
        TestBox root2 = null;
        try {
            final Object buildInstance1 = JsonBuilder.buildInstance(definition.getModel(), false, obj1);
            System.out.println(buildInstance1.getClass().getName());
            assertNotNull(root1 = (TestBox) buildInstance1);
        } catch (JsonBuildException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        try {
            final Object buildInstance2 = JsonBuilder.buildInstance(definition.getModel(), false, obj2);
            System.out.println(buildInstance2.getClass().getName());
            assertNotNull(root2 = (TestBox) buildInstance2);
        } catch (JsonBuildException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println("Target=============================================== Print objects");
        try {
            JsonObjectWriter.write(root1, System.out, definition, definition.getTestBox());
        } catch (IOException | JsonParseException | JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        try {
            JsonObjectWriter.write(root2, System.out, definition, definition.getTestBox());
        } catch (IOException | JsonParseException | JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println();
        System.out.println("Target=============================================== Comment");
        assertNotNull(root1.getOne());
        assertNotNull(root1.getList());
        assertNotNull(root1.getArr());
        System.out.println("subsub  = " + root1.getSubsub().getText());
        System.out.println("one  = " + root1.getOne().getText());
        for (ValueInterface elem : root1.getArr()) {
            System.out.println("elem  > " + elem.getText());
        }
        System.out.println("Target===============================================");
        System.out.println(root1);
        return root1;
    }

    /**
     * Test of getModel method with valid testbox configuration.
     */
    @Test
    public void testModel1() {
        System.out.println("===============================================");
        System.out.println("testModel1");
        System.out.println("===============================================");

        File configFile = new File("./assets/config/testbox.json");
        testModel(configFile, ImplTestDefinition.getInstance());
    }

    /**
     * Tests parsing and building with a testbox configuration containing errors. Target version using the new parser
     * pipeline.
     *
     * @param configFile The configuration file to parse.
     * @param definition The ImplTestDefinition to use.
     * @return The built TestBox instance.
     */
    private Object testModel_error(File configFile, ImplTestDefinition definition) {
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
            obj1 = JsonParser.parse(res, descriptor, definition.getTestBox().getcName(), JsonDebugLevel.INFO).getAnswer();
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
        System.out.println("Target=============================================== Config Class");
        System.out.println(obj1.getClass());

        TestBox root = null;
        try {
            final Object buildInstance1 = JsonBuilder.buildInstance(definition.getModel(), false, obj1);
            System.out.println(buildInstance1.getClass().getName());
            assertNotNull(root = (TestBox) buildInstance1);

        } catch (JsonBuildException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println("Target=============================================== Print object");
        try {
            JsonObjectWriter.write(root, System.out, definition, definition.getTestBox());
        } catch (IOException | JsonParseException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        } catch (JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        System.out.println("Target=============================================== Comment");
        assertNull(root.getOne());
        assertNotNull(root.getList());
        assertNotNull(root.getArr());
        System.out.println("subsub  = " + root.getSubsub().getText());
        for (ValueInterface elem : root.getArr()) {
            System.out.println("elem  > " + (elem == null ? "null" : elem.getText()));
        }
        System.out.println("Target===============================================");
        System.out.println(root);
        return root;
    }

    /**
     * Test of getModel method with testbox configuration containing errors.
     */
    @Test
    public void testModel1_error() {
        System.out.println("===============================================");
        System.out.println("testModel1");
        System.out.println("===============================================");

        File configFile = new File("./assets/config/testbox_error.json");
        testModel_error(configFile, ImplTestDefinition.getInstance());
    }

}
