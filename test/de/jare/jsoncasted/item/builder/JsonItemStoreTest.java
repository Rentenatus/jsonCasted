/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItemStore;
import de.jare.jsoncasted.item.JsonValue;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for JsonItemStore class.
 * Tests the central storage functionality for JsonItems during parsing and building.
 *
 * @author Janusch Rentenatus
 */
public class JsonItemStoreTest {

    public JsonItemStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.out.println("===============================================");
        System.out.println("## Start JsonItemStoreTest.");
        System.out.println("===============================================");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.out.println("## End JsonItemStoreTest.");
        System.out.println("===============================================");
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test creating an empty JsonItemStore.
     */
    @Test
    public void testEmptyStore() {
        JsonItemStore store = new JsonItemStore();
        assertNotNull(store);
        assertTrue(store.isEmpty());
        assertEquals(store.size(), 0);
        assertNull(store.getItem("nonexistent"));
        assertFalse(store.contains("nonexistent"));
    }

    /**
     * Test buildItemId method with valid inputs.
     */
    @Test
    public void testBuildItemId() {
        String itemId = JsonItemStore.buildItemId("provider", "object123");
        assertEquals(itemId, "provider::object123");
    }

    /**
     * Test buildItemId method with null inputs.
     */
    @Test
    public void testBuildItemIdNull() {
        assertNull(JsonItemStore.buildItemId(null, "object123"));
        assertNull(JsonItemStore.buildItemId("provider", null));
        assertNull(JsonItemStore.buildItemId(null, null));
    }

    /**
     * Test buildLinkId method.
     */
    @Test
    public void testBuildLinkId() {
        String linkId = JsonItemStore.buildLinkId("provider", "target123");
        assertEquals(linkId, "provider::target123");
    }

    /**
     * Test extractProviderName method.
     */
    @Test
    public void testExtractProviderName() {
        assertEquals(JsonItemStore.extractProviderName("provider::object123"), "provider");
        assertNull(JsonItemStore.extractProviderName(null));
        assertNull(JsonItemStore.extractProviderName("::object123"));  // Missing provider
        assertNull(JsonItemStore.extractProviderName("provider"));    // Missing separator
    }

    /**
     * Test extractObjectId method.
     */
    @Test
    public void testExtractObjectId() {
        assertEquals(JsonItemStore.extractObjectId("provider::object123"), "object123");
        assertNull(JsonItemStore.extractObjectId(null));
        assertNull(JsonItemStore.extractObjectId("provider::"));  // Missing object ID
        assertNull(JsonItemStore.extractObjectId("provider"));    // Missing separator
    }

    /**
     * Test adding and retrieving items from the store.
     */
    @Test
    public void testAddAndGetItem() {
        JsonItemStore store = new JsonItemStore();
        
        // Create a mock JsonItem (we use JsonValue as it's simple)
        JsonValue value = new JsonValue("test", null);
        
        store.addItem("provider::item1", value);
        
        assertFalse(store.isEmpty());
        assertEquals(store.size(), 1);
        assertTrue(store.contains("provider::item1"));
        assertSame(store.getItem("provider::item1"), value);
    }

    /**
     * Test adding null items (should be ignored).
     */
    @Test
    public void testAddNullItem() {
        JsonItemStore store = new JsonItemStore();
        
        store.addItem("provider::item1", null);
        store.addItem(null, new JsonValue("test", null));
        store.addItem(null, null);
        
        assertTrue(store.isEmpty());
        assertEquals(store.size(), 0);
    }

    /**
     * Test getting all items from the store.
     */
    @Test
    public void testGetAllItems() {
        JsonItemStore store = new JsonItemStore();
        
        JsonValue value1 = new JsonValue("test1", null);
        JsonValue value2 = new JsonValue("test2", null);
        JsonValue value3 = new JsonValue("test3", null);
        
        store.addItem("provider::item1", value1);
        store.addItem("provider::item2", value2);
        store.addItem("provider::item3", value3);
        
        assertEquals(store.size(), 3);
        assertEquals(store.getAllItems().size(), 3);
    }

    /**
     * Test toString method.
     */
    @Test
    public void testToString() {
        JsonItemStore store = new JsonItemStore();
        String toString = store.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("JsonItemStore"));
        assertTrue(toString.contains("items=0"));
        
        store.addItem("provider::item1", new JsonValue("test", null));
        toString = store.toString();
        assertTrue(toString.contains("items=1"));
    }

    /**
     * Test built objects cache.
     */
    @Test
    public void testBuiltObjectsCache() {
        JsonItemStore store = new JsonItemStore();
        
        Object obj1 = new Object();
        Object obj2 = new Object();
        
        // Test caching
        store.cacheBuiltObject("provider::item1", obj1);
        store.cacheBuiltObject("provider::item2", obj2);
        
        assertTrue(store.isObjectCached("provider::item1"));
        assertTrue(store.isObjectCached("provider::item2"));
        assertFalse(store.isObjectCached("nonexistent"));
        
        assertSame(store.getCachedObject("provider::item1"), obj1);
        assertSame(store.getCachedObject("provider::item2"), obj2);
        assertNull(store.getCachedObject("nonexistent"));
        
        // Test cache clear
        store.clearCache();
        assertFalse(store.isObjectCached("provider::item1"));
        assertNull(store.getCachedObject("provider::item1"));
    }

    /**
     * Test cache with null values (should be ignored).
     */
    @Test
    public void testCacheNullValues() {
        JsonItemStore store = new JsonItemStore();
        
        store.cacheBuiltObject(null, new Object());
        store.cacheBuiltObject("provider::item1", null);
        store.cacheBuiltObject(null, null);
        
        assertFalse(store.isObjectCached("provider::item1"));
        assertFalse(store.isObjectCached(null));
    }


}
