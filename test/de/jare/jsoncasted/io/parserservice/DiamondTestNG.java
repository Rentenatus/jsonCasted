/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.debug.JsonDebugLevel;

import de.jare.jsoncasted.io.JsonCastingLevel;

import de.jare.jsoncasted.io.JsonItemDefinition;

import de.jare.jsoncasted.io.JsonObjectWriter;

import de.jare.jsoncasted.io.JsonParser;

import de.jare.jsoncasted.io.JsonWoodIdLevel;

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

    /**
     * The reverse depth case: the reference sits at the root, the definition is buried at the deepest level (feature
     * comments, last member of the document). One shared instance, expected size everywhere.
     */
    @Test
    public void testDefinitionDeeperThanReference() throws Exception {

        final String json = "{ \"comments\": <this::7>, \"profiles\": [ { \"profile\": \"p\", \"features\": ["
                + " { \"feature\": \"f\", \"comments\": <\"7\">[\"deep1\", \"deep2\"] } ] } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertFalse(links.hasUnconnectedLinks(), "the deep definition must resolve the root stub");

        final JsonObject answer = parseAnswer(json);
        final JsonItem rootComments = answer.getParam("comments");
        assertListSize(rootComments, 2);
        final JsonObject profile = (JsonObject) ((JsonList) answer.getParam("profiles")).listIterator().next();
        final JsonObject feature = (JsonObject) ((JsonList) profile.getParam("features")).listIterator().next();
        assertSame(rootComments, feature.getParam("comments"),
                "the root stub must share the deeply nested definition");

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 2);
        assertCommentsSize(root.getProfiles().get(0).getFeatures().get(0).getComments(), 2);
    }

    /**
     * Quoted and unquoted diamond contents denote the same identity: {@code <1>[...] } defined, {@code <"1">}
     * referenced - one shared list.
     */
    @Test
    public void testQuotedAndUnquotedAreSameIdentity() throws Exception {

        final String json = "{ \"comments\": <1>[\"a\", \"b\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": <\"1\"> } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertEquals(links.getObjectIdMap().size(), 1, "quoted and unquoted must collapse to one identity");
        assertFalse(links.hasUnconnectedLinks());

        final JsonObject answer = parseAnswer(json);
        assertSame(profileComments(answer), answer.getParam("comments"));
        assertListSize(answer.getParam("comments"), 2);

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 2);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 2);
    }

    /**
     * Unquoted diamond contents tolerate whitespace: {@code < 1 >[...] } defines, {@code < this::1 >} references.
     */
    @Test
    public void testUnquotedWithWhitespace() throws Exception {

        final String json = "{ \"comments\": < 1 >[\"w\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": < this::1 > } ] }";
        final LinkingSet links = parseResource(json).getLinkingSet();
        assertTrue(links.getObjectIdMap().containsKey("self::1"), "whitespace must be trimmed: " + links.getObjectIdMap().keySet());
        assertFalse(links.hasUnconnectedLinks());

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 1);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 1);
    }

    /**
     * Many references, one definition: root plus two profiles and a feature all share the single defined array - every
     * field the same instance, every size as expected, never null.
     */
    @Test
    public void testManyReferencesOneDefinition() throws Exception {

        final String json = "{ \"comments\": <\"4\">[\"x\", \"y\", \"z\"], \"profiles\": ["
                + " { \"profile\": \"a\", \"comments\": <this::4>, \"features\": [ { \"feature\": \"f\", \"comments\": <\"this::4\"> } ] },"
                + " { \"profile\": \"b\", \"comments\": <this::4> } ] }";
        final JsonObject answer = parseAnswer(json);
        final JsonItem shared = answer.getParam("comments");
        assertListSize(shared, 3);
        final JsonList profiles = (JsonList) answer.getParam("profiles");
        final java.util.Iterator<JsonItem> it = profiles.listIterator();
        final JsonObject profA = (JsonObject) it.next();
        final JsonObject profB = (JsonObject) it.next();
        assertSame(profA.getParam("comments"), shared);
        assertSame(profB.getParam("comments"), shared);
        final JsonObject feature = (JsonObject) ((JsonList) profA.getParam("features")).listIterator().next();
        assertSame(feature.getParam("comments"), shared);

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 3);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 3);
        assertCommentsSize(root.getProfiles().get(0).getFeatures().get(0).getComments(), 3);
        assertCommentsSize(root.getProfiles().get(1).getComments(), 3);
    }

    /**
     * Stress document combining everything at once: one definition shared by several stubs across levels, a second id
     * with the stub before its definition, one dangling reference and one duplicate definition. Asserts the complete
     * linking-set state plus instance identity and size of every array field - none may be null.
     */
    @Test
    public void testCombinedStressDocument() throws Exception {

        final String json = "{ \"comments\": <\"1\">[\"r1\", \"r2\"], \"profiles\": ["
                + " { \"profile\": \"a\", \"comments\": <this::2>, \"features\": [ { \"feature\": \"f\", \"comments\": <this::1> } ] },"
                + " { \"profile\": \"b\", \"comments\": <\"2\">[\"x\"], \"features\": [ { \"feature\": \"g\", \"comments\": <this::99> } ] },"
                + " { \"profile\": \"c\", \"comments\": <\"1\">[\"dup\"] } ] }";

        final LinkingSet links = parseResource(json).getLinkingSet();
        assertEquals(links.getObjectIdMap().size(), 2, "self::1 and self::2: " + links.getObjectIdMap().keySet());
        assertTrue(links.getLinkMap().containsKey("self::1"));
        assertTrue(links.getLinkMap().containsKey("self::2"));
        assertTrue(links.getLinkMap().containsKey("self::99"));
        assertEquals(links.getUnconnectedSet().size(), 1);
        assertTrue(links.getUnconnectedSet().contains("self::99"), "only the dangling one: " + links.getUnconnectedSet());
        assertTrue(links.hasWarnings(), "the duplicate <\"1\"> definition must warn");
        assertTrue(links.getWarnings().get(0).contains("self::1"));

        final JsonObject answer = parseAnswer(json);
        final JsonItem shared1 = answer.getParam("comments");
        assertListSize(shared1, 2);
        final JsonList profiles = (JsonList) answer.getParam("profiles");
        final java.util.Iterator<JsonItem> it = profiles.listIterator();
        final JsonObject profA = (JsonObject) it.next();
        final JsonObject profB = (JsonObject) it.next();
        final JsonObject profC = (JsonObject) it.next();
        final JsonObject featF = (JsonObject) ((JsonList) profA.getParam("features")).listIterator().next();
        final JsonObject featG = (JsonObject) ((JsonList) profB.getParam("features")).listIterator().next();
        assertSame(featF.getParam("comments"), shared1, "feature stub shares the root definition");
        assertSame(profC.getParam("comments"), shared1, "the duplicate resolves to the first definition");
        assertListSize(profA.getParam("comments"), 1);
        assertSame(profA.getParam("comments"), profB.getParam("comments"), "early stub shares the later <\"2\">");
        assertListSize(featG.getParam("comments"), 0);
        assertNotNull(featG.getParam("comments"), "dangling must degrade to empty, not null");

        final ConfigRoot root = buildRoot(json);
        assertCommentsSize(root.getComments(), 2);
        assertCommentsSize(root.getProfiles().get(0).getComments(), 1);
        assertCommentsSize(root.getProfiles().get(0).getFeatures().get(0).getComments(), 2);
        assertCommentsSize(root.getProfiles().get(1).getComments(), 1);
        assertCommentsSize(root.getProfiles().get(1).getFeatures().get(0).getComments(), 0);
        assertCommentsSize(root.getProfiles().get(2).getComments(), 2);
    }

    /**
     * Reparsing the same diamond document twice yields identical linking sets and, per parse, one shared instance with
     * the expected size - the diamond resolution is deterministic.
     */
    @Test
    public void testReparseIsDeterministic() throws Exception {

        final String json = "{ \"comments\": <\"6\">[\"a\", \"b\", \"c\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": <this::6> } ] }";
        final LinkingSet first = parseResource(json).getLinkingSet();
        final LinkingSet second = parseResource(json).getLinkingSet();
        assertEquals(first.getObjectIdMap().keySet(), second.getObjectIdMap().keySet());
        assertEquals(first.getLinkMap().keySet(), second.getLinkMap().keySet());
        assertEquals(first.getUnconnectedSet(), second.getUnconnectedSet());

        final JsonObject answer1 = parseAnswer(json);
        final JsonObject answer2 = parseAnswer(json);
        assertListSize(answer1.getParam("comments"), 3);
        assertListSize(answer2.getParam("comments"), 3);
        assertSame(profileComments(answer1), answer1.getParam("comments"));
        assertSame(profileComments(answer2), answer2.getParam("comments"));
        assertNotSame(answer1.getParam("comments"), answer2.getParam("comments"),
                "two parses must not leak instances into each other");
    }

    /**
     * Write-read round trip of a shared ARRAY field: one String[] instance hanging at root/profile/feature comments is
     * written with the PREFIXED level as one {@code <"N">[...]} definition plus bare {@code <"N">} stubs, and the
     * re-parsed document shares one JsonList instance again with the expected sizes.
     */
    @Test
    public void testWriteReadRoundTripSharedArrayInstance() throws Exception {

        final JsonConfigDefinition definition = JsonConfigDefinition.getInstance();
        final JsonItemDefinition prefixed = prefixedLevel(definition);

        final String[] shared = {"shared a", "shared b"};
        final ConfigFeature feature = new ConfigFeature();
        feature.setFeature("ollama");
        feature.setComments(shared);
        final ConfigProfile profile = new ConfigProfile();
        profile.setProfile("main");
        profile.setComments(shared);
        profile.setFeatures(java.util.List.of(feature));
        final ConfigRoot bean = new ConfigRoot();
        bean.setComments(shared);
        bean.setProfiles(java.util.List.of(profile));

        final String json = JsonObjectWriter.writeToString(bean, prefixed, prefixed.getRootClass());
        assertTrue(json.matches("(?s).*\"comments\":\\s*<\"\\d+\">\\s*\\[.*"),
                "the first comments array must become the diamond definition: " + json);
        assertTrue(json.matches("(?s).*\"comments\":\\s*<\"\\d+\">(?!\\s*\\[).*"),
                "the later comments must be bare diamond references: " + json);

        final LinkingSet links = parseResource(json).getLinkingSet();
        assertEquals(countArrayDefinitions(links), 1, "exactly one array definition expected: " + links.getObjectIdMap().keySet());
        assertFalse(links.hasUnconnectedLinks(), "the written stubs must resolve: " + links.getUnconnectedSet());

        final JsonObject answer = parseAnswer(json);
        final JsonItem rootComments = answer.getParam("comments");
        assertListSize(rootComments, 2);
        assertSame(profileComments(answer), rootComments, "the written stub must share the written definition");

        final ConfigRoot reloaded = buildRoot(json);
        assertCommentsSize(reloaded.getComments(), 2);
        assertCommentsSize(reloaded.getProfiles().get(0).getComments(), 2);
        assertCommentsSize(reloaded.getProfiles().get(0).getFeatures().get(0).getComments(), 2);
    }

    /**
     * Null tolerance on build: a feature whose map fields are JSON null ({@code "settings": null}) must build without
     * an exception - the fields come out null, the diamond shared comments still resolve with the expected size.
     */
    @Test
    public void testNullMapFieldsToleratedOnBuild() throws Exception {

        final String json = "{ \"comments\": <\"2\">[\"n1\", \"n2\"], \"profiles\": [ { \"profile\": \"p\","
                + " \"comments\": <this::2>, \"features\": [ { \"feature\": \"f\", \"settings\": null,"
                + " \"labels\": null, \"enablements\": null, \"comments\": <this::2> } ] } ] }";
        final ConfigRoot root = buildRoot(json);
        assertNotNull(root);
        assertCommentsSize(root.getComments(), 2);
        final ConfigProfile profile = root.getProfiles().get(0);
        assertCommentsSize(profile.getComments(), 2);
        final ConfigFeature feature = profile.getFeatures().get(0);
        assertNull(feature.getSettings(), "a JSON null settings must build to a null field, not crash");
        assertNull(feature.getLabels());
        assertNull(feature.getEnablements());
        assertCommentsSize(feature.getComments(), 2);
    }

    /**
     * Policy boundary: equal content but distinct String[] instances stay inline under the sameCollection policy -
     * no array diamond is emitted, and the re-parsed document holds independent lists with the correct sizes.
     */
    @Test
    public void testWriteReadDistinctArrayInstancesStayInline() throws Exception {

        final JsonConfigDefinition definition = JsonConfigDefinition.getInstance();
        final JsonItemDefinition prefixed = prefixedLevel(definition);

        final ConfigProfile profile = new ConfigProfile();
        profile.setProfile("main");
        profile.setComments(new String[]{"same", "content"});
        final ConfigRoot bean = new ConfigRoot();
        bean.setComments(new String[]{"same", "content"});
        bean.setProfiles(java.util.List.of(profile));

        final String json = JsonObjectWriter.writeToString(bean, prefixed, prefixed.getRootClass());
        assertFalse(json.matches("(?s).*\"comments\":\\s*<\"\\d+\">.*"),
                "distinct array instances must not share an id: " + json);

        final LinkingSet links = parseResource(json).getLinkingSet();
        assertEquals(countArrayDefinitions(links), 0, "no array definition without sharing");

        final JsonObject answer = parseAnswer(json);
        assertListSize(answer.getParam("comments"), 2);
        assertNotSame(profileComments(answer), answer.getParam("comments"),
                "independent instances must stay independent");

        final ConfigRoot reloaded = buildRoot(json);
        assertCommentsSize(reloaded.getComments(), 2);
        assertCommentsSize(reloaded.getProfiles().get(0).getComments(), 2);
    }

    private static JsonItemDefinition prefixedLevel(final JsonConfigDefinition definition) {

        return new JsonItemDefinition() {
            @Override
            public de.jare.jsoncasted.model.JsonModel getModel() {
                return definition.getModel();
            }

            @Override
            public JsonCastingLevel getCastingLevel() {
                return definition.getCastingLevel();
            }

            @Override
            public JsonWoodIdLevel getWoodIdLevel() {
                return JsonWoodIdLevel.PREFIXED;
            }
        };
    }

    private static long countArrayDefinitions(LinkingSet links) {

        return links.getObjectIdMap().values().stream().filter(e -> e.getNode() != null && e.getNode().isArray()).count();
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
