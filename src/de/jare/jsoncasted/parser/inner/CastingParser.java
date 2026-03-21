/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.ParseStreamReader;
import java.io.IOException;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class CastingParser {

    private final JsonItemDefinition definition;
    private final JsonType aType;

    public CastingParser(JsonItemDefinition definition, JsonType aType) {
        this.definition = definition;
        this.aType = aType;
    }

    JsonClass parse(ParseStreamReader psr) throws IOException, JsonParseException {
        while (psr.hasNext()) {
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == ')') {
                    return search(sb, psr.getZeile(), psr.getDebbugLevel());
                }
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of cast.");
    }

    private JsonClass search(StringBuilder sb, int zeile, JsonDebugLevel debbugLevel) {
        String cName = sb.toString().trim();
        JsonClass found = definition.getModel().getJsonClass(cName);
        if (found == null) {
            found = definition.getModel().getEndsWith(cName);
        }
        if (found == null) {
            throw new RuntimeException("JsonClass '" + cName + "' not found. (:" + zeile + ")");
        }
        if (aType != null && !aType.contains(found)) {
            throw new RuntimeException("JsonClass '" + cName + "' not allowed. (:" + zeile + ")");
        }
        if (debbugLevel.satisfyInfo()) {
            Logger.getGlobal().log(Level.INFO, "JsonClass '{0}' found for casting.", new Object[]{cName});
        }
        return found;
    }
}
