/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Janusch Rentenatus
 *
 */
public class ListParser {

    static JsonNode parse(ParseStreamReader psr) throws IOException, JsonParseException {
        ArrayList<JsonNode> list = new ArrayList<>();
        JsonNode item = null;
        StringBuilder sb = new StringBuilder();
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == ']') {
                addItem(list, item, sb);
                return JsonNode.arrayNode(list);
            }
            if (c == '(') {
                item = CastingParser.parse(psr);
            } else if (c == '{') {
                item = ObjectParser.parse(psr);
            } else if (c == '"') {
                item = JsonNode.stringNode(StringParser.parse(psr, '"'));
            } else if (c == '\'') {
                item = JsonNode.stringNode(StringParser.parse(psr, '\''));
            } else if (c == ',') {
                addItem(list, item, sb);
                item = null;
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of list.");
    }

    private static void addItem(ArrayList<JsonNode> list, JsonNode item, StringBuilder sb) {
        if (item != null) {
            list.add(item);
            return;
        }
        final String toString = sb.toString().trim();
        if (toString.isEmpty()) {
            return;
        }
        if ("null".equals(toString.trim())) {
            list.add(JsonNode.nullNode());
        } else {
            list.add(JsonNode.varNode(sb.toString()));
        }
    }

}
