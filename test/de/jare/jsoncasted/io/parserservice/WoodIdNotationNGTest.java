/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.LinkingSet;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_OBJECT_ID;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests for the wood id notation in angle brackets: {@code <"56">} prefixes objects and arrays with an identity,
 * a bare id builds a reference stub. Covers the cast combination, quoted and unquoted contents, cross resource keys
 * and the duplicate id warning (including the old _woodObjectId member form).
 *
 * @author Janusch Rentenatus
 */
public class WoodIdNotationNGTest {

    /**
     * (Profile)<"56">{...} attaches cast and id to the object as members.
     */
    @Test
    public void testCastThenIdOnObject() throws Exception {
        final JsonNode root = parseRootOf("{ \"obj\": (Profile)<\"56\">{ \"a\": 1 } }");
        final JsonNode obj = root.asObjectValues().get("obj");
        assertTrue(obj.isObject());
        assertEquals(obj.asObjectValues().get(TERM_CLASS).asText(), "Profile");
        assertEquals(obj.asObjectValues().get(TERM_WOOD_OBJECT_ID).asText(), "56");
        assertEquals(obj.getObjectId("self"), "self::56");
    }

    /**
     * <"56">{...} is sugar for the _woodObjectId member.
     */
    @Test
    public void testIdOnObjectIsMemberSugar() throws Exception {
        final JsonNode root = parseRootOf("<\"56\">{ \"a\": 1 }");
        assertTrue(root.isObject());
        assertEquals(root.asObjectValues().get(TERM_WOOD_OBJECT_ID).asText(), "56");
        assertEquals(root.getObjectId("self"), "self::56");
    }

    /**
     * <"56">[...] carries the identity first class on the array node.
     */
    @Test
    public void testIdOnArrayIsFirstClass() throws Exception {
        final JsonNode root = parseRootOf("<\"56\">[1, 2]");
        assertTrue(root.isArray());
        assertEquals(root.getWoodObjectId(), "56");
        assertEquals(root.getObjectId("self"), "self::56");
        assertEquals(root.asArray().size(), 2);
    }

    /**
     * A bare id without a literal becomes a local reference stub: _woodLink with the this:: prefix.
     */
    @Test
    public void testBareIdIsLocalStub() throws Exception {
        final JsonNode root = parseRootOf("{ \"ref\": <\"56\"> }");
        final JsonNode ref = root.asObjectValues().get("ref");
        assertTrue(ref.isObject());
        assertEquals(ref.asObjectValues().get(TERM_WOOD_LINK).asText(), "this::56");
        final LinkingSet links = parseResource("{ \"ref\": <\"56\"> }").getLinkingSet();
        assertTrue(links.getLinkMap().containsKey("self::56"), "link must normalize to the provider key");
    }

    /**
     * A bare cross resource id keeps its provider key.
     */
    @Test
    public void testCrossResourceBareId() throws Exception {
        final JsonNode root = parseRootOf("{ \"ref\": <\"save::77\"> }");
        final JsonNode ref = root.asObjectValues().get("ref");
        assertEquals(ref.asObjectValues().get(TERM_WOOD_LINK).asText(), "save::77");
    }

    /**
     * Unquoted id contents are a tolerant fallback and yield the same text.
     */
    @Test
    public void testUnquotedIdFallback() throws Exception {
        final JsonNode root = parseRootOf("{ \"arr\": <56>[1] }");
        assertEquals(root.asObjectValues().get("arr").getWoodObjectId(), "56");

        final JsonNode root2 = parseRootOf("{ \"ref\": <56> }");
        assertEquals(root2.asObjectValues().get("ref").asObjectValues().get(TERM_WOOD_LINK).asText(), "this::56");
    }

    /**
     * Ids also work as list elements: identity before an object, bare id as stub element.
     */
    @Test
    public void testIdNotationInListElements() throws Exception {
        final JsonNode root = parseRootOf("[ <\"56\">{ \"a\": 1 }, <\"9\">, \"plain\" ]");
        assertEquals(root.asArray().size(), 3);
        assertEquals(root.asArray().get(0).asObjectValues().get(TERM_WOOD_OBJECT_ID).asText(), "56");
        assertEquals(root.asArray().get(1).asObjectValues().get(TERM_WOOD_LINK).asText(), "this::9");
        assertEquals(root.asArray().get(2).asText(), "plain");
    }

    /**
     * Duplicated ids in the new notation warn and keep the first occurrence.
     */
    @Test
    public void testDuplicateIdWarningNewNotation() throws Exception {
        final LinkingSet links = parseResource("{ \"a\": <\"56\">{ \"x\": 1 }, \"b\": <\"56\">{ \"x\": 2 } }")
                .getLinkingSet();
        assertTrue(links.hasWarnings(), "a duplicate id must warn");
        assertTrue(links.getWarnings().get(0).contains("self::56"));
        assertEquals(links.getObjectIdMap().size(), 1);
        final JsonNode first = links.getObjectIdMap().get("self::56").getNode();
        assertEquals(first.asObjectValues().get("x").asLong(), Long.valueOf(1L));
    }

    /**
     * The duplicate warning applies to the old _woodObjectId member form as well.
     */
    @Test
    public void testDuplicateIdWarningOldMemberForm() throws Exception {
        final LinkingSet links = parseResource("{ \"a\": { \"_woodObjectId\": \"56\", \"x\": 1 },"
                + " \"b\": { \"_woodObjectId\": \"56\", \"x\": 2 } }").getLinkingSet();
        assertTrue(links.hasWarnings(), "the old member form must warn too");
        assertEquals(links.getObjectIdMap().size(), 1);
        assertEquals(links.getObjectIdMap().get("self::56").getNode().asObjectValues().get("x").asLong(), 1L);
    }

    /**
     * A definition id with a provider prefix is rejected as a scan exception.
     */
    @Test
    public void testProviderPrefixOnDefinitionFails() throws Exception {
        final JsonResource res = parseResource("{ \"a\": <\"save::56\">{ } }");
        assertFalse(res.getLinkingSet().getScanExceptions().isEmpty(),
                "a :: in a definition id must become a scan exception");
    }

    /**
     * The reverse modifier order is rejected: the cast comes first, then the id.
     */
    @Test
    public void testCastAfterIdFails() {
        assertParseFails("{ \"x\": <\"56\"> (Profile){ } }", "cast comes first");
    }

    /**
     * An empty id is rejected.
     */
    @Test
    public void testEmptyIdFails() {
        assertParseFails("{ \"x\": <> }", "must not be empty");
    }

    private JsonNode parseRootOf(String json) throws Exception {
        return parseResource(json).getRoot();
    }

    private JsonResource parseResource(String json) throws Exception {
        return JsonParserService.parse(json, JsonDebugLevel.SIMPLE);
    }

    private void assertParseFails(String json, String expectedMessagePart) {
        try {
            JsonParserService.parse(json, JsonDebugLevel.SIMPLE);
            fail("expected a JsonParseException containing: " + expectedMessagePart);
        } catch (JsonParseException | java.io.IOException ex) {
            assertTrue(ex.getMessage().contains(expectedMessagePart),
                    "message '" + ex.getMessage() + "' must contain: " + expectedMessagePart);
        }
    }

}
