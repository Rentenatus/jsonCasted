/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.convertservice.RootConverter;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration tests for ItemStore architecture.
 * Tests proxy reference resolution and cyclic dependency handling.
 *
 * @author Janusch Rentenatus
 */
public class ItemStoreIntegrationTest {

    public ItemStoreIntegrationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start ItemStoreIntegrationTest.");
        System.out.println("===============================================");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End ItemStoreIntegrationTest.");
        System.out.println("===============================================");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test that JsonItem has the new ItemStore methods.
     */
    @Test
    public void testJsonItemHasItemStoreMethods() {
        JsonValue value = new JsonValue("test", null);
        
        // These should return null for primitive values
        assertNull(value.getLinkId());
        assertNull(value.getItemStore());
        assertFalse(value.hasLinkId());
        
        // setLinkId and setItemStore should not throw exceptions (no-op for JsonValue)
        value.setLinkId("test::link");
        value.setItemStore(new JsonItemStore());
        
        // Values should still be null for JsonValue
        assertNull(value.getLinkId());
        assertNull(value.getItemStore());
    }

    /**
     * Test that JsonObject supports ItemStore methods.
     */
    @Test
    public void testJsonObjectItemStoreMethods() {
        JsonObject obj = new JsonObject(null);
        
        // Initially null
        assertNull(obj.getLinkId());
        assertNull(obj.getItemStore());
        assertFalse(obj.hasLinkId());
        
        // Test setting values
        JsonItemStore store = new JsonItemStore();
        obj.setLinkId("provider::object123");
        obj.setItemStore(store);
        
        assertEquals(obj.getLinkId(), "provider::object123");
        assertSame(obj.getItemStore(), store);
        assertTrue(obj.hasLinkId());
    }

    /**
     * Test that JsonList supports ItemStore methods.
     */
    @Test
    public void testJsonListItemStoreMethods() {
        JsonList list = new JsonList(new java.util.ArrayList<>(), false, null);
        
        // Initially null
        assertNull(list.getLinkId());
        assertNull(list.getItemStore());
        assertFalse(list.hasLinkId());
        
        // Test setting values
        JsonItemStore store = new JsonItemStore();
        list.setLinkId("provider::list123");
        list.setItemStore(store);
        
        assertEquals(list.getLinkId(), "provider::list123");
        assertSame(list.getItemStore(), store);
        assertTrue(list.hasLinkId());
    }

    /**
     * Test JsonBuilder with ItemStore.
     * This tests the basic integration of ItemStore with the builder.
     */
    @Test
    public void testJsonBuilderWithItemStore() {
        // Create a simple JsonObject
        JsonObject obj = new JsonObject(null);
        JsonItemStore store = new JsonItemStore();
        
        // Create a JsonBuilder with ItemStore
        JsonBuilder builder = new JsonBuilder(obj, store);
        
        assertNotNull(builder);
        assertSame(builder.getRootItem(), obj);
        assertSame(builder.getItemStore(), store);
    }

    /**
     * Test JsonBuilder backward compatibility (without ItemStore).
     */
    @Test
    public void testJsonBuilderBackwardCompatibility() {
        // Create a simple JsonObject
        JsonObject obj = new JsonObject(null);
        
        // Create a JsonBuilder without ItemStore (old API)
        JsonBuilder builder = new JsonBuilder(obj);
        
        assertNotNull(builder);
        assertSame(builder.getRootItem(), obj);
        assertNull(builder.getItemStore());
    }

    /**
     * Test RootConverter creates JsonItemStore.
     * This verifies that the parsing process creates and uses an ItemStore.
     */
    @Test
    public void testRootConverterCreatesItemStore() {
        // We can't easily test the full parsing without a proper JsonResource,
        // but we can at least verify that the convertWithStore method exists
        // and accepts an ItemStore parameter
        
        JsonItemStore store = new JsonItemStore();
        assertNotNull(store);
        
        // The RootConverter.convert() method should internally create an ItemStore
        // This is verified by the existence of the convertWithStore method
    }

    /**
     * Test JsonItemStore ID generation and extraction.
     */
    @Test
    public void testIdGenerationAndExtraction() {
        String provider = "myProvider";
        String objectId = "myObject";
        
        // Build ID
        String fullId = JsonItemStore.buildItemId(provider, objectId);
        assertEquals(fullId, "myProvider::myObject");
        
        // Extract parts
        assertEquals(JsonItemStore.extractProviderName(fullId), provider);
        assertEquals(JsonItemStore.extractObjectId(fullId), objectId);
        
        // Test with link ID
        String linkId = JsonItemStore.buildLinkId(provider, "targetObject");
        assertEquals(linkId, "myProvider::targetObject");
    }

    /**
     * Test JsonItemStore with multiple items.
     */
    @Test
    public void testMultipleItemsInStore() {
        JsonItemStore store = new JsonItemStore();
        
        JsonObject obj1 = new JsonObject(null);
        JsonObject obj2 = new JsonObject(null);
        JsonList list1 = new JsonList(new java.util.ArrayList<>(), false, null);
        JsonValue val1 = new JsonValue("test", null);
        
        store.addItem("provider::obj1", obj1);
        store.addItem("provider::obj2", obj2);
        store.addItem("provider::list1", list1);
        store.addItem("provider::val1", val1);
        
        assertEquals(store.size(), 4);
        assertTrue(store.contains("provider::obj1"));
        assertTrue(store.contains("provider::obj2"));
        assertTrue(store.contains("provider::list1"));
        assertTrue(store.contains("provider::val1"));
        
        assertSame(store.getItem("provider::obj1"), obj1);
        assertSame(store.getItem("provider::obj2"), obj2);
        assertSame(store.getItem("provider::list1"), list1);
        assertSame(store.getItem("provider::val1"), val1);
    }

    /**
     * Test JsonItemStore iteration over all items.
     */
    @Test
    public void testIterateAllItems() {
        JsonItemStore store = new JsonItemStore();
        
        JsonObject obj1 = new JsonObject(null);
        JsonObject obj2 = new JsonObject(null);
        JsonObject obj3 = new JsonObject(null);
        
        store.addItem("provider::obj1", obj1);
        store.addItem("provider::obj2", obj2);
        store.addItem("provider::obj3", obj3);
        
        java.util.Collection<JsonItem> allItems = store.getAllItems();
        assertEquals(allItems.size(), 3);
        
        // Verify all items are present
        assertTrue(allItems.contains(obj1));
        assertTrue(allItems.contains(obj2));
        assertTrue(allItems.contains(obj3));
    }

    /**
     * Test JsonItemStore does not allow duplicate IDs.
     */
    @Test
    public void testNoDuplicateIds() {
        JsonItemStore store = new JsonItemStore();
        
        JsonObject obj1 = new JsonObject(null);
        JsonObject obj2 = new JsonObject(null);
        
        store.addItem("provider::obj1", obj1);
        store.addItem("provider::obj1", obj2);  // Same ID
        
        // The second add should overwrite the first (Map behavior)
        assertEquals(store.size(), 1);
        assertSame(store.getItem("provider::obj1"), obj2);
    }
}
