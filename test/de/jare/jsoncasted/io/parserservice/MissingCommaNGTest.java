/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Regression tests for the missing comma hardening: content that follows a value without a separating comma was
 * silently discarded before (the classic silent loss), now the parser reports the missing separator with row and
 * key name. Valid multi-line wood JSON keeps parsing without false positives.
 *
 * @author Janusch Rentenatus
 */
public class MissingCommaNGTest {

    /**
     * The original silent loss pattern: the next entry after a value without a comma was swallowed completely.
     */
    @Test
    public void testMissingCommaBetweenEntriesThrows() {
        assertParseFails("settings: { host1: \"http://localhost\" port: 11434 }",
                "Unexpected characters after the value of 'host1'",
                "The swallowed entry must be reported");
    }

    /**
     * A second value token after a parsed value (no comma in between) is a missing comma, not an overwrite.
     */
    @Test
    public void testSecondValueAfterValueThrows() {
        assertParseFails(" level: \"10\" \"20\" ",
                "Missing comma after the value of 'level'",
                "The second value must be reported, not silently overwriting the first");
        assertParseFails(" list: [1, 2] \"stray\" ",
                "Missing comma after the value of 'list'",
                "A value after a list without a comma must be reported");
    }

    /**
     * Characters between a quoted key and its colon were silently discarded before.
     */
    @Test
    public void testJunkBetweenKeyAndColonThrows() {
        assertParseFails(" \"key\" junk : \"v\" ",
                "Unexpected characters between the key 'key' and its colon",
                "Junk between key and colon must be reported");
    }

    /**
     * Valid wood JSON with multi-line lists, nested objects and quoted keys keeps parsing - the hardening must
     * not produce false positives.
     */
    @Test
    public void testValidMultiLineJsonStillParses() throws Exception {
        final String json = "{\n"
                + "  \"_woodModel\": { \"name\": \"Seed\" },\n"
                + "  @hint: [\n"
                + "    \"Object level annotation.\",\n"
                + "    \"Second row.\"\n"
                + "  ],\n"
                + "  mainLogging: {\n"
                + "    level: 10,\n"
                + "    path: \"seeddata/status/\"\n"
                + "  },\n"
                + "  profiles: [ { profile: \"main\", features: [] } ]\n"
                + "}\n";
        assertNotNull(JsonParserService.parse(json, JsonDebugLevel.SIMPLE),
                "Valid wood JSON must keep parsing");
    }

    private void assertParseFails(String json, String expectedMessagePart, String what) {
        try {
            JsonParserService.parse("{" + json + "}", JsonDebugLevel.SIMPLE);
            fail(what + ": expected a JsonParseException");
        } catch (JsonParseException ex) {
            assertTrue(ex.getMessage() != null && ex.getMessage().contains(expectedMessagePart),
                    what + ": expected '" + expectedMessagePart + "' but got '" + ex.getMessage() + "'");
        } catch (Exception ex) {
            fail(what + ": expected a JsonParseException but got " + ex);
        }
    }
}
