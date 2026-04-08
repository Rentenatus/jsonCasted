/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.jare.impltest;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.parserservice.JsonParserService;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
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
import static org.testng.Assert.fail;

/**
 *
 * @author Janusch Rentenatus
 */
public class TestBoxNGTest {

    public TestBoxNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start TestBoxNGTest.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End TestBoxNGTest.");
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
        System.out.println("===============================================");
        System.out.println("getLangDef");
        System.out.println("===============================================");

        File configFile = new File("./assets/config/testbox.json");
        testModel(configFile, ImplTestDefinition.getInstance());
    }

    /**
     * Target version.
     *
     * @param configFile
     * @param definition
     * @return
     */
    private Object testModel(File configFile, ImplTestDefinition definition) {
        System.out.println("Target=============================================== File");
        System.out.println(configFile.getAbsolutePath());

        JsonItem obj1 = null;
        JsonNode node = null;
        try {
            final JsonResource res = JsonParserService.parse(configFile);
            System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
            System.out.println(res.getExpectedBox());
            System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWw");
            node = res.getRoot();
            final JsonModelDescriptor descriptor = definition.getDescriptor();
            obj1 = JsonParser.parse(node, descriptor, definition.getTestBox().getcName(), JsonDebugLevel.INFO);
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
            final Object buildInstance1 = obj1.buildInstance(definition.getModel());
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
        for (ValueInterface elem : root.getArr()) {
            System.out.println("elem  > " + elem.getText());
        }
        System.out.println("Target===============================================");
        System.out.println(root);
        return root;
    }
}
