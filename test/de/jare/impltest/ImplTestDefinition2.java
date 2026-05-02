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
import de.jare.jsoncasted.model.JsonRepo;
import de.jare.jsoncasted.model.JsonRepoModel;
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
public class ImplTestDefinition2 implements JsonItemDefinition {

    /**
     * Singleton instance of ImplTestDefinition.
     */
    public static final ImplTestDefinition2 INSTANCE = new ImplTestDefinition2();

    /**
     * Returns the singleton instance.
     *
     * @return The singleton ImplTestDefinition instance.
     */
    public static ImplTestDefinition2 getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonRepoModel repoModel;
    private final JsonClass testBox;

    /**
     * Constructs an ImplTestDefinition instance. Creates the model and defines
     * the structure for all test value classes.
     */
    public ImplTestDefinition2() {
        model = new JsonModel("impltest");
        model.addBasicModel();
        repoModel = new JsonRepoModel("repo");
        repoModel.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");
        final JsonClass asInteger = model.getJsonClass("Integer");

        JsonClass valueBoolean = model.newJsonReflect(ValueBoolean.class);
        valueBoolean.addCParam("frage", asBoolean, "getFrage");

        JsonClass valueInteger = model.newJsonReflect(ValueInteger.class);
        valueInteger.addCParam("zahl", asInteger);

        JsonClass valueString = model.newJsonReflect(ValueString.class);
        valueString.addCParam("text", asString);

        JsonClass valueStringSub = model.newJsonReflect(ValueStringSub.class, valueString);
        // valueStringSub.addCParam("text", asString); // passing on valueString

        JsonClass valueStringSubSub = model.newJsonReflect(ValueStringSubSub.class, valueStringSub);
        // valueStringSubSub.addCParam("text", asString); // passing on valueStringSub
        valueStringSubSub.addCParam("frage", asBoolean, "getFrage");

        JsonClass enumSeason = model.newJsonEnumByName(EnumSeason.class, EnumSeason.VALUES);
        JsonClass valueSeason = model.newJsonReflect(ValueSeason.class);
        valueSeason.addCParam("season", enumSeason);

        // ValueStringSub implements ValueInterface, but isn't registered.
        JsonInter valueIx = model.newJsonInterface(ValueInterface.class, valueBoolean, valueInteger, valueString, valueStringSubSub, valueSeason);

        testBox = model.newJsonReflect(TestBox.class);
        testBox.addField("subsub", valueString);
        testBox.addField("one", valueIx);
        testBox.addField("list", valueIx, LIST);
        testBox.addField("arr", valueIx, ARRAY);

        repoModel.addRecursive(model, valueIx);
        JsonClass repo = model.newJsonReflect(JsonRepo.class);
        repo.addCParam("repoName", asString);
        repo.addField("contents", valueIx, LIST);
        model.addRepoModel("save", repoModel);

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
