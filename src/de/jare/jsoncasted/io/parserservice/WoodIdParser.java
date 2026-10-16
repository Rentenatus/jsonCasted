/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.COLONCOLON;
import static de.jare.jsoncasted.lang.JsonTerms.PREFIX_THIS;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_OBJECT_ID;
import de.jare.jsoncasted.io.JsonParseException;
import java.io.IOException;

/**
 * Parser for the wood id notation in angle brackets (e.g., {@code <"56">}).
 *
 * <p>
 * This class handles the Wood Json Jack extension syntax for identities and references. The id may prefix a literal
 * or stand alone as a reference stub:</p>
 * <pre>{@code
 * (Profile)<"56">{ ... }    // cast and id before an object
 * <"56">{ ... }             // id before an object - sugar for the _woodObjectId member
 * <"56">[ ... ]             // id before an array - first class identity on the node
 * <"56">                    // bare id - reference stub (_woodLink)
 * }</pre>
 *
 * <p>
 * The id content is preferably quoted ({@code <"56">}); an unquoted token ({@code <56>}) is accepted as a tolerant
 * fallback and yields the same text. A bare id without a provider becomes a local reference ({@code this::56}); a
 * content with {@code ::} is kept as cross resource key.</p>
 *
 * @author Janusch Rentenatus
 */
public class WoodIdParser {

    /**
     * Parses a wood id expression from the stream reader, positioned right after the opening '<'. When a literal
     * follows, the id attaches to it; otherwise a bare reference stub is built.
     *
     * @param psr the ParseStreamReader providing character input.
     * @return the node with the attached identity, or a reference stub.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if parsing fails.
     */
    static JsonNode parse(ParseStreamReader psr) throws IOException, JsonParseException {
        final String id = parseId(psr);
        while (psr.hasNext()) {
            final char c = psr.view();
            if (c == ' ' || c == '\t') {
                psr.next();
                continue;
            }
            break;
        }
        if (psr.hasNext()) {
            final char c = psr.view();
            if (c == '(') {
                throw new JsonParseException(psr.getRow(),
                        "After a wood id, '(' is not allowed. The cast comes first: (Type)<id>{...}");
            }
            if (c == '{') {
                psr.next();
                JsonNode obj = ObjectParser.parse(psr);
                JsonNode idNode = JsonNode.stringNode(id);
                idNode.withSourcePosition(psr.getRow(), psr.getPos());
                return obj.put(TERM_WOOD_OBJECT_ID, idNode);
            }
            if (c == '[') {
                psr.next();
                final JsonNode array = ListParser.parse(psr);
                array.setWoodObjectId(id);
                return array;
            }
        }
        final JsonNode stub = JsonNode.objectNode();
        stub.withSourcePosition(psr.getRow(), psr.getPos());
        JsonNode linkNode = JsonNode.stringNode(localizeKey(id));
        linkNode.withSourcePosition(psr.getRow(), psr.getPos());
        stub.put(TERM_WOOD_LINK, linkNode);
        return stub;
    }

    /**
     * Reads the id content between the angle brackets, positioned right after the opening '<'. Quoted and unquoted
     * contents are both accepted and return the same text.
     *
     * @param psr the ParseStreamReader providing character input.
     * @return the raw id text (never empty).
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if the id is malformed or empty.
     */
    static String parseId(ParseStreamReader psr) throws IOException, JsonParseException {
        final StringBuilder sb = new StringBuilder();
        while (psr.hasNext()) {
            final char c = psr.next();
            if (c == '>') {
                return requireNonEmpty(sb.toString().trim(), psr);
            }
            if (c == '"' || c == '\'') {
                if (sb.length() > 0) {
                    throw new JsonParseException(psr.getRow(), "Unexpected quote inside a wood id.");
                }
                return readQuotedId(psr, c);
            }
            sb.append(c);
        }
        throw new JsonParseException("End of file without end of wood id.");
    }

    /**
     * Reads a quoted id content and expects the closing angle bracket behind it.
     */
    private static String readQuotedId(ParseStreamReader psr, char quotation) throws IOException, JsonParseException {
        final String content = StringParser.parse(psr, quotation);
        while (psr.hasNext()) {
            final char c = psr.next();
            if (c == ' ' || c == '\t') {
                continue;
            }
            if (c == '>') {
                return requireNonEmpty(content, psr);
            }
            throw new JsonParseException(psr.getRow(), "Only '>' may follow a quoted wood id.");
        }
        throw new JsonParseException("End of file without end of wood id.");
    }

    /**
     * Rejects an empty id.
     */
    private static String requireNonEmpty(String id, ParseStreamReader psr) throws JsonParseException {
        if (id == null || id.isEmpty()) {
            throw new JsonParseException(psr.getRow(), "A wood id must not be empty.");
        }
        return id;
    }

    /**
     * Extends a bare id to a local reference key; contents with a provider stay as they are.
     */
    private static String localizeKey(String content) {
        return content.contains(COLONCOLON) ? content : PREFIX_THIS + content;
    }

}
