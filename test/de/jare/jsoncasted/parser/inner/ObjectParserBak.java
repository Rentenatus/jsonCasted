/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.debug.DebugTuple;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonListBak;
import de.jare.jsoncasted.item.JsonObjectBak;
import de.jare.jsoncasted.item.JsonValueBak;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.parserservice.ParseStreamReader;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Legacy parser for JSON object structures.
 * Parses JSON objects and their properties from the stream.
 *
 * @author Janusch Rentenatus
 * @deprecated Replaced by JsonNode-based parsing pipeline.
 */
@Deprecated
public class ObjectParserBak {

    private final JsonItemDefinition definition;
    private final JsonClass aClass;

    private final JsonClass strClazz;

    /**
     * Constructs an ObjectParserBak instance.
     *
     * @param definition The JSON item definition.
     * @param aClass The JSON class for the object being parsed.
     */
    public ObjectParserBak(JsonItemDefinition definition, JsonClass aClass) {
        this.definition = definition;
        strClazz = definition == null ? null : definition.getModel().getJsonClass("String");
        this.aClass = aClass;

    }

    /**
     * Parses a JSON object from the stream.
     *
     * @param psr The ParseStreamReader to read from.
     * @return The parsed JsonObjectBak.
     * @throws IOException If I/O errors occur.
     * @throws JsonParseException If parsing fails.
     */
    public JsonItem parse(ParseStreamReader psr) throws IOException, JsonParseException {
        JsonObjectBak myObject = new JsonObjectBak(aClass);
        while (psr.hasNext()) {
            JsonValueBak paramName = null;
            JsonItem paramValue = null;
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == '}') {
                    return myObject;
                }
                if (c == '"') {
                    paramName = new StringParserBak(definition).parse(psr);
                    while (psr.hasNext() && psr.next() != ':') {
                    }
                    break;
                }
                if (c == ':') {
                    paramName = new JsonValueBak(sb.toString(), strClazz);
                    break;
                }
                sb.append(c);
            }
            JsonClass castClass = null;
            JsonField field = paramField(paramKey(paramName));
            if (field == null && psr.getDebugLevel().satisfyWarning()) {
                Logger.getGlobal().log(Level.WARNING, "The field {0} not found in {1}.", new Object[]{
                    paramName == null ? "null" : paramName.getStringValue(), aClass == null ? "null" : aClass.getcName()});
            }
            sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == '}') {
                    appendParam(myObject, paramName, paramValue, sb.toString(), psr.getDebugLevel());
                    return myObject;
                }
                if (c == '"') {
                    checkDoubleParam(paramValue, paramName, psr.getZeile());
                    paramValue = new StringParserBak(definition, paramClass(field, null)).parse(psr);
                } else if (c == '[') {
                    checkDoubleParam(paramValue, paramName, psr.getZeile());
                    if (field != null && !field.isAsListOrArray()) {
                        throw new JsonParseException("Field " + field.getfName() + " is not a list nor array. (:" + psr.getZeile() + ")");
                    }
                    paramValue = new ListParserBak(definition, paramType(field)).parse(psr, field == null || field.isAsList());
                } else if (c == '(') {
                    castClass = new CastingParserBak(definition, paramType(field)).parse(psr);
                } else if (c == '{') {
                    checkDoubleParam(paramValue, paramName, psr.getZeile());
                    paramValue = new ObjectParserBak(definition, paramClass(field, castClass)).parse(psr);
                } else if (c == ',') {
                    appendParam(myObject, paramName, paramValue, sb.toString(), psr.getDebugLevel());
                    break;
                } else {
                    sb.append(c);
                }
            }
        }
        throw new JsonParseException("End of file without end of list.");
    }

    /**
     * Checks for duplicate parameter values.
     *
     * @param paramValue The current parameter value.
     * @param paramName The parameter name.
     * @param zeile The line number for error reporting.
     * @throws JsonParseException If a duplicate parameter is detected.
     */
    protected void checkDoubleParam(JsonItem paramValue, JsonValueBak paramName, int zeile) throws JsonParseException {
        if (paramValue != null) {
            String key = paramName == null ? null : paramName.getStringValue();
            key = key == null ? "null" : key.trim();
            throw new JsonParseException("Double param '" + key + "' value. (:" + zeile + ")   Mostly because a \" is missing and string remains open.");
        }
    }

    /**
     * Appends a parameter to the object being built.
     *
     * @param myObject The JsonObjectBak to append to.
     * @param paramName The parameter name.
     * @param paramValue The parameter value.
     * @param alternativ Alternative string value if paramValue is null.
     * @param debugLevel The debug level for controlling debug output.
     */
    protected void appendParam(JsonObjectBak myObject, JsonValueBak paramName, JsonItem paramValue, String alternativ, JsonDebugLevel debugLevel) {
        String key = paramKey(paramName);
        JsonField field = paramField(key);
        final String cName = aClass == null ? "null" : aClass.getcName();
        if (paramValue == null) {
            myObject.putParam(key, new JsonValueBak(alternativ.trim(), paramClass(field, null)));
            if (aClass != null && debugLevel.satisfyInfo()) {
                // Only interesting if aClass is meaningful.
                Logger.getGlobal().log(Level.INFO, "The key {0} of {1} is set to string {2}.", new Object[]{
                    key, cName, alternativ.trim()});
            }
            return;
        }
        if (field == null) {
            myObject.putParam(key, paramValue);
            if (aClass != null && debugLevel.satisfyInfo()) {
                // Only interesting if aClass is meaningful.
                Logger.getGlobal().log(Level.INFO, "The key {0} of {1} is set to {2}.", new Object[]{
                    key, cName, paramValue.getStringValue()});
            }
            return;
        }
        if (field.isAsListOrArray() && !paramValue.isList()) {
            ArrayList<JsonItem> list = new ArrayList<>(1);
            list.add(paramValue);
            myObject.putParam(key, new JsonListBak(list, field.isAsList(), field.getjType()));
            debugLevel.warning(() -> new DebugTuple(
                    "The field {0} of {1} is a list, but the value is not. Convert the value to a single-element list.",
                    field.getfName(), cName));
            return;
        }
        myObject.putParam(key, paramValue);
        if (debugLevel.satisfyInfo()) {
            Logger.getGlobal().log(Level.INFO, "The field {0} of {1} is set to {2}.", new Object[]{
                field.getfName(), cName, paramValue.getStringValue()});
        }
    }

    /**
     * Extracts the parameter key from a JsonValueBak.
     *
     * @param paramName The JsonValueBak containing the parameter name.
     * @return The trimmed parameter key.
     */
    private String paramKey(JsonValueBak paramName) {
        String key = paramName.getStringValue();
        key = key == null ? "null" : key.trim();
        return key;
    }

    /**
     * Retrieves the JsonField for a given parameter key.
     *
     * @param key The parameter key.
     * @return The JsonField, or null if not found.
     */
    private JsonField paramField(String key) {
        return aClass == null ? null : aClass.get(key);
    }

    /**
     * Determines the JsonClass for a field's parameter.
     *
     * @param field The JsonField.
     * @param castClass The cast class from parsing, or null.
     * @return The JsonClass to use for the parameter.
     */
    private static JsonClass paramClass(JsonField field, JsonClass castClass) {
        return castClass != null
                ? castClass
                : (field == null ? null : field.getjType().getDirectClass());
    }

    /**
     * Retrieves the JsonType for a field's parameter.
     *
     * @param field The JsonField.
     * @return The JsonType, or null if field is null.
     */
    private static JsonType paramType(JsonField field) {
        return field == null ? null : field.getjType();
    }

}
