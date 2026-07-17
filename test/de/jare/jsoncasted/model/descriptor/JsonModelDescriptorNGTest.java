/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/EmptyTestNGTest.java to edit this template
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.JsonWriteException;
import de.jare.jsoncasted.model.descriptor.def.JsonDescriptorDefinition;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Administrator
 */
public class JsonModelDescriptorNGTest {

    public JsonModelDescriptorNGTest() {
    }

    /**
     * Sets up the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If setup fails.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start JsonModelDescriptorNGTest.");
    }

    /**
     * Tears down the test class. Printed to stdout for test tracking.
     *
     * @throws Exception If teardown fails.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End JsonConfigFactoryNGTest.");
        System.out.println("===============================================");
    }

    /**
     * Test of saveAs method, of class JsonModelDescriptor. Creates a test descriptor and saves it to
     * ./out/test_desc.json Creates the descriptor definition's own model descriptor and saves it to
     * ./out/description.json
     */
    @Test
    public void testSaveAs() throws IOException, JsonParseException, JsonWriteException {
        System.out.println("saveAs");

        // Create output directory
        File outDir = new File("./out");
        if (!outDir.exists()) {
            assertTrue(outDir.mkdirs(), "Failed to create output directory");
        }

        // Create a test model descriptor with some types
        JsonModelDescriptor testDescriptor = new JsonModelDescriptor("TestModel");

        // Add a simple type descriptor
        JsonTypeDescriptor stringType = new JsonTypeDescriptor("java.lang.String")
                .withNodeType(de.jare.jsoncasted.lang.JsonNodeType.STRING)
                .withPrimitive(true);
        testDescriptor.addType(stringType);

        // Save test description to ./out/test_desc.json
        String testDescPath = "./out/test_desc.json";
        System.out.println("=============================================== testDescriptor");
        System.out.println(testDescriptor);
        System.out.println("=============================================== testDescriptor");
        testDescriptor.saveAs(testDescPath);

        // Verify test_desc.json was created and is valid JSON
        File testDescFile = new File(testDescPath);
        assertTrue(testDescFile.exists(), "test_desc.json was not created");
        assertTrue(testDescFile.length() > 0, "test_desc.json is empty");

        String testDescContent = new String(Files.readAllBytes(Paths.get(testDescPath)));
        assertFalse(testDescContent.isBlank(), "test_desc.json content is blank");
        assertTrue(testDescContent.trim().startsWith("{") && testDescContent.trim().endsWith("}"),
                "test_desc.json does not appear to be valid JSON");

        // Create a self-describing descriptor
        JsonModelDescriptor selfDescriptor = JsonDescriptorDefinition.getInstance().getModel().getOrCreateDescriptor();

        // Save self-description to ./out/description.json
        String selfDescPath = "./out/description.json";
        System.out.println("=============================================== selfDescriptor");
        System.out.println(selfDescriptor);
        System.out.println("=============================================== selfDescriptor");

        selfDescriptor.saveAs(selfDescPath);

        // Verify description.json was created and is valid JSON
        File selfDescFile = new File(selfDescPath);
        assertTrue(selfDescFile.exists(), "description.json was not created");
        assertTrue(selfDescFile.length() > 0, "description.json is empty");

        String selfDescContent = new String(Files.readAllBytes(Paths.get(selfDescPath)));
        assertFalse(selfDescContent.isBlank(), "description.json content is blank");
        assertTrue(selfDescContent.trim().startsWith("{") && selfDescContent.trim().endsWith("}"),
                "description.json does not appear to be valid JSON");

        System.out.println("Successfully saved test descriptor to: " + testDescPath);
        System.out.println("Successfully saved self descriptor to: " + selfDescPath);
    }

}
