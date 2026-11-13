/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.debug.JsonDebugLevel;

import de.jare.jsoncasted.io.JsonParser;

import de.jare.jsoncasted.io.convertservice.WoodResolution;

import de.jare.jsoncasted.item.JsonItem;

import de.jare.jsoncasted.item.JsonList;

import de.jare.jsoncasted.item.JsonObject;

import de.jare.jsoncasted.item.builder.JsonBuilder;

import de.jare.jsoncasted.lang.JsonNode;

import de.jare.jsoncasted.lang.JsonResource;

import de.jare.jsoncasted.lang.LinkingSet;

import de.jare.jsonconfig.def.JsonConfigDefinition;

import de.jare.jsonconfig.item.ConfigFeature;

import de.jare.jsonconfig.item.ConfigProfile;

import de.jare.jsonconfig.item.ConfigRoot;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

/**
 * Diamond notation on arrays only (no object identities): {@code <"1">[...]} defines a String array, every bare
 * {@code <this::1>} (quoted or not) references that definition. The model is {@link JsonConfigDefinition}, whose
 * String array fields ({@code comments} on root/profile/feature) carry the definitions and references. Asserts the
 * linking set, the shared JsonList instance at item level and the built bean contents.
 *
 * @author Janusch Rentenatus
 */
public class DiamondTestNG {

    /**
     * A definition on the root comments array referenced by profile and feature comments: all three fields share one
     * JsonList instance and build the same String array contents.
     */
    @Test
    public void testArrayDefinitionSharedAcrossLevels() throws Exception {

        final String json = "{ \"comments\": <\"1\">[\"root a\", \"root b\"], \"profiles\": [ { \"profile\": \"main\","
                + " \"comments\": <this::1>, \"features\": [ { \"feature\": \"ollama\", \"comments\": <\"this::1\"> } ] } ] }";

        final JsonResource res = parseResource(json);
        final LinkingSet links = res.getLinkingSet();
        assertTrue(links.getObjectIdMap().containsKey("self::1"), "the definition must register as self::1");
        assertTrue(links.getLinkMap().containsKey("self::1"), "the stubs must normalize to self::1");
        assertFalse(links.hasUnconnectedLinks(), "no dangling links expected");

        final JsonObject answer = parseAnswer(json);
        final JsonItem rootComments = answer.getParam("comments");
        final JsonObject profile = (JsonObject) ((JsonList) answer.getParam("profiles")).listIterator().next();
        final JsonItem profileComments = profile.getParam("comments");
        final JsonObject feature = (JsonObject) ((JsonList) profile.getParam("features")).listIterator().next();
        final JsonItem featureComments = feature.getParam("comments");
        assertTrue(rootComments instanceof JsonList);
        assertSame(profileComments, rootComments, "profile comments must reference the defined array");
        assertSame(featureComments, rootComments, "feature comments must reference the defined array");

        final ConfigRoot root = buildRoot(json);
        assertEquals(root.getComments(), new String[]{"root a", "root b"});
        final ConfigProfile prof = root.getProfiles().get(0);
        assertEquals(prof.getComments(), new String[]{"root a", "root b"});
        final ConfigFeature feat = prof.getFeatures().get(0);
        assertEquals(feat.getComments(), new String[]{"root a", "root b"});
    }

    /**
     * Unquoted diamond contents are the tolerant fallback: {@code <1>[...] } defines, {@code <this::1>} references.
     */
    @Test
    public void testUnquotedDefinitionAndReference() throws Exception {

        final String json = "{ \"comments\": <1>[\"x\"], \"profiles\": [ { \"profile\": \"p\", \"comments\": <this::1> } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertTrue(links.getObjectIdMap().containsKey("self::1"));
        assertTrue(links.getLinkMap().containsKey("self::1"));
        assertFalse(links.hasUnconnectedLinks());

        final JsonObject answer = parseAnswer(json);
        assertSame(answer.getParam("profiles") instanceof JsonList ? profileComments(answer) : null,
                answer.getParam("comments"), "unquoted stub must share the unquoted definition");

        final ConfigRoot root = buildRoot(json);
        assertEquals(root.getComments(), new String[]{"x"});
        assertEquals(root.getProfiles().get(0).getComments(), new String[]{"x"});
    }

    /**
     * A stub that appears before its definition in document order still resolves to the same shared list.
     */
    @Test
    public void testReferenceBeforeDefinition() throws Exception {

        final String json = "{ \"profiles\": [ { \"profile\": \"p\", \"comments\": <this::9> } ],"
                + " \"comments\": <\"9\">[\"late\"] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertFalse(links.hasUnconnectedLinks(), "order must not matter: " + links.getUnconnectedSet());

        final JsonObject answer = parseAnswer(json);
        assertSame(profileComments(answer), answer.getParam("comments"),
                "the stub before the definition must still share the instance");

        final ConfigRoot root = buildRoot(json);
        assertEquals(root.getComments(), new String[]{"late"});
        assertEquals(root.getProfiles().get(0).getComments(), new String[]{"late"});
    }

