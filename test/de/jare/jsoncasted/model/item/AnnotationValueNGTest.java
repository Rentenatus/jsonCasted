/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.builder.JsonStringBuilder;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.def.JsonModelDescriptorDefinition;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Tests of the declaration value (annotation concept, decision 13): the factories on JsonAnnotation, the describeHead
 * transport into the descriptor, the round trip through a description file and the value copy of the default
 * annotation adoption.
 *
 * @author Janusch Rentenatus
 */
public class AnnotationValueNGTest {

    private static final String PROFILE_TYPE = "de.jare.jsonconfig.item.ConfigProfile";

    /**
     * The factories wire name and value without detours: editString carries the template, doc the text, the doc
     * wildcard stays value free, the generic withValue covers everything else.
     */
    @Test
    public void testFactoriesWireNameAndValue() {
        final JsonAnnotation editString = JsonAnnotation.editString("${profile}");
        assertEquals(editString.getName(), JsonAnnotation.EDIT_STRING);
        assertEquals(editString.getValue(), "${profile}");
        assertFalse(editString.isTransient());

        final JsonAnnotation doc = JsonAnnotation.doc("This is a profile.");
        assertEquals(doc.getName(), JsonAnnotation.DOC);
        assertEquals(doc.getValue(), "This is a profile.");

        assertEquals(JsonAnnotation.doc().getName(), JsonAnnotation.DOC_ALL);
        assertNull(JsonAnnotation.doc().getValue(), "the doc wildcard declaration stays value free");

        assertEquals(new JsonAnnotation("hint").withValue("x").getValue(), "x",
                "withValue is the generic escape hatch");
    }

    /**
     * describeHead carries the declaration values into the descriptor: the config model speaks, the ConfigProfile
     * type of its descriptor knows template and documentation - without any editString special case on the way.
     */
    @Test
    public void testDescribeHeadCarriesTheDeclaredValue() {
        final JsonModelDescriptor descriptor = JsonConfigDefinition.getInstance().getModel().getOrCreateDescriptor();
        final JsonTypeDescriptor profile = descriptor.getType(PROFILE_TYPE);
        assertNotNull(profile, "the config model must describe ConfigProfile");

        assertEquals(profile.getAnnotation(JsonAnnotation.EDIT_STRING).getValue(), "${profile}");
        assertEquals(profile.getAnnotation(JsonAnnotation.DOC).getValue(), "This is a profile.");
    }

    /**
     * Every object type of the config model carries its display template: the root over the recursive logging
     * display, a feature via its name, logging with the upper filter on its level.
     */
    @Test
    public void testConfigModelEditStringTemplates() {
        final JsonModelDescriptor descriptor = JsonConfigDefinition.getInstance().getModel().getOrCreateDescriptor();

        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigRoot")
                        .getAnnotation(JsonAnnotation.EDIT_STRING).getValue(), "config ${mainLogging}",
                "the root shows itself with the recursive display of its main logging");
        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigLogging")
                        .getAnnotation(JsonAnnotation.EDIT_STRING).getValue(), "${level|upper}: ${path}",
                "logging demonstrates the upper filter");
        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigFeature")
                        .getAnnotation(JsonAnnotation.EDIT_STRING).getValue(), "${feature}",
                "a feature displays via its feature name");
    }

    /**
     * The doc declarations document types and fields: the describeHead path carries the type docs, the
     * describeDependencies path the field docs into the descriptor - both readable via getAnnotation(DOC).
     */
    @Test
    public void testConfigModelDocDeclarations() {
        final JsonModelDescriptor descriptor = JsonConfigDefinition.getInstance().getModel().getOrCreateDescriptor();

        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigLogging")
                        .getAnnotation(JsonAnnotation.DOC).getValue(), "Logging configuration: a level and a file path.");
        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigFeature")
                        .getAnnotation(JsonAnnotation.DOC).getValue(), "A feature with its own logging, settings and labels.");
        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigRoot")
                        .getAnnotation(JsonAnnotation.DOC).getValue(), "Root of a seed configuration.");

        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigLogging").getField("level")
                        .getAnnotation(JsonAnnotation.DOC).getValue(), "Log level of this logger.",
                "the field doc rides describeDependencies into the field descriptor");
        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigProfile").getField("profile")
                        .getAnnotation(JsonAnnotation.DOC).getValue(), "Name of the profile.");
        assertEquals(descriptor.getType("de.jare.jsonconfig.item.ConfigRoot").getField("profiles")
                        .getAnnotation(JsonAnnotation.DOC).getValue(), "The profiles of the seed.");
    }

    /**
     * The round trip through a description file: saveAs writes the declaration values into the annotations list, the
     * parse reads them back - the reflected value field of the self description carries them in both directions.
     */
    @Test
    public void testRoundTripThroughDescriptionFile() throws Exception {
        final JsonModelDescriptor descriptor = JsonConfigDefinition.getInstance().getModel().getOrCreateDescriptor();
        final File file = File.createTempFile("annotation_value", ".json");
        file.deleteOnExit();
        descriptor.saveAs(file.getPath());

        final String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("${profile}"), "the file carries the declaration value:\n" + content);

        final JsonModelDescriptorDefinition definition = JsonModelDescriptorDefinition.getInstance();
        final WoodResolution resolution = JsonParser.parse(file, definition, definition.getRootClass());
        final JsonModelDescriptor parsed = (JsonModelDescriptor) JsonBuilder.buildInstance(
                definition.getModel(), false, resolution.getAnswer());

        assertEquals(parsed.getType(PROFILE_TYPE).getAnnotation(JsonAnnotation.EDIT_STRING).getValue(),
                "${profile}", "the parse returns the template of the declaration");
        assertEquals(parsed.getType(PROFILE_TYPE).getAnnotation(JsonAnnotation.DOC).getValue(),
                "This is a profile.", "the parse returns the documentation of the declaration");
    }

    /**
     * The adoption of the default annotations copies the declaration value with: a basic model annotation with a
     * value reaches every registered type as a value copy.
     */
    @Test
    public void testAdoptionCopiesTheValue() {
        final JsonModel model = new JsonModel("adoptTest");
        model.addBasicModel();
        model.getJsonClass("String").addAnnotation(JsonAnnotation.doc("base doc"));

        final JsonClass mine = new JsonClass("mine", JsonNodeType.OBJECT, new JsonStringBuilder());
        model.addClass(mine);

        assertEquals(mine.getAnnotation(JsonAnnotation.DOC).getValue(), "base doc",
                "the adopted declaration carries the value");
    }
}
