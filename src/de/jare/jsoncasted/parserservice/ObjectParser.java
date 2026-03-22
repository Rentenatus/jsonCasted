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

/**
 *
 * @author Janusch Rentenatus
 *
 */
public class ObjectParser {

    static JsonNode parse(ParseStreamReader psr) throws IOException, JsonParseException {
        JsonNode myObject = JsonNode.objectNode();
        while (psr.hasNext()) {
            String paramName = null;
            JsonNode paramValue = null;
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == '}') {
                    return myObject;
                }
                if (c == '"') {
                    paramName = StringParser.parse(psr);
                    while (psr.hasNext() && psr.next() != ':') {
                    }
                    break;
                }
                if (c == ':') {
                    paramName = sb.toString();
                    break;
                }
                sb.append(c);
            }

            sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == '}') {
                    appendParam(myObject, paramName, paramValue, sb.toString());
                    return myObject;
                }
                if (c == '"') {
                    paramValue = JsonNode.stringNode(StringParser.parse(psr));
                } else if (c == '[') {

                    paramValue = ListParser.parse(psr);
                } else if (c == '(') {
                    paramValue = CastingParser.parse(psr);
                } else if (c == '{') {
                    paramValue = ObjectParser.parse(psr);
                } else if (c == ',') {
                    appendParam(myObject, paramName, paramValue, sb.toString());
                    break;
                } else {
                    sb.append(c);
                }
            }
        }
        throw new JsonParseException("End of file without end of list.");
    }

    private static void appendParam(JsonNode myObject, String paramName, JsonNode paramValue, String alternativ) {
        String key = paramKey(paramName);
        myObject.put(key, paramValue != null ? paramValue : JsonNode.stringNode(alternativ));
    }

    private static String paramKey(final String paramName) {
        return paramName == null ? "null" : paramName.trim();
    }

}
