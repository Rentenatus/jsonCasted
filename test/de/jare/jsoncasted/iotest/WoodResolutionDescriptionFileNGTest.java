/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.iotest;

import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import java.io.File;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for verifying the descriptionFileMap functionality in WoodResolution. Tests that when loading JSON files
 * with _woodModel nodes containing fileName, the description file mapping is correctly populated.
 *
 * @author Janusch Rentenatus
 */
public class WoodResolutionDescriptionFileNGTest {

    private static File testReadFile;
    private static JsonConfigDefinition definition;

    public WoodResolutionDescriptionFileNGTest() {
    }

    /**
     * Sets up the test class.
     *
     * @throws Exception If setup fails.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start WoodResolutionDescriptionFileNGTest.");

        definition = JsonConfigDefinition.getInstance();
        String basePath = "./assets/read/test_read.json";
        testReadFile = new File(basePath);

        System.out.println("Test file: " + testReadFile.getAbsolutePath());
        System.out.println("Test file exists: " + testReadFile.exists());
    }

    /**
     * Tears down the test class.
     *
     * @throws Exception If teardown fails.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End WoodResolutionDescriptionFileNGTest.");
        System.out.println("===============================================");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test that parsing a JSON file with _woodModel containing fileName correctly populates the descriptionFileMap in
     * WoodResolution.
     */
    @Test
    public void testDescriptionFileMap_PopulatedFromWoodModel() throws Exception {
        System.out.println("testDescriptionFileMap_PopulatedFromWoodModel");

        if (!testReadFile.exists()) {
            System.out.println("Test file does not exist, skipping test: " + testReadFile.getAbsolutePath());
            return;
        }

        // Parse the file
        WoodResolution resolution = JsonParser.parse(testReadFile, definition, definition.getRootClass());

        assertNotNull(resolution, "WoodResolution should not be null");

        // Check that descriptionFileMap is populated
        Map<String, String> descriptionFileMap = resolution.getDescriptionFileMap();
        assertNotNull(descriptionFileMap, "descriptionFileMap should not be null");
        assertFalse(descriptionFileMap.isEmpty(), "descriptionFileMap should not be empty");

        // Check that the map contains the expected entry
        assertEquals(descriptionFileMap.size(), 1, "descriptionFileMap should contain exactly 1 entry");
        assertTrue(descriptionFileMap.containsKey("Seed"), "descriptionFileMap should contain key 'Seed'");

        // Check the file path value
        String filePath = resolution.getDescriptionFile("Seed");
        assertNotNull(filePath, "File path for model 'Seed' should not be null");
        assertTrue(filePath.contains("test_write_5args_model.json"), 
                "File path should contain 'test_write_5args_model.json', got '"+filePath+"'");
        assertTrue(filePath.contains("read"), 
                "File path should contain 'read' directory, got '"+filePath+"'");

        // Check helper methods
        assertTrue(resolution.hasDescriptionFile("Seed"), "hasDescriptionFile should return true for 'Seed'");
        assertFalse(resolution.hasDescriptionFile("NonExistent"), "hasDescriptionFile should return false for non-existent key");
        assertTrue(resolution.getDescriptionFileKeys().contains("Seed"), "getDescriptionFileKeys should contain 'Seed'");

        System.out.println("Description file mapping found: modelName='Seed', fileName='" + filePath + "'");
    }

    /**
     * Test that the descriptionFileMap is included in toString output.
     */
    @Test
    public void testDescriptionFileMap_InToString() throws Exception {
        System.out.println("testDescriptionFileMap_InToString");

        if (!testReadFile.exists()) {
            System.out.println("Test file does not exist, skipping test: " + testReadFile.getAbsolutePath());
            return;
        }

        // Parse the file
        WoodResolution resolution = JsonParser.parse(testReadFile, definition, definition.getRootClass());

        assertNotNull(resolution, "WoodResolution should not be null");

        // Convert to string and check it contains descriptionFileMap
        String toStringOutput = resolution.toString();
        assertNotNull(toStringOutput, "toString should not return null");
        assertTrue(toStringOutput.contains("descriptionFileMap"),
                "toString should contain 'descriptionFileMap'");

        System.out.println("toString output: " + toStringOutput);
    }

