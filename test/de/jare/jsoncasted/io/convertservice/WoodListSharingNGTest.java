/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.convertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.impltest.lib.SharedBox;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Live sharing of wood identified lists: two list fields with the same identity (definition and stub) must hold the
 * same JsonList instance, no matter whether the stub stands before or after the definition. Plain lists stay
 * independent, and a duplicated definition keeps the first occurrence.
 *
 * @author Janusch Rentenatus
 */
public class WoodListSharingNGTest {

    /**
     * A definition followed by a bare stub: both fields share one list instance.
     */
    @Test
    public void testDefinitionThenStubShare() throws Exception {
        final JsonObject answer = parseAnswer("{ \"left\": <\"7\">[\"a\", \"b\"], \"right\": <\"7\"> }");
        final JsonItem left = answer.getParam("left");
        final JsonItem right = answer.getParam("right");
        assertTrue(left instanceof JsonList);
        assertTrue(right instanceof JsonList);
        assertSame(left, right, "definition and stub must share the same list instance");
        assertEquals(((JsonList) left).listSize(), 2);
        assertEquals(left.listIterator().next().getStringValue(), "a");
    }

    /**
     * A bare stub BEFORE its definition: the resolution loop pre-converts the definition, so the stub still shares.
     */
    @Test
    public void testStubBeforeDefinitionShare() throws Exception {
        final JsonObject answer = parseAnswer("{ \"right\": <\"7\">, \"left\": <\"7\">[\"a\", \"b\"] }");
        final JsonItem left = answer.getParam("left");
        final JsonItem right = answer.getParam("right");
        assertSame(left, right, "the stub before the definition must still share the instance");
        assertEquals(((JsonList) left).listSize(), 2);
        assertEquals(left.listIterator().next().getStringValue(), "a");
    }

    /**
     * Plain lists without identity stay independent instances.
     */
    @Test
    public void testPlainListsStayIndependent() throws Exception {
        final JsonObject answer = parseAnswer("{ \"left\": [\"a\"], \"right\": [\"a\"] }");
        assertNotSame(answer.getParam("left"), answer.getParam("right"),
                "plain lists must not be merged");
        assertEquals(((JsonList) answer.getParam("left")).listSize(), 1);
    }

    /**
     * A duplicated definition keeps the first occurrence, mirroring the first-wins rule of the wood id finder.
     */
    @Test
    public void testDuplicateDefinitionKeepsFirst() throws Exception {
        final JsonObject answer = parseAnswer("{ \"left\": <\"7\">[\"a\"], \"right\": <\"7\">[\"b\"] }");
        final JsonItem left = answer.getParam("left");
        assertSame(left, answer.getParam("right"), "both definitions resolve to the shared first list");
        assertEquals(((JsonList) left).listSize(), 1);
        assertEquals(left.listIterator().next().getStringValue(), "a");
    }

    /**
     * The shared list builds into a plain Java bean without breaking on the cast-driven element conversion.
     */
    @Test
    public void testSharedListBuildsIntoBean() throws Exception {
        final JsonObject answer = parseAnswer("{ \"left\": <\"7\">[\"a\", \"b\"], \"right\": <\"7\"> }");
        final Object build = JsonBuilder.buildInstance(model(), false, answer);
        assertTrue(build instanceof SharedBox);
        final SharedBox box = (SharedBox) build;
        assertEquals(box.getLeft(), java.util.Arrays.asList("a", "b"));
        assertEquals(box.getRight(), java.util.Arrays.asList("a", "b"));
    }

    private JsonObject parseAnswer(String json) throws Exception {
        final WoodResolution resolution = JsonParser.parse(json, descriptor(), rootName(), JsonDebugLevel.SIMPLE);
        assertNotNull(resolution.getAnswer(), "the answer must exist");
        return (JsonObject) resolution.getAnswer();
    }

    private JsonModel model() {
        final JsonModel model = new JsonModel("woodShare");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonClass holder = model.newJsonReflect(SharedBox.class);
        holder.addField("left", asString, LIST);
        holder.addField("right", asString, LIST);
        model.setRootNodeCast(holder.getcName());
        return model;
    }

    private JsonModelDescriptor descriptor() {
        return model().getOrCreateDescriptor();
    }

    private String rootName() {
        return model().getJsonClass(model().getRootNodeCast()).getcName();
    }

}