    /**
     * An empty array definition is a valid identity carrier: {@code <"5">[]} plus a {@code <this::5>} stub share the
     * same empty list.
     */
    @Test
    public void testEmptyArrayDefinitionShared() throws Exception {

        final String json = "{ \"comments\": <\"5\">[], \"profiles\": [ { \"profile\": \"p\", \"comments\": <this::5> } ] }";
        final JsonObject answer = parseAnswer(json);
        final JsonItem shared = answer.getParam("comments");
        assertTrue(shared instanceof JsonList);
        assertSame(profileComments(answer), shared);
        assertEquals(((JsonList) shared).listSize(), 0);

        final ConfigRoot root = buildRoot(json);
        assertNotNull(root.getComments());
        assertEquals(root.getComments().length, 0);
        assertEquals(root.getProfiles().get(0).getComments().length, 0);
    }

    /**
     * A cross resource reference {@code <"save::1">} keeps its provider key and stays dangling when only a local
     * array is defined.
     */
    @Test
    public void testCrossResourceReferenceStaysDangling() throws Exception {

        final String json = "{ \"comments\": <\"1\">[\"local\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": <\"save::1\"> } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertTrue(links.getObjectIdMap().containsKey("self::1"));
        assertTrue(links.getLinkMap().containsKey("save::1"), "the cross resource key must be kept as is");
        assertTrue(links.getUnconnectedSet().contains("save::1"), "save::1 has no local definition: dangling");
        assertFalse(links.getUnconnectedSet().contains("self::1"));
    }

    /**
     * A definition must not carry a provider: {@code <this::1>[...] } registers a scan exception and no object id.
     */
    @Test
    public void testDefinitionWithProviderPrefixIsRejected() throws Exception {

        final LinkingSet links = parseResource("{ \"comments\": <this::1>[\"x\"] }").getLinkingSet();
        assertFalse(links.getScanExceptions().isEmpty(), "a :: in a definition id must become a scan exception");
        assertTrue(links.getObjectIdMap().isEmpty(), "no identity may be registered for a prefixed definition id");
    }

    /**
     * A duplicated array definition warns and keeps the first occurrence.
     */
    @Test
    public void testDuplicateArrayDefinitionWarnsFirstWins() throws Exception {

        final String json = "{ \"comments\": <\"3\">[\"first\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": <\"3\">[\"second\"] } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertTrue(links.hasWarnings(), "a duplicate array id must warn");
        assertTrue(links.getWarnings().get(0).contains("self::3"));
        assertEquals(links.getObjectIdMap().size(), 1);

        final JsonObject answer = parseAnswer(json);
        final JsonItem shared = answer.getParam("comments");
        assertEquals(((JsonList) shared).listSize(), 1);
        assertEquals(shared.listIterator().next().getStringValue(), "first");
        assertSame(profileComments(answer), shared, "the duplicate definition resolves to the first list");
    }

