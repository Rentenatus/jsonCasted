/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.ParseStreamReader;
import java.io.IOException;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 *
 * @author Janusch Rentenatus
 */
public class StringParser {

    private final JsonClass aClass;

    public StringParser(JsonItemDefinition definition) {
        this.aClass = definition == null ? null : definition.getModel().getJsonClass("String");
    }

    public StringParser(JsonItemDefinition definition, JsonClass aClass) {
        this.aClass = aClass == null
                ? //
                (definition == null ? null : definition.getModel().getJsonClass("String"))
                : aClass;
    }

    JsonValue parse(ParseStreamReader psr) throws IOException, JsonParseException {
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
                return new JsonValue(sb.toString(), aClass);
            }
            escape = c == '\\';
            if (!escape) {
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of string.");
    }

}
