/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonMap;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests for ObjectCircleScannerWalker and JsonMap attribute access: cycles that run through the values of a JSON
 * map must be detected, and the map attribute lookup must work with and without debug level.
 *
 * @author Janusch Rentenatus
 */
public class ObjectCircleScannerWalkerNGTest {

    /**
     * Bean whose children are held in a JSON map, so cycles can be built through map values.
     */
    public static class Node {

        private JsonInstance<Node> childs = new JsonInstance<>();

        public JsonInstance<Node> getChilds() {
            return childs;
        }

        public void setChilds(JsonInstance<Node> childs) {
            this.childs = childs;
        }
    }

    /**
     * A cycle whose last edges run through the values of a reference-typed map field (a -&gt; map -&gt; b -&gt; map
     * -&gt; a) must be detected by the cycle scanner, like the describedTypes map of a model descriptor. Before the
     * map branch existed, the scanner returned null for map entries and never descended into map values.
     */
    @Test
    public void testCycleThroughMapValuesIsDetected() {
        final JsonModel model = new JsonModel("CycleTest");
        model.addBasicModel();
        final JsonClass node = model.newJsonReflect(Node.class);
        final JsonMap map = model.newRawJsonMapIndividually(JsonInstance.class, null, node);
        node.addField("childs", map).makeAsReference();

        final Node a = new Node();
        final Node b = new Node();
        a.getChilds().put("b", b);
        b.getChilds().put("a", a);

        final ObjectCircleScannerWalker scanner = new ObjectCircleScannerWalker(
                new DefinitionsContext(model, null), JsonCastingLevel.NEVER);
        scanner.scan(a, node);

        assertFalse(scanner.getFindings().isEmpty(), "Cycle through map values must be detected");
    }

    /**
     * The debug level variant of JsonMap.getAttr must return the map value like the two-argument variant, not fall
     * through to the reflective getter lookup of JsonClass.
     */
    @Test
    public void testJsonMapGetAttrWithDebugLevelReturnsMapValue() {
        final JsonModel model = new JsonModel("MapAttrTest");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonMap map = model.newRawJsonMapIndividually(JsonInstance.class, null, asString);

        final JsonInstance<String> inst = new JsonInstance<>();
        inst.put("key", "value");
        final JsonField field = map.getField("key");

        assertEquals(map.getAttr(field, inst), "value");
        assertEquals(map.getAttr(field, inst, JsonDebugLevel.SIMPLE), "value");
    }
}
