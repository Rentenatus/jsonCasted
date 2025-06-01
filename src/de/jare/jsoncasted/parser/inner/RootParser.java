/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.ParseStreamReader;
import java.io.IOException;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 *
 * @author Janusch Rentenatus
 */
public class RootParser {

    private final JsonItemDefinition definition;
    private final JsonType jType;
    private JsonClass castClass;
    private final StringBuilder sb;

    public RootParser(JsonItemDefinition definition, JsonType jType) {
        this.definition = definition;
        this.jType = jType;
        this.sb = new StringBuilder();
        this.castClass = null;
    }

    public RootParser(JsonItemDefinition definition, String className) {
        this.definition = definition;
        this.jType = definition.getModel().getJsonClass(className);
        if (this.jType == null) {
            throw new IllegalArgumentException("Typa name " + className + " not found.");
        }
        this.sb = new StringBuilder();
        this.castClass = null;
    }

    public RootParser(JsonItemDefinition definition, Class<?> clazz) {
        this(definition, clazz.getTypeName());
    }

    public JsonItem parse(ParseStreamReader psr) throws IOException, JsonParseException {
        castClass = (jType == null) ? castClass : jType.getDirectClass();
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == '{') {
                return new ObjectParser(definition, castClass).parse(psr);
            }
            if (c == '[') {
                return new ListParser(definition, castClass == null ? jType : castClass).parse(psr, true);
            }
            if (c == '"') {
                return new StringParser(definition, castClass).parse(psr);
            }
            if (c == '(') {
                castClass = new CastingParser(definition, jType).parse(psr);
            } else {
                sb.append(c);
            }
        }
        return new JsonValue(sb.toString(), castClass);
    }

}
