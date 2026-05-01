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
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserservice.ParseStreamReader;
import java.io.IOException;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Legacy parser for handling JSON type casting.
 * Parses type cast expressions like (ClassName) from the JSON stream.
 *
 * @author Janusch Rentenatus
 * @deprecated Replaced by JsonNode-based parsing pipeline.
 */
@Deprecated
public class CastingParserBak {

    private final JsonItemDefinition definition;
    private final JsonType aType;

    /**
     * Constructs a CastingParserBak instance.
     *
     * @param definition The JSON item definition.
     * @param aType The allowed JSON type for the cast.
     */
    public CastingParserBak(JsonItemDefinition definition, JsonType aType) {
        this.definition = definition;
        this.aType = aType;
    }

    /**
     * Parses a type cast expression from the stream.
     *
     * @param psr The ParseStreamReader to read from.
     * @return The JsonClass corresponding to the cast type name.
     * @throws IOException If I/O errors occur.
     * @throws JsonParseException If parsing fails.
     */
    JsonClass parse(ParseStreamReader psr) throws IOException, JsonParseException {
        while (psr.hasNext()) {
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == ')') {
                    return search(sb, psr.getZeile(), psr.getDebugLevel());
                }
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of cast.");
    }

    /**
     * Searches for a JsonClass by its name.
     *
     * @param sb The StringBuilder containing the class name.
     * @param zeile The line number for error reporting.
     * @param debugLevel The debug level for controlling debug output.
     * @return The found JsonClass.
     * @throws RuntimeException If the class is not found or not allowed.
     */
    private JsonClass search(StringBuilder sb, int zeile, JsonDebugLevel debugLevel) {
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
        if (debugLevel.satisfyInfo()) {
            Logger.getGlobal().log(Level.INFO, "JsonClass {0} found for casting.", new Object[]{cName});
        }
        return found;
    }
}
