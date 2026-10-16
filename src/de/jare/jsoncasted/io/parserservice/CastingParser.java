/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_OBJECT_ID;
import de.jare.jsoncasted.io.JsonParseException;
import java.io.IOException;

/**
 * Parser for type-cast syntax in JSON (e.g., `(TypeName){...}`).
 *
 * <p>
 * This class handles the Wood Json Jack extension syntax for explicit type declarations. The syntax allows specifying
 * the concrete type for polymorphic objects:</p>
 * <pre>{@code (ValueStringSubSub){"text": "hello", "frage": true}}</pre>
 *
 * <p>
 * An optional wood id in angle brackets may follow the cast before the object literal:</p>
 * <pre>{@code (ValueStringSubSub)<"56">{"text": "hello"}}</pre>
 *
 * <p>
 * The parser:</p>
 * <ol>
 * <li>Reads the type name from within parentheses</li>
 * <li>Reads an optional wood id ({@code <"56">})</li>
 * <li>Expects an object definition (curly braces) to follow</li>
 * <li>Adds a {@code _class} field and, when present, a {@code _woodObjectId} field to the parsed object</li>
 * </ol>
 *
 * @author Janusch Rentenatus
 *
 */
public class CastingParser {

    /**
     * Parses a type-cast expression from the stream reader.
     *
     * <p>
     * Example input: {@code (com.example.MyClass){...}}</p>
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
                    return parseCastTarget(psr, cast);
                }
                sb.append(c);
            }
        }
        throw new JsonParseException("End of file without end of cast.");
    }

    /**
     * Parses the object literal behind the cast, with an optional wood id in between: {@code (Type)<"56">{...}}.
     *
     * @param psr the ParseStreamReader positioned right after the closing parenthesis.
     * @param cast the type name read from the parentheses.
     * @return the parsed object with the cast and, when present, the wood id attached.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException when anything but an optional id and the object literal follows the cast.
     */
    private static JsonNode parseCastTarget(ParseStreamReader psr, String cast) throws IOException, JsonParseException {
        String woodId = null;
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                continue;
            }
            if (c == '<') {
                if (woodId != null) {
                    throw new JsonParseException(psr.getRow(), "Only one wood id may follow a cast.");
                }
                woodId = WoodIdParser.parseId(psr);
                continue;
            }
            if (c == '{') {
                JsonNode ret = ObjectParser.parse(psr);
                JsonNode classNode = JsonNode.stringNode(cast);
                classNode.withSourcePosition(psr.getRow(), psr.getPos());
                ret.put(TERM_CLASS, classNode);
                if (woodId != null) {
                    JsonNode idNode = JsonNode.stringNode(woodId);
                    idNode.withSourcePosition(psr.getRow(), psr.getPos());
                    ret.put(TERM_WOOD_OBJECT_ID, idNode);
                }
                return ret;
            }
            throw new JsonParseException(psr.getRow(), "After the end of the cast, '{' must follow for the object definition.");
        }
        throw new JsonParseException("End of file without end of cast.");
    }

}
