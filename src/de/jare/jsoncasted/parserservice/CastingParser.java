/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
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
public class CastingParser {

    static JsonNode parse(ParseStreamReader psr) throws IOException, JsonParseException {
        while (psr.hasNext()) {
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == ')') {
                    String cast = sb.toString().trim();
                    while (psr.hasNext()) {
                        c = psr.next();
                        if (c == '{') {
                            JsonNode ret = ObjectParser.parse(psr);
                            ret.put("_class", JsonNode.stringNode(cast));
                            return ret;
                        }
                        if (c == ' ' || c == '\n' || c == '\t') {
                            continue;
                        }
                        throw new JsonParseException("After the end of the cast, '{' must follow for the object definition.");
                    }
                }
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of cast.");
    }

}
