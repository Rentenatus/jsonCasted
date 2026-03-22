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
public class RootParser {

    static JsonNode parse(ParseStreamReader psr) throws IOException, JsonParseException {
        StringBuilder sb = new StringBuilder();
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == '{') {
                return ObjectParser.parse(psr);
            }
            if (c == '[') {
                return ListParser.parse(psr);
            }
            if (c == '"') {
                return JsonNode.stringNode(StringParser.parse(psr));
            }
            if (c == '(') {
                return CastingParser.parse(psr);
            }
            sb.append(c);
        }
        return JsonNode.stringNode(sb.toString());
    }

}
