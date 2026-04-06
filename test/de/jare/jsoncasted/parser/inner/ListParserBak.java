/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonListBak;
import de.jare.jsoncasted.item.JsonValueBak;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserservice.ParseStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 *
 * @author Janusch Rentenatus
 *
 * @deprecated Replaced by JsonNode-based parsing pipeline.
 */
@Deprecated
public class ListParserBak {

    private final JsonItemDefinition definition;
    private final JsonType jType;
    private JsonClass castClass;
    private final ArrayList<JsonItem> list;

    public ListParserBak(JsonItemDefinition definition, JsonType jType) {
        this.definition = definition;
        this.jType = jType;
        this.list = new ArrayList<>();
        this.castClass = null;
    }

    public JsonItem parse(ParseStreamReader psr, boolean asList) throws IOException, JsonParseException {
        castClass = (jType == null) ? castClass : jType.getDirectClass();
        JsonItem item = null;
        StringBuilder sb = new StringBuilder();
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == ']') {
                addItem(item, sb);
                return new JsonListBak(list, asList, jType);
            }
            if (c == '(') {
                castClass = new CastingParserBak(definition, jType).parse(psr);
            } else if (c == '{') {
                item = new ObjectParserBak(definition, castClass).parse(psr);
            } else if (c == '"') {
                item = new StringParserBak(definition, castClass).parse(psr);
            } else if (c == ',') {
                addItem(item, sb);
                item = null;
                sb = new StringBuilder();
                if (jType != null) {
                    castClass = jType.getDirectClass();
                }
            } else {
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of list.");
    }

    private void addItem(JsonItem item, StringBuilder sb) {
        if (item != null) {
            list.add(item);
            return;
        }
        final String toString = sb.toString().trim();
        if (toString.isEmpty()) {
            return;
        }
        if ("null".equals(toString.trim())) {
            list.add(new JsonValueBak(toString, null));
        } else if (castClass != null) {
            list.add(new JsonValueBak(toString, castClass));
        } else {
            throw new RuntimeException("JsonClass not found.");
        }
    }

}
