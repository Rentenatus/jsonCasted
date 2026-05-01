/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.IOException;

/**
 * Parser for type-cast syntax in JSON (e.g., `(TypeName){...}`).
 *
 * <p>This class handles the Wood Json Jack extension syntax for explicit type
 * declarations. The syntax allows specifying the concrete type for polymorphic
 * objects:</p>
 * <pre>{@code (ValueStringSubSub){"text": "hello", "frage": true}}</pre>
 *
 * <p>The parser:</p>
 * <ol>
 *   <li>Reads the type name from within parentheses</li>
 *   <li>Expects an object definition (curly braces) to follow</li>
 *   <li>Adds a {@code _class} field to the parsed object with the type name</li>
 * </ol>
 *
 * @author Janusch Rentenatus
 *
 */
public class CastingParser {

    /**
     * Parses a type-cast expression from the stream reader.
     *
     * <p>Example input: {@code (com.example.MyClass){...}}</p>
     *
     * @param psr the ParseStreamReader providing character input.
     * @return a JsonNode representing the object with the type information added.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if parsing fails (e.g., missing opening brace after type).
     */
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
                            ret.put(TERM_CLASS, JsonNode.stringNode(cast));
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
