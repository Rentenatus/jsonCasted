/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.io.parserservice.JsonParserService;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.descriptor.def.JsonModelDescriptorDefinition;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Regression tests for the null hardening: the unquoted null token parses as a real null node (not as the string
 * "null"), a null value for a list-typed field loads as an empty list instead of crashing with an NPE, and null
 * elements in an annotations list never poison the descriptor lookups.
 *
 * @author Janusch Rentenatus
 */
public class NullHardeningNGTest {

    /**
     * The unquoted null token is a real null node; true, false and numbers keep their own types.
     */
    @Test
    public void testNullTokenParsesAsNullNode() {
        assertEquals(JsonNode.varNode("null").getType(), JsonNodeType.NULL,
                "The unquoted null token must be a null node, not the string 'null'");
        assertEquals(JsonNode.varNode(" null ").getType(), JsonNodeType.NULL,
                "Surrounding whitespace must not turn null into a string");
        assertEquals(JsonNode.varNode("true").getType(), JsonNodeType.BOOLEAN, "true stays boolean");
        assertEquals(JsonNode.varNode("123").getType(), JsonNodeType.LONG, "numbers stay longs");
        assertEquals(JsonNode.varNode("hello").getType(), JsonNodeType.STRING,
                "Other unquoted tokens stay strings");
    }

    /**
     * "annotations": null and "annotations": [null] both load as an empty annotation list - neither crashes the
     * load nor poisons a later getAnnotation lookup.
     */
    @Test
    public void testNullAnnotationsLoadAsEmptyList() throws Exception {
        checkNullAnnotations("null", "A null value for a list-typed field loads as an empty list");
        checkNullAnnotations("[null]", "A list with a null element loads without the poison");
    }

    private void checkNullAnnotations(String annotationsValue, String what) throws Exception {
        final String json = "{\n"
                + "  modelName: \"Seed\",\n"
                + "  describedTypes: {\n"
                + "    \"de.jare.jsonconfig.item.ConfigRoot\": {\n"
                + "      \"typeName\": \"de.jare.jsonconfig.item.ConfigRoot\",\n"
                + "      \"annotations\": " + annotationsValue + "\n"
                + "    }\n"
                + "  }\n"
                + "}\n";
        final JsonResource resource = JsonParserService.parse(json, JsonDebugLevel.SIMPLE);
        final JsonModelDescriptorDefinition metaDef = JsonModelDescriptorDefinition.getInstance();
        final WoodResolution reso = JsonParser.parse(resource, metaDef.getDescriptor(),
                metaDef.getRootClass().getcName());
        final JsonModelDescriptor loaded = (JsonModelDescriptor) JsonBuilder
                .buildInstance(metaDef.getModel(), true, reso.getAnswer());

        assertNotNull(loaded, what + ": the descriptor must load at all");
        final JsonTypeDescriptor rootType = loaded.getType("de.jare.jsonconfig.item.ConfigRoot");
        assertNotNull(rootType, what + ": the type must be present");
        assertTrue(rootType.getAnnotations().isEmpty(),
                what + ": the annotations must be empty: " + rootType.getAnnotations());
        assertNull(rootType.getAnnotation("doc"),
                what + ": a lookup on the hardened list must simply find nothing");
    }
}
