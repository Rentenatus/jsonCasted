/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

import de.jare.debug.JsonDebugLevel;
import de.jare.impltest.lib.SharedBox;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.JsonObjectWriter;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Round trip of the collection sharing policy (withSameCollection, the default): a list instance that hangs at two
 * fields is written once as a wood identified definition ({@code <"id">[...]}) and once as a bare id reference
 * ({@code <"id">}), and the parser restores the sharing. Plain lists stay plain, an empty shared list survives
 * skippingEmpty.
 *
 * @author Janusch Rentenatus
 */
public class SharedListWriterNGTest {

    /**
     * A shared list instance becomes a definition with id prefix plus a bare reference; parsing restores one shared
     * instance.
     */
    @Test
    public void testSharedListRoundTrip() throws Exception {
        final SharedBox box = new SharedBox();
        final java.util.List<String> shared = new java.util.ArrayList<>(java.util.Arrays.asList("a", "b"));
        box.setLeft(shared);
        box.setRight(shared);

        final String json = write(box);
        assertTrue(json.contains("<\""), "the wood id notation must appear: " + json);
        assertTrue(json.matches("(?s).*<\"\\d+\">\\s*\\[.*"), "the definition must carry the id prefix: " + json);

        final JsonObject answer = parseAnswer(json);
        assertSame(answer.getParam("left"), answer.getParam("right"),
                "the round trip must restore one shared list instance");
        assertEquals(((JsonList) answer.getParam("left")).listSize(), 2);
    }

    /**
     * An empty shared list is written despite skippingEmpty, so the stub does not hang in the air.
     */
    @Test
    public void testEmptySharedListSurvivesSkippingEmpty() throws Exception {
        final SharedBox box = new SharedBox();
        final java.util.List<String> shared = new java.util.ArrayList<>();
        box.setLeft(shared);
        box.setRight(shared);

        final String json = write(box);
        assertTrue(json.contains("<\""), "an empty shared list must be written: " + json);

        final JsonObject answer = parseAnswer(json);
        assertSame(answer.getParam("left"), answer.getParam("right"));
        assertEquals(((JsonList) answer.getParam("left")).listSize(), 0);
    }

    /**
     * A list that hangs at only one field stays a plain array without any id.
     */
    @Test
    public void testSingleListStaysPlain() throws Exception {
        final SharedBox box = new SharedBox();
        box.setLeft(new java.util.ArrayList<>(java.util.Arrays.asList("a")));
        box.setRight(new java.util.ArrayList<>(java.util.Arrays.asList("a")));

        final String json = write(box);
        assertFalse(json.contains("<\""), "unshared lists must stay plain: " + json);

        final JsonObject answer = parseAnswer(json);
        assertNotSame(answer.getParam("left"), answer.getParam("right"));
    }

    /**
     * Opting out: withSameCollection(false) keeps two references to the same instance as two plain lists.
     */
    @Test
    public void testOptOutKeepsPlainLists() throws Exception {
        final SharedBox box = new SharedBox();
        final java.util.List<String> shared = new java.util.ArrayList<>(java.util.Arrays.asList("a"));
        box.setLeft(shared);
        box.setRight(shared);

        final String json = write(box, false);
        assertFalse(json.contains("<\""), "the opt-out must keep plain arrays: " + json);
    }

    private String write(SharedBox box) throws Exception {
        return write(box, true);
    }

    private String write(SharedBox box, boolean sameCollection) throws Exception {
        final JsonModel model = model(sameCollection);
        final JsonItemDefinition definition = new JsonItemDefinition() {
            @Override
            public JsonModel getModel() {
                return model;
            }

            @Override
            public JsonCastingLevel getCastingLevel() {
                return JsonCastingLevel.NECESSARY_CLASS_DEF;
            }
        };
        return JsonObjectWriter.writeToString(box, definition, definition.getRootClass());
    }

    private JsonObject parseAnswer(String json) throws Exception {
        final JsonModel model = model(true);
        final WoodResolution resolution = JsonParser.parse(json, model.getOrCreateDescriptor(),
                model.getRootNodeCast(), JsonDebugLevel.SIMPLE);
        assertNotNull(resolution.getAnswer());
        return (JsonObject) resolution.getAnswer();
    }

    private JsonModel model(boolean sameCollection) {
        final JsonModel model = new JsonModel("woodShareWrite");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonClass holder = model.newJsonReflect(SharedBox.class);
        holder.addField("left", asString, LIST).withSameCollection(sameCollection);
        holder.addField("right", asString, LIST).withSameCollection(sameCollection);
        model.setRootNodeCast(holder.getcName());
        return model;
    }

}
