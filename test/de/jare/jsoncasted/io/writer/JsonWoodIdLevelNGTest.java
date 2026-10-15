/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

import de.jare.debug.JsonDebugLevel;
import de.jare.impltest.lib.SharedBox;
import de.jare.impltest.lib.SharedNode;
import de.jare.impltest.lib.SharedRefBox;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.JsonObjectWriter;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.JsonWoodIdLevel;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * The wood id level of the item definition steers the writer, analog to the casting level: NEVER ignores the collection
 * sharing policies and keeps object identities in the member form, NECESSARY (the default) writes ids only where
 * necessary, PREFIXED moves the object identities into the angle bracket notation - definitions carry the id prefix,
 * references become the bare id.
 *
 * @author Janusch Rentenatus
 */
public class JsonWoodIdLevelNGTest {

    /**
     * NEVER ignores the sharing policies: the same list instance at two fields is written inline twice and the round
     * trip yields two independent lists.
     */
    @Test
    public void testNeverDisablesListSharing() throws Exception {
        final SharedBox box = new SharedBox();
        final java.util.List<String> shared = new java.util.ArrayList<>(java.util.Arrays.asList("a", "b"));
        box.setLeft(shared);
        box.setRight(shared);

        final String json = writeBox(box, JsonWoodIdLevel.NEVER);
        assertFalse(json.contains("<\""), "NEVER must not write the wood id notation: " + json);

        final JsonObject answer = parseBox(json);
        assertNotSame(answer.getParam("left"), answer.getParam("right"),
                "NEVER must leave the lists independent");
    }

    /**
     * NEVER leaves the object identity mechanism untouched: a node referenced by two reference fields keeps the
     * _woodObjectId member form on the definition, the strict JSON fallback the links rely on.
     */
    @Test
    public void testNeverKeepsObjectMemberForm() throws Exception {
        final SharedNode node = new SharedNode("core");
        final SharedRefBox box = boxOf(node);
        final String json = writeRef(box, JsonWoodIdLevel.NEVER);

        assertTrue(json.contains("_woodObjectId"), "NEVER must keep the member form: " + json);
        assertFalse(json.contains("<\""), "NEVER must not write the prefix notation: " + json);

        final SharedRefBox answer = buildRef(json);
        assertSame(answer.getLeft(), answer.getRight(), "the round trip must restore one shared node");
        assertEquals(answer.getLeft().getText(), "core");
    }

    /**
     * NECESSARY, the default, keeps the member form for object identities and the angle bracket for lists.
     */
    @Test
    public void testNecessaryWritesMemberForm() throws Exception {
        final SharedNode node = new SharedNode("core");
        final SharedRefBox box = boxOf(node);
        final String json = writeRef(box, JsonWoodIdLevel.NECESSARY);

        assertTrue(json.contains("_woodObjectId"), "NECESSARY must keep the member form: " + json);

        final SharedRefBox answer = buildRef(json);
        assertSame(answer.getLeft(), answer.getRight());
        assertEquals(answer.getLeft().getText(), "core");
    }

    /**
     * PREFIXED moves the object identities into the angle bracket notation: the definition carries the id prefix in
     * front of the object, the references become the bare id - no _woodObjectId member and no _woodLink stub anywhere.
     */
    @Test
    public void testPrefixedWritesAngleNotation() throws Exception {
        final SharedNode node = new SharedNode("core");
        final SharedRefBox box = boxOf(node);
        final String json = writeRef(box, JsonWoodIdLevel.PREFIXED);

        assertFalse(json.contains("_woodObjectId"), "PREFIXED must leave the member form out: " + json);
        assertFalse(json.contains("_woodLink"), "PREFIXED must leave the link stub out: " + json);
        assertTrue(json.matches("(?s).*<\"\\d+\">\\s*\\{.*"), "the definition must carry the id prefix: " + json);
        assertTrue(json.matches("(?s).*:\\s*<\"\\d+\">.*"), "the references must be bare ids: " + json);

        final SharedRefBox answer = buildRef(json);
        assertSame(answer.getLeft(), answer.getRight(), "the round trip must restore one shared node");
        assertEquals(answer.getLeft().getText(), "core");
    }

