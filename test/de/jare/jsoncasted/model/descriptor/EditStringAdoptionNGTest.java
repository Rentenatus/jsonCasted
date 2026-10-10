/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.model.descriptor.def.JsonModelDescriptorDefinition;
import de.jare.jsoncasted.model.item.JsonAnnotation;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests of the editString annotation adoption (annotation concept): the cast based parse is field driven and blind
 * for {@code @} keys, so JsonModelDescriptor.adoptEditStrings walks the raw description tree and hands single element
 * {@code @editString} string arrays to the matching type descriptors.
 *
 * @author Janusch Rentenatus
 */
public class EditStringAdoptionNGTest {

    /**
     * A single element @editString array is adopted as the display template of the matching type; types without the
     * annotation keep a null template.
     */
    @Test
    public void testAdoptionOfSingleElementTemplate() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        descriptor.addType(new JsonTypeDescriptor("profile"));
        descriptor.addType(new JsonTypeDescriptor("plain"));

        final JsonNode description = JsonNode.objectNode()
                .put("modelName", JsonNode.stringNode("Mini"))
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("profile"))
                                .put("@editString", JsonNode.arrayNode(List.of(
                                        JsonNode.stringNode("${name} (${typ})")))))
                        .put("plain", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("plain"))));

        final int adopted = descriptor.adoptEditStrings(description);

        assertEquals(adopted, 1);
        assertEquals(descriptor.getType("profile").getEditString(), "${name} (${typ})");
        assertNull(descriptor.getType("plain").getEditString());
    }

    /**
     * Arrays with zero or multiple elements are invalid templates and are skipped by the adoption - the editor
     * validation reports them, the runtime stays untouched.
     */
    @Test
    public void testInvalidArraysAreSkipped() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        descriptor.addType(new JsonTypeDescriptor("empty"));
        descriptor.addType(new JsonTypeDescriptor("multi"));

        final JsonNode description = JsonNode.objectNode()
                .put("describedTypes", JsonNode.objectNode()
                        .put("empty", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("empty"))
                                .put("@editString", JsonNode.arrayNode(List.of())))
                        .put("multi", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("multi"))
                                .put("@editString", JsonNode.arrayNode(List.of(
                                        JsonNode.stringNode("${a}"),
                                        JsonNode.stringNode("${b}"))))));

        assertEquals(descriptor.adoptEditStrings(description), 0);
        assertNull(descriptor.getType("empty").getEditString());
        assertNull(descriptor.getType("multi").getEditString());
    }

    /**
     * An inline string value counts as its single element - the annotation is implicitly a string array, the plain
     * form is the tolerated shorthand for exactly one template.
     */
    @Test
    public void testInlineStringCountsAsSingleElement() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        descriptor.addType(new JsonTypeDescriptor("profile"));

        final JsonNode description = JsonNode.objectNode()
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("profile"))
                                .put("@editString", JsonNode.stringNode("${name}"))));

        assertEquals(descriptor.adoptEditStrings(description), 1);
        assertEquals(descriptor.getType("profile").getEditString(), "${name}");
    }

    /**
     * Missing inputs are harmless: a null root, a non-object root and a description without describedTypes adopt
     * nothing and never throw.
     */
    @Test
    public void testMissingInputsAdoptNothing() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");

        assertEquals(descriptor.adoptEditStrings(null), 0);
        assertEquals(descriptor.adoptEditStrings(JsonNode.stringNode("no object")), 0);
        assertEquals(descriptor.adoptEditStrings(JsonNode.objectNode()
                .put("modelName", JsonNode.stringNode("Mini"))), 0);
    }

    /**
     * The type entry name falls back to the describedTypes map key when the entry carries no typeName - the map key
     * is the registration key of the descriptor anyway.
     */
    @Test
    public void testTypeNameFallsBackToMapKey() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        descriptor.addType(new JsonTypeDescriptor("profile"));

        final JsonNode description = JsonNode.objectNode()
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("@editString", JsonNode.arrayNode(List.of(
                                        JsonNode.stringNode("${name}"))))));

        assertEquals(descriptor.adoptEditStrings(description), 1);
        assertEquals(descriptor.getType("profile").getEditString(), "${name}");
    }

    /**
     * The self description declares the editString annotation on the type definition (JsonTypeDescriptor), so every
     * type entry of a description file may carry @editString and the model editor shows the annotation row.
     */
    @Test
    public void testEditStringDeclaredOnTypeDefinition() {
        final JsonModelDescriptorDefinition definition = JsonModelDescriptorDefinition.getInstance();

        assertNotNull(definition.getDescriptType().getAnnotation(JsonAnnotation.EDIT_STRING),
                "descriptType must declare the editString annotation");

        final JsonModelDescriptor self = definition.getModel().getOrCreateDescriptor();
        final JsonTypeDescriptor type = self.getType(definition.getDescriptType().getcName());
        assertNotNull(type);
        assertNotNull(type.getAnnotation(JsonAnnotation.EDIT_STRING),
                "describeHead must carry the editString declaration into the descriptor");
    }
}
