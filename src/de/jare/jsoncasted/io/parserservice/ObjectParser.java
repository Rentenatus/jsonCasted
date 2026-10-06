/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.io.JsonParseException;
import java.io.IOException;

/**
 * Parser for JSON object structures (key-value pairs enclosed in curly braces).
 *
 * <p>
 * This class handles the parsing of JSON objects with the following syntax:</p>
 * <pre>{@code { "key": value, "key2": value2 }}</pre>
 *
 * <p>
 * Supported value types:</p>
 * <ul>
 * <li>Strings (double or single quoted)</li>
 * <li>Numbers</li>
 * <li>Booleans</li>
 * <li>Null</li>
 * <li>Nested objects</li>
 * <li>Arrays</li>
 * <li>Type-cast expressions (in parentheses)</li>
 * </ul>
 *
 * <p>
 * Keys can be:</p>
 * <ul>
 * <li>Quoted strings (double or single quotes)</li>
 * <li>Unquoted identifiers (accumulated until colon)</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 *
 */
public class ObjectParser {

    /**
     * Parses a JSON object from the stream reader.
     *
     * @param psr the ParseStreamReader providing character input.
     * @return a JsonNode representing the parsed object.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails (e.g., end of file without closing brace).
     */
    static JsonNode parse(ParseStreamReader psr) throws IOException, JsonParseException {
        JsonNode myObject = JsonNode.objectNode();
        while (psr.hasNext()) {
            String paramName = null;
            JsonNode paramValue = null;
            StringBuilder sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == ' ') {
                    continue;
                }
                if (c == '}') {
                    return myObject;
                }
                if (c == '[') {
                    if (!sb.toString().trim().isEmpty()) {
                        throw new JsonParseException(psr.getRow(), "Unexpected characters before an array bracket: '" + sb + "'");
                    }
                    if (paramName != null && !paramName.trim().isEmpty()) {
                        throw new JsonParseException(psr.getRow(), "Unexpected characters before an array bracket: '" + paramName + "'");
                    }
                    if (myObject.asObjectValues() != null && !myObject.asObjectValues().isEmpty()) {
                        throw new JsonParseException(psr.getRow(), "Unexpected characters before an array bracket: '" + paramName + "'");
                    }
                    JsonNode internArr = ListParser.parse(psr);
                    while (psr.hasNext()) {
                        c = psr.next();
                        if (c == ',') {
                            return internArr;
                        }
                    }
                    throw new JsonParseException("End of file without end of list.");
                }
                if (c == '"') {
                    paramName = StringParser.parse(psr, '"');
                    requireDirectColon(psr, paramName);
                    break;
                }
                if (c == '\'') {
                    paramName = StringParser.parse(psr, '\'');
                    requireDirectColon(psr, paramName);
                    break;
                }
                if (c == ':' || c == '=') {
                    paramName = sb.toString();
                    break;
                }
                sb.append(c);
            }

            sb = new StringBuilder();
            while (psr.hasNext()) {
                char c = psr.next();
                if (c == ' ') {
                    continue;
                }
                if (c == '}') {
                    requireCleanValueEnd(psr, paramName, paramValue, sb);
                    appendParam(myObject, paramName, paramValue, sb.toString());
                    return myObject;
                }
                if (c == '"') {
                    requireOpenValue(psr, paramName, paramValue);
                    paramValue = JsonNode.stringNode(StringParser.parse(psr, '"'));
                } else if (c == '\'') {
                    requireOpenValue(psr, paramName, paramValue);
                    paramValue = JsonNode.stringNode(StringParser.parse(psr, '\''));
                } else if (c == '[') {
                    requireOpenValue(psr, paramName, paramValue);
                    paramValue = ListParser.parse(psr);
                } else if (c == '(') {
                    requireOpenValue(psr, paramName, paramValue);
                    paramValue = CastingParser.parse(psr);
                } else if (c == '{') {
                    requireOpenValue(psr, paramName, paramValue);
                    paramValue = ObjectParser.parse(psr);
                } else if (c == ',') {
                    requireCleanValueEnd(psr, paramName, paramValue, sb);
                    appendParam(myObject, paramName, paramValue, sb.toString());
                    break;
                } else {
                    // Content that follows a value without a separating
                    // comma is reported by requireCleanValueEnd at the
                    // entry end - it is no longer silently swallowed.
                    sb.append(c);
                }
            }
        }
        throw new JsonParseException("End of file without end of list.");
    }

    /**
     * Guards against the silent loss of a value that follows a parsed value without a separating comma: once a
     * value was parsed, no further value token may start before the comma closes the entry.
     */
    private static void requireOpenValue(ParseStreamReader psr, String paramName, JsonNode paramValue)
            throws JsonParseException {
        if (paramValue != null) {
            throw new JsonParseException(psr.getRow(),
                    "Missing comma after the value of '" + paramName + "'");
        }
    }

    /**
     * Guards against the silent loss of characters that follow a parsed value without a separating comma: the
     * content (typically the next entry, e.g. port: 11434 right after "host3": "") was silently discarded
     * before - now the parser reports the missing separator. Whitespace alone is legitimate.
     */
    private static void requireCleanValueEnd(ParseStreamReader psr, String paramName, JsonNode paramValue,
            StringBuilder sb) throws JsonParseException {
        if (paramValue != null && !sb.toString().trim().isEmpty()) {
            throw new JsonParseException(psr.getRow(),
                    "Unexpected characters after the value of '" + paramName + "': '"
                    + sb.toString().trim() + "' (missing comma?)");
        }
    }

    /**
     * Guards against the silent swallowing of characters between a quoted key and its colon: only whitespace is
     * legitimate there, anything else was discarded without an error before.
     */
    private static void requireDirectColon(ParseStreamReader psr, String paramName)
            throws java.io.IOException, JsonParseException {
        StringBuilder skipped = new StringBuilder();
        while (psr.hasNext()) {
            char between = psr.next();
            if (between == ':') {
                if (!skipped.toString().trim().isEmpty()) {
                    throw new JsonParseException(psr.getRow(),
                            "Unexpected characters between the key '" + paramName + "' and its colon: '"
                            + skipped.toString().trim() + "'");
                }
                return;
            }
            skipped.append(between);
        }
    }

    /**
     * Adds a parameter to the object node.
     *
     * @param myObject the object node to add the parameter to.
     * @param paramName the parameter name.
     * @param paramValue the parsed parameter value.
     * @param alternativ fallback string value if paramValue is null.
     */
    private static void appendParam(JsonNode myObject, String paramName, JsonNode paramValue, String alternativ) {
        String key = paramKey(paramName);
        myObject.put(key, paramValue != null ? paramValue : JsonNode.varNode(alternativ));
    }

    /**
     * Normalizes a parameter key, using "null" if the input is null.
     *
     * @param paramName the parameter name to normalize.
     * @return the normalized key (trimmed, or "null" if input is null).
     */
    private static String paramKey(final String paramName) {
        return paramName == null ? "null" : paramName.trim();
    }

}
