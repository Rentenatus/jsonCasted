/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonValueBak;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserservice.ParseStreamReader;
import java.io.IOException;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 * Legacy root parser that delegates to specific parsers based on JSON structure.
 * Parses JSON from the root level and dispatches to appropriate parsers.
 *
 * @author Janusch Rentenatus
 * @deprecated Replaced by JsonNode-based parsing pipeline.
 */
@Deprecated
public class RootParserBak {

    private final JsonItemDefinition definition;
    private final JsonType jType;
    private JsonClass castClass;
    private final StringBuilder sb;

    /**
     * Constructs a RootParserBak instance.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type for the root element.
     */
    public RootParserBak(JsonItemDefinition definition, JsonType jType) {
        this.definition = definition;
        this.jType = jType;
        this.sb = new StringBuilder();
        this.castClass = null;
    }

    /**
     * Constructs a RootParserBak instance with a class name.
     *
     * @param definition The JSON item definition.
     * @param className The name of the root class.
     * @throws IllegalArgumentException If the type name is not found.
     */
    public RootParserBak(JsonItemDefinition definition, String className) {
        this.definition = definition;
        this.jType = definition.getModel().getJsonClass(className);
        if (this.jType == null) {
            throw new IllegalArgumentException("Type name " + className + " not found.");
        }
        this.sb = new StringBuilder();
        this.castClass = null;
    }

    /**
     * Constructs a RootParserBak instance with a Class object.
     *
     * @param definition The JSON item definition.
     * @param clazz The Class to use as the root.
     */
    public RootParserBak(JsonItemDefinition definition, Class<?> clazz) {
        this(definition, clazz.getTypeName());
    }

    /**
     * Parses JSON from the stream at the root level.
     *
     * @param psr The ParseStreamReader to read from.
     * @return The parsed JsonItem.
     * @throws IOException If I/O errors occur.
     * @throws JsonParseException If parsing fails.
     */
    public JsonItem parse(ParseStreamReader psr) throws IOException, JsonParseException {
        castClass = (jType == null) ? castClass : jType.getDirectClass();
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == '{') {
                return new ObjectParserBak(definition, castClass).parse(psr);
            }
            if (c == '[') {
                return new ListParserBak(definition, castClass == null ? jType : castClass).parse(psr, true);
            }
            if (c == '"') {
                return new StringParserBak(definition, castClass).parse(psr);
            }
            if (c == '(') {
                castClass = new CastingParserBak(definition, jType).parse(psr);
            } else {
                sb.append(c);
            }
        }
        return new JsonValueBak(sb.toString(), castClass);
    }

}
