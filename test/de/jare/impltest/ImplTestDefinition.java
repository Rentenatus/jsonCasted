/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
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
 *
 * @author Janusch Rentenatus
 */
public class ImplTestDefinition implements JsonItemDefinition {

    public static final ImplTestDefinition INSTANCE = new ImplTestDefinition();

    public static ImplTestDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass testBox;

    public ImplTestDefinition() {
        model = new JsonModel("impltest");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");
        final JsonClass asInteger = model.getJsonClass("Integer");

        JsonClass valueBoolean = model.newJsonEnumByName(ValueBoolean.class);
        valueBoolean.addCParam("frage", asBoolean, "getFrage");

        JsonClass valueInteger = model.newJsonEnumByName(ValueInteger.class);
        valueInteger.addCParam("zahl", asInteger);

        JsonClass valueString = model.newJsonEnumByName(ValueString.class);
        valueString.addCParam("text", asString);

        JsonInter valueIx = model.newJsonInterface(ValueInterface.class, valueBoolean, valueInteger, valueString);

        testBox = model.newJsonReflect(TestBox.class);
        testBox.addField("one", valueIx);
        testBox.addField("list", valueIx, LIST);
        testBox.addField("arr", valueIx, ARRAY);

    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    public JsonClass getTestBox() {
        return testBox;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
