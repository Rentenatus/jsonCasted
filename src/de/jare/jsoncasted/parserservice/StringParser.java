/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.IOException;

/**
 *
 * @author Janusch Rentenatus
 *
 */
public class StringParser {

    static String parse(ParseStreamReader psr) throws IOException, JsonParseException {
        boolean escape = false;
        StringBuilder sb = new StringBuilder();

        while (psr.hasNext()) {
            char c = psr.next();
            if (escape) {
                escape = false;
                if (c == 'u') {
                    sb.append((char) Integer.parseInt(psr.next(4), 16));
                } else if (c == 'r') {
                    sb.append('\r');
                } else if (c == 'f') {
                    sb.append('\f');
                } else if (c == 't') {
                    sb.append('\t');
                } else if (c == 'n') {
                    sb.append('\n');
                } else if (c == 'b') {
                    sb.append('\b');
                } else {
                    sb.append('\\').append(c);
                }

                continue;
            }
            if (c == '"') {
                return sb.toString();
            }
            escape = c == '\\';
            if (!escape) {
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of string.");
    }

}
