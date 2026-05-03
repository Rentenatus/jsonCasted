/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

import static de.jare.jsoncasted.model.JsonCollectionType.ARRAY;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonInter;
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 * Definition class for implementation tests. Sets up the JSON model structure
 * for testing various value types and their inheritance relationships.
 *
 * @author Janusch Rentenatus
 */
public class ImplTestDefinition implements JsonItemDefinition {

    /**
     * Singleton instance of ImplTestDefinition.
     */
    public static final ImplTestDefinition INSTANCE = new ImplTestDefinition();

    /**
     * Returns the singleton instance.
     *
     * @return The singleton ImplTestDefinition instance.
     */
    public static ImplTestDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass testBox;

    /**
     * Constructs an ImplTestDefinition instance. Creates the model and defines
     * the structure for all test value classes.
     */
    public ImplTestDefinition() {
        model = new JsonModel("impltest");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");
        final JsonClass asInteger = model.getJsonClass("Integer");

        JsonClass valueBoolean = model.newJsonReflect(ValueBoolean.class, null);
        valueBoolean.addCParam("frage", asBoolean, "getFrage");

        JsonClass valueInteger = model.newJsonReflect(ValueInteger.class, null);
        valueInteger.addCParam("zahl", asInteger);

        JsonClass valueString = model.newJsonReflect(ValueString.class, null);
        valueString.addCParam("text", asString);

        JsonClass valueStringSub = model.newJsonReflect(ValueStringSub.class, null, valueString);
        // valueStringSub.addCParam("text", asString); // passing on valueString

        JsonClass valueStringSubSub = model.newJsonReflect(ValueStringSubSub.class, null, valueStringSub);
        // valueStringSubSub.addCParam("text", asString); // passing on valueStringSub
        valueStringSubSub.addCParam("frage", asBoolean, "getFrage");

        JsonClass enumSeason = model.newJsonEnumByName(EnumSeason.class, null, EnumSeason.VALUES);
        JsonClass valueSeason = model.newJsonReflect(ValueSeason.class, null);
        valueSeason.addCParam("season", enumSeason);

        // ValueStringSub implements ValueInterface, but isn't registered.
        JsonInter valueIx = model.newJsonInterface(ValueInterface.class, null, valueBoolean, valueInteger, valueString, valueStringSubSub, valueSeason);

        testBox = model.newJsonReflect(TestBox.class, null);
        testBox.addField("subsub", valueString);
        testBox.addField("one", valueIx);
        testBox.addField("list", valueIx, LIST);
        testBox.addField("arr", valueIx, ARRAY);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    /**
     * Returns the JsonClass for TestBox.
     *
     * @return The JsonClass describing the TestBox structure.
     */
    public JsonClass getTestBox() {
        return testBox;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NECESSARY_CLASS_DEF;
    }
}