    /**
     * PREFIXED changes nothing for lists: they carry the angle bracket anyway, exactly as with NECESSARY.
     */
    @Test
    public void testPrefixedKeepsListSharing() throws Exception {
        final SharedBox box = new SharedBox();
        final java.util.List<String> shared = new java.util.ArrayList<>(java.util.Arrays.asList("a", "b"));
        box.setLeft(shared);
        box.setRight(shared);

        final String json = writeBox(box, JsonWoodIdLevel.PREFIXED);
        assertTrue(json.matches("(?s).*<\"\\d+\">\\s*\\[.*"), "the list definition must carry the id prefix: " + json);

        final JsonObject answer = parseBox(json);
        assertSame(answer.getParam("left"), answer.getParam("right"));
    }

    private SharedRefBox boxOf(SharedNode node) {
        final SharedRefBox box = new SharedRefBox();
        box.setLeft(node);
        box.setRight(node);
        return box;
    }

    private String writeBox(SharedBox box, JsonWoodIdLevel level) throws Exception {
        final JsonModel model = boxModel();
        final JsonItemDefinition definition = definitionOf(model, level);
        return JsonObjectWriter.writeToString(box, definition, definition.getRootClass());
    }

    private String writeRef(SharedRefBox box, JsonWoodIdLevel level) throws Exception {
        final JsonModel model = refModel();
        final JsonItemDefinition definition = definitionOf(model, level);
        return JsonObjectWriter.writeToString(box, definition, definition.getRootClass());
    }

    private JsonItemDefinition definitionOf(final JsonModel model, final JsonWoodIdLevel level) {
        return new JsonItemDefinition() {
            @Override
            public JsonModel getModel() {
                return model;
            }

            @Override
            public JsonCastingLevel getCastingLevel() {
                return JsonCastingLevel.NECESSARY_CLASS_DEF;
            }

        };
    }

    private JsonObject parseBox(String json) throws Exception {
        final WoodResolution resolution = JsonParser.parse(json, boxModel().getOrCreateDescriptor(),
                boxModel().getRootNodeCast(), JsonDebugLevel.SIMPLE);
        assertNotNull(resolution.getAnswer());
        return (JsonObject) resolution.getAnswer();
    }

    private SharedRefBox buildRef(String json) throws Exception {
        final JsonModel model = refModel();
        final WoodResolution resolution = JsonParser.parse(json, model.getOrCreateDescriptor(),
                model.getRootNodeCast(), JsonDebugLevel.SIMPLE);
        assertNotNull(resolution.getAnswer());
        final Object build = JsonBuilder.buildInstance(model, false, resolution.getAnswer());
        assertTrue(build instanceof SharedRefBox, "the build must yield the ref box");
        return (SharedRefBox) build;
    }

    private JsonModel boxModel() {
        final JsonModel model = new JsonModel("woodLevelBox");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonClass holder = model.newJsonReflect(SharedBox.class);
        holder.addField("left", asString, LIST).withSameCollection(true);
        holder.addField("right", asString, LIST).withSameCollection(true);
        model.setRootNodeCast(holder.getcName());
        return model;
    }

    private JsonModel refModel() {
        final JsonModel model = new JsonModel("woodLevelRef");
        model.addBasicModel();
        final JsonClass asString = model.getJsonClass("String");
        final JsonClass node = model.newJsonReflect(SharedNode.class);
        node.addField("text", asString);
        final JsonClass holder = model.newJsonReflect(SharedRefBox.class);
        holder.addField("left", node).makeAsReference();
        holder.addField("right", node).makeAsReference();
        model.setRootNodeCast(holder.getcName());
        return model;
    }

}
