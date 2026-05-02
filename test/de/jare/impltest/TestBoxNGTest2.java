/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.parserservice.JsonParserService;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.writer.inner.RootObjectWriter;
import java.io.File;
import java.io.IOException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

/**
 * Test class for the TestBox implementation.
 * Tests parsing and building of complex JSON structures with the ImplTestDefinition.
 *
 * @author Janusch Rentenatus
 */
public class TestBoxNGTest2 {

    /**
     * Default constructor for TestBoxNGTest.
     */
    public TestBoxNGTest2() {
    }

    /**
     * Sets up the test class.
     * Printed to stdout for test tracking.
     *
     * @throws Exception If setup fails.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start TestBoxNGTest2.");
    }

    /**
     * Tears down the test class.
     * Printed to stdout for test tracking.
     *
     * @throws Exception If teardown fails.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End TestBoxNGTest2.");
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
     * Tests parsing and building with a valid testbox configuration.
     * Target version using the new parser pipeline.
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
            obj1 = JsonParser.parse(res, descriptor, definition.getTestBox().getcName(), JsonDebugLevel.INFO);
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(obj1);
        System.out.println("Target=============================================== Print node");
        RootObjectWriter writer = new RootObjectWriter(definition, definition.getTestBox());
        writer.writeNode(System.out, node);
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
        writer.write(System.out, definition.getTestBox(), root);

        System.out.println("Target=============================================== Comment");
        assertNotNull(root.getOne());
        assertNotNull(root.getList());
        assertNotNull(root.getArr());
        System.out.println("subsub  = " + root.getSubsub().getText());
        System.out.println("one  = " + root.getOne().getText());
        for (ValueInterface elem : root.getArr()) {
            System.out.println("elem  > " + elem.getText());
        }
        System.out.println("Target===============================================");
        System.out.println(root);
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

        File configFile = new File("./assets/config/testbox_2.json");
        testModel(configFile, ImplTestDefinition2.getInstance());
    }

    /**
     * Tests parsing and building with a testbox configuration containing errors.
     * Target version using the new parser pipeline.
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
            obj1 = JsonParser.parse(res, descriptor, definition.getTestBox().getcName(), JsonDebugLevel.INFO);
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(obj1);
        System.out.println("Target=============================================== Print node");
        RootObjectWriter writer = new RootObjectWriter(definition, definition.getTestBox());
        writer.writeNode(System.out, node);
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
        writer.write(System.out, definition.getTestBox(), root);

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
    public void testModel3_error() {
        System.out.println("===============================================");
        System.out.println("testModel3");
        System.out.println("===============================================");

        File configFile = new File("./assets/config/testbox_error.json");
        testModel_error(configFile, ImplTestDefinition.getInstance());
    }

}