    /**
     * The hardest order case: the references stand at the very start of the document (profile and feature comments),
     * the array definition is the last member of the root. The resolver loop must still find the array definition and
     * share one instance across all three fields - with the expected size, never null.
     */
    @Test
    public void testReferenceAtStartDefinitionAtEnd() throws Exception {

        final String json = "{ \"profiles\": [ { \"profile\": \"p\", \"comments\": <this::1>,"
                + " \"features\": [ { \"feature\": \"f\", \"comments\": <\"this::1\"> } ] } ],"
                + " \"comments\": <\"1\">[\"a\", \"b\", \"c\"] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertTrue(links.getObjectIdMap().containsKey("self::1"));
        assertFalse(links.hasUnconnectedLinks(), "the late definition must still resolve: " + links.getUnconnectedSet());

        final JsonObject answer = parseAnswer(json);
        final JsonItem rootComments = answer.getParam("comments");
        assertListSize(rootComments, 3);
        final JsonObject profile = (JsonObject) ((JsonList) answer.getParam("profiles")).listIterator().next();
        assertSame(profile.getParam("comments"), rootComments, "early stub must share the late definition");
        final JsonObject feature = (JsonObject) ((JsonList) profile.getParam("features")).listIterator().next();
        assertSame(feature.getParam("comments"), rootComments);

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 3);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 3);
        assertCommentsSize(root.getProfiles().get(0).getFeatures().get(0).getComments(), 3);
    }

    /**
     * Interleaved definitions and references of two array ids: profile "a" references {@code <this::2>} before profile
     * "b" defines it - each reference must land on its own array with its own size.
     */
    @Test
    public void testInterleavedDefinitionsAndReferences() throws Exception {

        final String json = "{ \"comments\": <\"1\">[\"root\"], \"profiles\": ["
                + " { \"profile\": \"a\", \"comments\": <this::2> },"
                + " { \"profile\": \"b\", \"comments\": <\"2\">[\"x\", \"y\"] } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertEquals(links.getObjectIdMap().size(), 2);
        assertFalse(links.hasUnconnectedLinks());

        final JsonObject answer = parseAnswer(json);
        final JsonList profiles = (JsonList) answer.getParam("profiles");
        final JsonObject profA = (JsonObject) profiles.listIterator().next();
        final JsonItem commentsA = profA.getParam("comments");
        assertListSize(commentsA, 2);
        final JsonObject profB = (JsonObject) profiles.listIterator().next();
        assertSame(commentsA, profB.getParam("comments"), "the early <this::2> must share the later definition");
        assertNotSame(answer.getParam("comments"), commentsA, "different ids must stay different lists");
        assertListSize(answer.getParam("comments"), 1);

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 1);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 2);
        assertCommentsSize(root.getProfiles().get(1).getComments(), 2);
    }

    /**
     * A dangling reference with no definition anywhere degrades to an empty list - never null, and the linking set
     * reports the key as unconnected.
     */
    @Test
    public void testDanglingReferenceYieldsEmptyListNotNull() throws Exception {

        final String json = "{ \"comments\": [\"local\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": <this::99> } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertTrue(links.getUnconnectedSet().contains("self::99"), "self::99 must be reported dangling");
        assertTrue(links.getObjectIdMap().isEmpty());

        final JsonObject answer = parseAnswer(json);
        final JsonItem dangling = profileComments(answer);
        assertNotNull(dangling, "a dangling reference must not produce null");
        assertTrue(dangling instanceof JsonList);
        assertListSize(dangling, 0);
        assertListSize(answer.getParam("comments"), 1);

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 1);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 0);
    }

    /**
     * Diamond identities on arrays at parse level, including a reference stub as a list element: the linking set
     * registers both array definitions and the stub, nothing dangles, and every array keeps its expected size.
     */
    @Test
    public void testNestedDiamondArraysAtParseLevel() throws Exception {

        final JsonResource res = parseResource("[ <\"2\">[\"x\", \"y\"], <this::2>, <\"3\">[\"z\"] ]");
        final JsonNode root = res.getRoot();
        assertTrue(root.isArray());
        assertEquals(root.asArray().size(), 3);

        final JsonNode def2 = root.asArray().get(0);
        assertTrue(def2.isArray());
        assertEquals(def2.getWoodObjectId(), "2");
        assertEquals(def2.asArray().size(), 2);

        final JsonNode stub = root.asArray().get(1);
        assertTrue(stub.isObject());
        assertEquals(stub.asObjectValues().get(de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK).asText(), "this::2");

        final JsonNode def3 = root.asArray().get(2);
        assertTrue(def3.isArray());
        assertEquals(def3.asArray().size(), 1);

        final LinkingSet links = res.getLinkingSet();
        assertTrue(links.getObjectIdMap().containsKey("self::2"));
        assertTrue(links.getObjectIdMap().containsKey("self::3"));
        assertTrue(links.getLinkMap().containsKey("self::2"));
        assertFalse(links.hasUnconnectedLinks());
    }

    private static void assertListSize(JsonItem item, int expected) {

        assertNotNull(item, "the list must not be null");
        assertTrue(item instanceof JsonList, "expected a JsonList but got: " + item);
        assertEquals(((JsonList) item).listSize(), expected, "unexpected list size");
    }

    private static void assertCommentsSize(String[] comments, int expected) {

        assertNotNull(comments, "the built comments array must not be null");
        assertEquals(comments.length, expected, "unexpected comments size");
    }

    private JsonResource parseResource(String json) throws Exception {

        return JsonParserService.parse(json, JsonDebugLevel.SIMPLE);
    }

    private JsonObject parseAnswer(String json) throws Exception {

        final JsonConfigDefinition definition = JsonConfigDefinition.getInstance();
        final JsonResource res = parseResource(json);
        final WoodResolution resolution = JsonParser.parse(res, definition.getDescriptor(),
                definition.getRootClass().getcName(), JsonDebugLevel.SIMPLE);
        return (JsonObject) resolution.getAnswer();
    }

    private ConfigRoot buildRoot(String json) throws Exception {

        final JsonConfigDefinition definition = JsonConfigDefinition.getInstance();
        final JsonResource res = parseResource(json);
        final WoodResolution resolution = JsonParser.parse(res, definition.getDescriptor(),
                definition.getRootClass().getcName(), JsonDebugLevel.SIMPLE);
        return (ConfigRoot) JsonBuilder.buildInstance(definition.getModel(), false, resolution.getAnswer());
    }

    private JsonItem profileComments(JsonObject answer) {

        final JsonObject profile = (JsonObject) ((JsonList) answer.getParam("profiles")).listIterator().next();
        return profile.getParam("comments");
    }

}