    /**
     * Test that parsing a JSON file without _woodModel results in empty descriptionFileMap.
     */
    @Test
    public void testDescriptionFileMap_EmptyWhenNoWoodModel() throws Exception {
        System.out.println("testDescriptionFileMap_EmptyWhenNoWoodModel");

        // Create a simple JSON string without _woodModel
        String simpleJson = "{\"test\": \"value\"}";

        // Parse the string
        WoodResolution resolution = JsonParser.parse(
                simpleJson,
                definition.getModel().getOrCreateDescriptor(),
                definition.getRootClass().getcName());

        assertNotNull(resolution, "WoodResolution should not be null");

        // Check that descriptionFileMap is empty
        Map<String, String> descriptionFileMap = resolution.getDescriptionFileMap();
        assertNotNull(descriptionFileMap, "descriptionFileMap should not be null");
        assertTrue(descriptionFileMap.isEmpty(), "descriptionFileMap should be empty when no _woodModel is present");

        System.out.println("Empty descriptionFileMap as expected");
    }

    /**
     * Test the individual methods of descriptionFileMap.
     */
    @Test
    public void testDescriptionFileMap_Methods() throws Exception {
        System.out.println("testDescriptionFileMap_Methods");

        // Create a new WoodResolution
        WoodResolution resolution = new WoodResolution();

        // Test putDescriptionFile
        resolution.putDescriptionFile("TestModel", "/path/to/test.json");

        // Test getDescriptionFile
        assertEquals(resolution.getDescriptionFile("TestModel"), "/path/to/test.json");
        assertNull(resolution.getDescriptionFile("NonExistent"));

        // Test hasDescriptionFile
        assertTrue(resolution.hasDescriptionFile("TestModel"));
        assertFalse(resolution.hasDescriptionFile("NonExistent"));

        // Test getDescriptionFileKeys
        assertTrue(resolution.getDescriptionFileKeys().contains("TestModel"));
        assertEquals(resolution.getDescriptionFileKeys().size(), 1);

        // Test getDescriptionFileMap
        Map<String, String> map = resolution.getDescriptionFileMap();
        assertEquals(map.size(), 1);
        assertEquals(map.get("TestModel"), "/path/to/test.json");

        // Test that map is unmodifiable
        try {
            map.put("NewModel", "/new/path.json");
            fail("getDescriptionFileMap should return an unmodifiable map");
        } catch (UnsupportedOperationException e) {
            // Expected
            System.out.println("Map is correctly unmodifiable");
        }

        System.out.println("All descriptionFileMap methods work correctly");
    }

    /**
     * Test that null checks work correctly for descriptionFileMap methods.
     */
    @Test
    public void testDescriptionFileMap_NullChecks() throws Exception {
        System.out.println("testDescriptionFileMap_NullChecks");

        WoodResolution resolution = new WoodResolution();

        // Test putDescriptionFile with null key
        try {
            resolution.putDescriptionFile(null, "/some/path.json");
            fail("putDescriptionFile should throw NullPointerException for null key");
        } catch (NullPointerException e) {
            System.out.println("Correctly throws NPE for null key");
        }

        // Test putDescriptionFile with null filePath
        try {
            resolution.putDescriptionFile("SomeModel", null);
            fail("putDescriptionFile should throw NullPointerException for null filePath");
        } catch (NullPointerException e) {
            System.out.println("Correctly throws NPE for null filePath");
        }

        // Test getDescriptionFile with null key
        try {
            resolution.getDescriptionFile(null);
            fail("getDescriptionFile should throw NullPointerException for null key");
        } catch (NullPointerException e) {
            System.out.println("Correctly throws NPE for null key in get");
        }

        // Test hasDescriptionFile with null key
        try {
            resolution.hasDescriptionFile(null);
            fail("hasDescriptionFile should throw NullPointerException for null key");
        } catch (NullPointerException e) {
            System.out.println("Correctly throws NPE for null key in hasDescriptionFile");
        }

        System.out.println("All null checks work correctly");
    }
}
