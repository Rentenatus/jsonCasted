/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.model.item.JsonAnnotation;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests of the raw annotation value adoption (annotation concept, decision 13): the cast based parse is field
 * driven and blind for @ keys, so JsonModelDescriptor.adoptAnnotationValues walks the raw description tree - type
 * entries and field entries - and hands every @ key value to the matching declaration (array elements joined line
 * by line, javadoc style).
 *
 * @author Janusch Rentenatus
 */
public class AnnotationValueAdoptionNGTest {

    /**
     * Type entry values reach the declarations of the descriptor: a single element template overrides, a multi
     * element doc joins line by line like a javadoc.
     */
    @Test
    public void testTypeEntryValuesAreAdopted() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        final JsonTypeDescriptor profile = new JsonTypeDescriptor("profile");
        profile.addAnnotation(JsonAnnotation.editString("${name}"));
        descriptor.addType(profile);

        final JsonNode description = JsonNode.objectNode()
                .put("modelName", JsonNode.stringNode("Mini"))
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("profile"))
                                .put("@editString", JsonNode.arrayNode(List.of(
                                        JsonNode.stringNode("${name} (${typ})"))))
                                .put("@doc", JsonNode.arrayNode(List.of(
                                        JsonNode.stringNode("Line one."),
                                        JsonNode.stringNode("Line two."))))));

        assertEquals(descriptor.adoptAnnotationValues(description), 2);
        assertEquals(profile.getAnnotation(JsonAnnotation.EDIT_STRING).getValue(), "${name} (${typ})");
        assertEquals(profile.getAnnotation(JsonAnnotation.DOC).getValue(), "Line one.\nLine two.",
                "a multi element doc joins line by line, javadoc style");
    }

    /**
     * A hand edited value for a silent model: without a declaration the adoption creates one, so the value reaches
     * the runtime anyway - the binding in the editor stays a free annotation with a warning, the value does not.
     */
    @Test
    public void testDeclarationIsCreatedWhenModelIsSilent() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        descriptor.addType(new JsonTypeDescriptor("profile"));

        final JsonNode description = JsonNode.objectNode()
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("profile"))
                                .put("@editString", JsonNode.arrayNode(List.of(
                                        JsonNode.stringNode("${name}"))))));

        assertEquals(descriptor.adoptAnnotationValues(description), 1);
        final JsonAnnotation created = descriptor.getType("profile").getAnnotation(JsonAnnotation.EDIT_STRING);
        assertNotNull(created, "the adoption creates the declaration of a silent model");
        assertEquals(created.getValue(), "${name}");
        assertFalse(created.isTransient());
    }

    /**
     * Field entry values reach the field declarations: a doc on the field entry of the description lands on the
     * JsonFieldDescriptor declaration.
     */
    @Test
    public void testFieldEntryValuesAreAdopted() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        final JsonTypeDescriptor profile = new JsonTypeDescriptor("profile");
        profile.addField(new JsonFieldDescriptor("name", "String"));
        profile.addField(new JsonFieldDescriptor("other", "String"));
        descriptor.addType(profile);

        final JsonNode description = JsonNode.objectNode()
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("profile"))
                                .put("fields", JsonNode.arrayNode(List.of(
                                        JsonNode.objectNode()
                                                .put("fieldName", JsonNode.stringNode("name"))
                                                .put("@doc", JsonNode.arrayNode(List.of(
                                                        JsonNode.stringNode("The name.")))),
                                        JsonNode.objectNode()
                                                .put("fieldName", JsonNode.stringNode("other")))))));

        assertEquals(descriptor.adoptAnnotationValues(description), 1);
        assertEquals(profile.getField("name").getAnnotation(JsonAnnotation.DOC).getValue(), "The name.");
        assertNull(profile.getField("other").getAnnotation(JsonAnnotation.DOC),
                "only the annotated field entry carries a value");
    }

    /**
     * An inline string counts as its single line, an empty array carries no value, and missing inputs adopt
     * nothing without ever throwing.
     */
    @Test
    public void testInlineStringEmptyArrayAndMissingInputs() {
        final JsonModelDescriptor descriptor = new JsonModelDescriptor("Mini");
        final JsonTypeDescriptor profile = new JsonTypeDescriptor("profile");
        descriptor.addType(profile);

        final JsonNode description = JsonNode.objectNode()
                .put("describedTypes", JsonNode.objectNode()
                        .put("profile", JsonNode.objectNode()
                                .put("typeName", JsonNode.stringNode("profile"))
                                .put("@doc", JsonNode.stringNode("Inline text."))
                                .put("@hint", JsonNode.arrayNode(List.of()))));

        assertEquals(descriptor.adoptAnnotationValues(description), 1,
                "the inline string is one adopted value");
        assertEquals(profile.getAnnotation(JsonAnnotation.DOC).getValue(), "Inline text.");
        assertNull(profile.getAnnotation("hint"), "an empty array carries no value");

        assertEquals(descriptor.adoptAnnotationValues(null), 0);
        assertEquals(descriptor.adoptAnnotationValues(JsonNode.stringNode("no object")), 0);
        assertEquals(descriptor.adoptAnnotationValues(JsonNode.objectNode()
                .put("modelName", JsonNode.stringNode("Mini"))), 0);
    }
}
