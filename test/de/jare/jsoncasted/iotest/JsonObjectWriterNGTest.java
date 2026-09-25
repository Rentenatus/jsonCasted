/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.iotest;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonObjectWriter;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import de.jare.jsonconfig.item.ConfigRoot;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
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
 * @author Janusch Rentenatus
 */
public class JsonObjectWriterNGTest {

    private static File configFile;
    private static String testOutPath;
    private static ConfigRoot configRoot;
    private static JsonConfigDefinition definition;
    private static File outFile;

    public JsonObjectWriterNGTest() {
    }

    /**
     * Sets up the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If setup fails.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start JsonObjectWriterNGTest.");

        // Set up paths relative to test_assets working directory
        String basePath = "./assets/config/config1.json";
        configFile = new File(basePath);

        // Create out directory if it doesn't exist
        File outDir = new File("./out");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        testOutPath = "./out/test_write_desc.json";
        outFile = new File(testOutPath);

        // Load config directly using JsonParser
        definition = JsonConfigDefinition.getInstance();
        configRoot = loadConfigRoot(configFile, definition);

        if (configRoot == null) {
            // Fallback: create a simple config root
            configRoot = new ConfigRoot();
        }

        System.out.println("Config loaded from: " + configFile.getAbsolutePath());
        System.out.println("Config root class: " + (configRoot != null ? configRoot.getClass().getName() : "null"));
    }

    private static ConfigRoot loadConfigRoot(File configFile, JsonConfigDefinition definition) {
        try {
            WoodResolution reso = JsonParser.parse(configFile, definition, definition.getRootClass());

            if (reso.hasExceptions()) {
                final List<JsonParseException> exceptions = reso.getUnmodifiableExceptions();
                for (Exception exception : exceptions) {
                    Logger.getLogger(JsonObjectWriterNGTest.class.getName()).log(Level.SEVERE, "Parsing error: ", exception);
                }
            }
            JsonItem item = reso.getAnswer();
            return (ConfigRoot) JsonBuilder.buildInstance(definition.getModel(), true, item);
        } catch (JsonParseException | IOException | JsonBuildException | NullPointerException ex) {
            Logger.getLogger(JsonObjectWriterNGTest.class.getName()).log(Level.SEVERE, "Error loading config: ", ex);
            return null;
        }
    }

    /**
     * Tears down the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If teardown fails.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End JsonObjectWriterNGTest.");
        System.out.println("===============================================");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Ensure config1.json is saved to testOutPath before each test that needs a file
        if (configRoot != null && definition != null) {
            JsonObjectWriter.write(configRoot, new File(testOutPath), definition, definition.getRootClass());
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of writeToString method with 4 args (object, definition, root, charset), of class JsonObjectWriter.
     */
    @Test
    public void testWriteToString_4args_1() throws Exception {
        System.out.println("writeToString (4 args with charset)");
        String charsetName = "UTF-8";
        String result = JsonObjectWriter.writeToString(configRoot, definition, definition.getRootClass(), charsetName);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("main") || result.contains("other") || result.contains("Test1"));
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of writeToString method with 3 args (object, definition, root), of class JsonObjectWriter.
     */
    @Test
    public void testWriteToString_3args_1() throws Exception {
        System.out.println("writeToString (3 args)");
        String result = JsonObjectWriter.writeToString(configRoot, definition, definition.getRootClass());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("profile") || result.contains("features"));
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 4 args (object, file, definition, root), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_4args_1() throws Exception {
        System.out.println("write (4 args: object, file, definition, root)");
        File tempOutFile = new File("./out/test_write_4args.json");

        JsonObjectWriter.write(configRoot, tempOutFile, definition, definition.getRootClass());

        assertTrue(tempOutFile.exists());
        assertTrue(tempOutFile.length() > 0);

        // Verify content
        String content = new String(Files.readAllBytes(tempOutFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(content.contains("profile") || content.contains("main"));
        System.out.println("File written: " + tempOutFile.getAbsolutePath());
    }

    /**
     * Test of write method with 5 args (object, file, fileModel, definition, root), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_5args_1() throws Exception {
        System.out.println("write (5 args: object, file, fileModel, definition, root)");
        File tempOutFile = new File("./out/test_write_5args.json");
        File modelFile = new File("./out/test_write_5args_model.json");

        JsonObjectWriter.write(configRoot, tempOutFile, modelFile, definition, definition.getRootClass());

        assertTrue(tempOutFile.exists());
        assertTrue(tempOutFile.length() > 0);
        assertTrue(modelFile.exists());
        assertTrue(modelFile.length() > 0);

        System.out.println("Data file written: " + tempOutFile.getAbsolutePath());
        System.out.println("Model file written: " + modelFile.getAbsolutePath());
    }

    /**
     * Test of writeToString method with 3 args (object, definition, charset), of class JsonObjectWriter.
     */
    @Test
    public void testWriteToString_3args_2() throws Exception {
        System.out.println("writeToString (object, definition, charset)");
        String charsetName = "UTF-8";
        String result = JsonObjectWriter.writeToString(configRoot, definition, charsetName);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of writeToString method with 2 args (object, definition), of class JsonObjectWriter.
     */
    @Test
    public void testWriteToString_Object_JsonItemDefinition() throws Exception {
        System.out.println("writeToString (object, definition)");
        String result = JsonObjectWriter.writeToString(configRoot, definition);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("main") || result.contains("profile"));
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 3 args (object, file, definition), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_3args() throws Exception {
        System.out.println("write (object, file, definition)");
        File tempOutFile = new File("./out/test_write_3args.json");

        JsonObjectWriter.write(configRoot, tempOutFile, definition);

        assertTrue(tempOutFile.exists());
        assertTrue(tempOutFile.length() > 0);

        String content = new String(Files.readAllBytes(tempOutFile.toPath()), StandardCharsets.UTF_8);
        assertNotNull(content);
        System.out.println("File written: " + tempOutFile.getAbsolutePath());
    }

    /**
     * Test of write method with 4 args (object, outputstream, definition, root), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_4args_2() throws Exception {
        System.out.println("write (object, OutputStream, definition, root)");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JsonObjectWriter.write(configRoot, out, definition, definition.getRootClass());

        String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("profile") || result.contains("main"));
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 5 args (object, outputstream, definition, root, debugLevel), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_5args_2() throws Exception {
        System.out.println("write (object, OutputStream, definition, root, debugLevel)");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonDebugLevel debugLevel = JsonDebugLevel.SIMPLE;

        JsonObjectWriter.write(configRoot, out, definition, definition.getRootClass(), debugLevel);

        String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 5 args (object, outputstream, model, castingLevel, root), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_5args_3() throws Exception {
        System.out.println("write (object, OutputStream, model, castingLevel, root)");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonModel model = definition.getModel();
        JsonCastingLevel castingLevel = JsonCastingLevel.NEVER;

        JsonObjectWriter.write(configRoot, out, model, castingLevel, definition.getRootClass());

        String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 6 args (object, outputstream, definitionsContext, castingLevel, root, debugLevel), of
     * class JsonObjectWriter.
     */
    @Test
    public void testWrite_6args() throws Exception {
        System.out.println("write (6 args: object, OutputStream, DefinitionsContext, castingLevel, root, debugLevel)");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DefinitionsContext definitionsContext = new DefinitionsContext(definition.getModel(), null);
        JsonCastingLevel castingLevel = JsonCastingLevel.NEVER;
        JsonDebugLevel debugLevel = JsonDebugLevel.SIMPLE;

        JsonObjectWriter.write(configRoot, out, definitionsContext, castingLevel, definition.getRootClass(), debugLevel);

        String result = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 5 args (object, file, definition, root, debugLevel), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_5args_4() throws Exception {
        System.out.println("write (object, file, definition, root, debugLevel)");
        File tempOutFile = new File("./out/test_write_5args_4.json");
        JsonDebugLevel debugLevel = JsonDebugLevel.SIMPLE;

        JsonObjectWriter.write(configRoot, tempOutFile, definition, definition.getRootClass(), debugLevel);

        assertTrue(tempOutFile.exists());
        assertTrue(tempOutFile.length() > 0);

        String content = new String(Files.readAllBytes(tempOutFile.toPath()), StandardCharsets.UTF_8);
        assertNotNull(content);
        System.out.println("File written: " + tempOutFile.getAbsolutePath());
    }

    /**
     * Test of writeToString method with 4 args (object, definition, root, debugLevel), of class JsonObjectWriter.
     */
    @Test
    public void testWriteToString_4args_2() throws Exception {
        System.out.println("writeToString (object, definition, root, debugLevel)");
        JsonDebugLevel debugLevel = JsonDebugLevel.SIMPLE;
        String result = JsonObjectWriter.writeToString(configRoot, definition, definition.getRootClass(), debugLevel);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("profile") || result.contains("main"));
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of writeToString method with 3 args (object, definition, debugLevel), of class JsonObjectWriter.
     */
    @Test
    public void testWriteToString_3args_3() throws Exception {
        System.out.println("writeToString (object, definition, debugLevel)");
        JsonDebugLevel debugLevel = JsonDebugLevel.SIMPLE;
        String result = JsonObjectWriter.writeToString(configRoot, definition, debugLevel);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("Result length: " + result.length());
    }

    /**
     * Test of write method with 4 args (object, file, definition, debugLevel), of class JsonObjectWriter.
     */
    @Test
    public void testWrite_4args_3() throws Exception {
        System.out.println("write (object, file, definition, debugLevel)");
        File tempOutFile = new File("./out/test_write_4args_3.json");
        JsonDebugLevel debugLevel = JsonDebugLevel.SIMPLE;

        JsonObjectWriter.write(configRoot, tempOutFile, definition, debugLevel);

        assertTrue(tempOutFile.exists());
        assertTrue(tempOutFile.length() > 0);

        String content = new String(Files.readAllBytes(tempOutFile.toPath()), StandardCharsets.UTF_8);
        assertNotNull(content);
        System.out.println("File written: " + tempOutFile.getAbsolutePath());
    }

}
