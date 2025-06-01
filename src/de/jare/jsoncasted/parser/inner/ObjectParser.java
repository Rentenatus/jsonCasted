/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.ParseStreamReader;
import java.io.IOException;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class ObjectParser {

    private final JsonItemDefinition definition;
    private final JsonClass aClass;

    private final JsonClass strClazz;

    public ObjectParser(JsonItemDefinition definition, JsonClass aClass) {
        this.definition = definition;
        strClazz = definition == null ? null : definition.getModel().getJsonClass("String");
        this.aClass = aClass;

    }

    public JsonItem parse(ParseStreamReader psr) throws IOException, JsonParseException {
        JsonObject myObject = new JsonObject(aClass);
        while (psr.hasNext()) {
            JsonValue paramName = null;
            JsonItem paramValue = null;
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == '}') {
                    return myObject;
                }
                if (c == '"') {
                    paramName = new StringParser(definition).parse(psr);
                    while (psr.hasNext() && psr.next() != ':') {
                    }
                    break;
                }
                if (c == ':') {
                    paramName = new JsonValue(sb.toString(), strClazz);
                    break;
                }
                sb.append(c);
            }
            JsonClass castClass = null;
            JsonField field = paramField(paramKey(paramName));
            if (field == null && psr.getDebbugLevel().satisfyWarning()) {
                Logger.getGlobal().log(Level.WARNING, "The field {0} not found in {1}.", new Object[]{
                    paramName == null ? "null" : paramName.getStringValue(), aClass == null ? "null" : aClass.getcName()});
            }
            sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == '}') {
                    appendParam(myObject, paramName, paramValue, sb.toString(), psr.getDebbugLevel());
                    return myObject;
                }
                if (c == '"') {
                    checkDoubleParam(paramValue, paramName, psr.getZeile());
                    paramValue = new StringParser(definition, paramClass(field, null)).parse(psr);
                } else if (c == '[') {
                    checkDoubleParam(paramValue, paramName, psr.getZeile());
                    if (field != null && !field.isAsListOrArray()) {
                        throw new JsonParseException("Field " + field.getfName() + " is not a list nor array. (:" + psr.getZeile() + ")");
                    }
                    paramValue = new ListParser(definition, paramType(field)).parse(psr, field == null || field.isAsList());
                } else if (c == '(') {
                    castClass = new CastingParser(definition, paramType(field)).parse(psr);
                } else if (c == '{') {
                    checkDoubleParam(paramValue, paramName, psr.getZeile());
                    paramValue = new ObjectParser(definition, paramClass(field, castClass)).parse(psr);
                } else if (c == ',') {
                    appendParam(myObject, paramName, paramValue, sb.toString(), psr.getDebbugLevel());
                    break;
                } else {
                    sb.append(c);
                }
            }
        }
        throw new JsonParseException("End of file without end of list.");
    }

    protected void checkDoubleParam(JsonItem paramValue, JsonValue paramName, int zeile) throws JsonParseException {
        if (paramValue != null) {
            String key = paramName == null ? null : paramName.getStringValue();
            key = key == null ? "null" : key.trim();
            throw new JsonParseException("Double param '" + key + "' value. (:" + zeile + ")   Mostly because a \" is missing and string remains open.");
        }
    }

    protected void appendParam(JsonObject myObject, JsonValue paramName, JsonItem paramValue, String alternativ, JsonDebugLevel debbugLevel) {
        String key = paramKey(paramName);
        JsonField field = paramField(key);
        final String cName = aClass == null ? "null" : aClass.getcName();
        if (paramValue == null) {
            myObject.putParam(key, new JsonValue(alternativ.trim(), paramClass(field, null)));
            if (aClass != null && debbugLevel.satisfyInfo()) {
                // nur interesssant, wenn aClass sinnvoll ist.
                Logger.getGlobal().log(Level.INFO, "The key {0} of {1} is set to string {2}.", new Object[]{
                    key, cName, alternativ.trim()});
            }
            return;
        }
        if (field == null) {
            myObject.putParam(key, paramValue);
            if (aClass != null && debbugLevel.satisfyInfo()) {
                // nur interesssant, wenn aClass sinnvoll ist.
                Logger.getGlobal().log(Level.INFO, "The key {0} of {1} is set to {2}.", new Object[]{
                    key, cName, paramValue.getStringValue()});
            }
            return;
        }
        if (field.isAsListOrArray() && !paramValue.isList()) {
            ArrayList<JsonItem> list = new ArrayList<>(1);
            list.add(paramValue);
            myObject.putParam(key, new JsonList(list, field.isAsList(), field.getjType()));
            if (debbugLevel.satisfyWarning()) {
                Logger.getGlobal().log(Level.WARNING, "The field {0} of {1} is a list, but the value is not. Convert the value to a single-element list.", new Object[]{
                    field.getfName(), cName});
            }
            return;
        }
        myObject.putParam(key, paramValue);
        if (debbugLevel.satisfyInfo()) {
            Logger.getGlobal().log(Level.INFO, "The field {0} of {1} is set to {2}.", new Object[]{
                field.getfName(), cName, paramValue.getStringValue()});
        }
    }

    private String paramKey(JsonValue paramName) {
        String key = paramName.getStringValue();
        key = key == null ? "null" : key.trim();
        return key;
    }

    private JsonField paramField(String key) {
        return aClass == null ? null : aClass.get(key);
    }

    private static JsonClass paramClass(JsonField field, JsonClass castClass) {
        return castClass != null
                ? castClass
                : (field == null ? null : field.getjType().getDirectClass());
    }

    private static JsonType paramType(JsonField field) {
        return field == null ? null : field.getjType();
    }

}
