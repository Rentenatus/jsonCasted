/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.cycle;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.walker.ObjectCircleScannerWalker;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Cycle detection tests on a model with the object Harald, who knows other Haralds in a collection and in a map, both
 * passed through the constructor. Field cycles are allowed cycles; only cycles through constructor parameters are
 * forbidden, so the cycle cases must raise a write exception while the foreign-only case must stay clean.
 *
 * @author Janusch Rentenatus
 */
public class HaraldCycleNGTest {

    /**
     * Bean with a collection of Haralds and a map from names to Haralds, both constructor parameters.
     */
    public static class Harald {

        private final String name;
        private final java.util.List<Harald> friends;
        private final JsonInstance<Harald> namedFriends;

        public Harald(String name, java.util.List<Harald> friends, JsonInstance<Harald> namedFriends) {
            this.name = name;
            this.friends = friends != null ? friends : new java.util.ArrayList<>();
            this.namedFriends = namedFriends != null ? namedFriends : new JsonInstance<>();
        }

        public String getName() {
            return name;
        }

        public java.util.List<Harald> getFriends() {
            return friends;
        }

        public JsonInstance<Harald> getNamedFriends() {
            return namedFriends;
        }
    }

    /**
     * Harald knows only foreign Haralds: a directed acyclic graph with a shared reference (two paths reach the same
     * Harald). Neither a loop nor a diamond is a cycle - the scanner must report neither findings nor exceptions.
     */
    @Test
    public void testHaraldKnowsOnlyForeignHaralds() {
        final JsonModel model = buildHaraldModel();

        final Harald harald3 = new Harald("Harald-3", new java.util.ArrayList<>(), new JsonInstance<>());
        final Harald harald2 = new Harald("Harald-2", new java.util.ArrayList<>(), new JsonInstance<>());
        final Harald harald1 = new Harald("Harald-1",
                new java.util.ArrayList<>(java.util.List.of(harald2)), new JsonInstance<>());
        harald1.getNamedFriends().put("three", harald3);
        harald3.getFriends().add(harald2);

        final ObjectCircleScannerWalker scanner = scan(model, harald1);

        assertTrue(scanner.getFindings().isEmpty(),
                "Foreign Haralds must not be reported as cycle, but found: " + scanner.getFindings());
        assertTrue(scanner.getExceptions().isEmpty(),
                "Foreign Haralds must not raise a write exception: " + scanner.getExceptions());
    }

    /**
     * Harald appears in his own collection: harald1 -&gt; friends (constructor parameter) -&gt; harald1 is a forbidden
     * cycle via constructor parameters and must raise a write exception.
     */
    @Test
    public void testHaraldItselfInCollection() {
        final JsonModel model = buildHaraldModel();

        final Harald harald2 = new Harald("Harald-2", new java.util.ArrayList<>(), new JsonInstance<>());
        final Harald harald1 = new Harald("Harald-1",
                new java.util.ArrayList<>(java.util.List.of(harald2)), new JsonInstance<>());
        harald1.getFriends().add(harald1);

        final ObjectCircleScannerWalker scanner = scan(model, harald1);

        assertFalse(scanner.getExceptions().isEmpty(),
                "Harald in his own collection must raise a forbidden constructor cycle");
        assertTrue(scanner.getExceptions().iterator().next().getMessage()
                .contains("Forbidden cycle via constructor parameters"),
                "The exception must name the forbidden constructor cycle");
    }

    /**
     * Harald appears in his own map: harald1 -&gt; namedFriends (constructor parameter) -&gt; harald1 is a forbidden
     * cycle via constructor parameters through a map value and must raise a write exception. Before the map branch
     * existed, the scanner never descended into map values and the cycle stayed invisible.
     */
    @Test
    public void testHaraldItselfInMap() {
        final JsonModel model = buildHaraldModel();

        final Harald harald2 = new Harald("Harald-2", new java.util.ArrayList<>(), new JsonInstance<>());
        final Harald harald1 = new Harald("Harald-1",
                new java.util.ArrayList<>(), new JsonInstance<>());
        harald1.getNamedFriends().put("two", harald2);
        harald1.getNamedFriends().put("self", harald1);

        final ObjectCircleScannerWalker scanner = scan(model, harald1);

        assertFalse(scanner.getExceptions().isEmpty(),
                "Harald in his own map must raise a forbidden constructor cycle");
        assertTrue(scanner.getExceptions().iterator().next().getMessage()
                .contains("Forbidden cycle via constructor parameters"),
                "The exception must name the forbidden constructor cycle");
    }

    /**
     * Mixed cycle: harald1 reaches harald2 through the collection and harald2 reaches harald1 back through the map,
     * both constructor parameters. Cycle detection must work across collection and map edges combined.
     */
    @Test
    public void testMixedCycleThroughCollectionAndMap() {
        final JsonModel model = buildHaraldModel();

        final Harald harald2 = new Harald("Harald-2", new java.util.ArrayList<>(), new JsonInstance<>());
        final Harald harald1 = new Harald("Harald-1",
                new java.util.ArrayList<>(java.util.List.of(harald2)), new JsonInstance<>());
        harald2.getNamedFriends().put("back", harald1);

        final ObjectCircleScannerWalker scanner = scan(model, harald1);

        assertFalse(scanner.getExceptions().isEmpty(),
                "Mixed cycle through collection and map must raise a forbidden constructor cycle");
    }

    /**
     * Builds the Harald model: the type Harald with name, a LIST of Haralds and a name-to-Harald map, all passed
     * through the constructor as reference-typed constructor parameters.
     *
     * @return the model with the Harald type registered
     */
    private JsonModel buildHaraldModel() {
        final JsonModel model = new JsonModel("HaraldModel");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonClass harald = model.newJsonReflect(Harald.class);
        final JsonMap map = model.newRawJsonMapIndividually(JsonInstance.class, null, harald);
        harald.addCParam("name", asString);
        harald.addCParam("friends", harald, JsonCollectionType.LIST).makeAsReference();
        harald.addCParam("namedFriends", map).makeAsReference();
        return model;
    }

    /**
     * Scans the given Harald as root of the object graph.
     *
     * @param model the Harald model
     * @param root the Harald to scan
     * @return the executed scanner
     */
    private ObjectCircleScannerWalker scan(JsonModel model, Harald root) {
        final JsonClass haraldClass = model.getJsonClass(Harald.class.getTypeName());
        assertNotNull(haraldClass, "Harald type must be registered in the model");
        final ObjectCircleScannerWalker scanner = new ObjectCircleScannerWalker(
                new DefinitionsContext(model, (String) null), JsonCastingLevel.NEVER);
        scanner.scan(root, haraldClass);
        return scanner;
    }
}
