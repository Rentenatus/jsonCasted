package de.jare.jsoncasted.parserservice;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;

import java.io.*;
import java.util.*;

/**
 * Simple recursive-descent JSON parser service. Provides overloads for
 * StringReader, FileReader and BufferedReader and returns a JsonNode.
 */
public class JsonParserService {

    public JsonNode parse(StringReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public JsonNode parse(FileReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public JsonNode parse(BufferedReader reader) throws IOException, JsonParseException {
        return parse((Reader) reader);
    }

    public JsonNode parse(Reader reader) throws IOException, JsonParseException, JsonParseException {
        ParseStreamReader psr = new ParseStreamReader(reader, JsonDebugLevel.SIMPLE);
        return RootParser.parse(psr);

    }

    // Basic parser implementation
    private static class Parser {

        private final String in;
        private int pos = 0;

        Parser(String in) {
            this.in = in;
        }

        JsonNode parse() throws JsonParseException {
            skipWhitespace();
            JsonNode v = parseValue();
            return v;
        }

        boolean isAtEnd() {
            return pos >= in.length();
        }

        void skipWhitespace() {
            while (!isAtEnd()) {
                char c = in.charAt(pos);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        JsonNode parseValue() throws JsonParseException {
            skipWhitespace();
            if (isAtEnd()) {
                throw new JsonParseException("Unexpected end of input");
            }
            char c = in.charAt(pos);
            switch (c) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return JsonNode.stringNode(parseString());
                case 't':
                    expectLiteral("true");
                    return JsonNode.booleanNode(true);
                case 'f':
                    expectLiteral("false");
                    return JsonNode.booleanNode(false);
                case 'n':
                    expectLiteral("null");
                    return JsonNode.nullNode();
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return JsonNode.numberNode(parseNumber());
                    }
                    throw new JsonParseException("Unexpected character '" + c + "' at position " + pos);
            }
        }

        void expectLiteral(String lit) throws JsonParseException {
            if (in.startsWith(lit, pos)) {
                pos += lit.length();
            } else {
                throw new JsonParseException("Expected literal '" + lit + "' at position " + pos);
            }
        }

        JsonNode parseObject() throws JsonParseException {
            // consume '{'
            pos++;
            JsonNode obj = JsonNode.objectNode();
            skipWhitespace();
            if (!isAtEnd() && in.charAt(pos) == '}') {
                pos++;
                return obj;
            }
            while (true) {
                skipWhitespace();
                if (isAtEnd()) {
                    throw new JsonParseException("Unexpected end in object at position " + pos);
                }
                String key;
                char kc = in.charAt(pos);
                if (kc == '"') {
                    key = parseString();
                } else {
                    // accept unquoted identifier keys (legacy/config style)
                    int nameStart = pos;
                    while (!isAtEnd()) {
                        char c = in.charAt(pos);
                        if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '$') {
                            pos++;
                        } else {
                            break;
                        }
                    }
                    if (pos == nameStart) {
                        throw new JsonParseException("Expected key at position " + pos);
                    }
                    key = in.substring(nameStart, pos);
                }
                skipWhitespace();

                // Accept ':' (standard JSON) or '=' for legacy-cast pattern detection
                if (isAtEnd()) {
                    throw new JsonParseException("Expected ':' after key at position " + pos);
                }
                char sep = in.charAt(pos);
                if (sep == ':' || sep == '=') {
                    pos++; // consume separator
                } else {
                    throw new JsonParseException("Expected ':' after key at position " + pos);
                }

                skipWhitespace();

                // Detect legacy cast syntax: (ClassName) { ... }
                int savePos = pos;
                String legacyClass = null;
                if (!isAtEnd() && in.charAt(pos) == '(') {
                    pos++; // consume '('
                    skipWhitespace();
                    int nameStart = pos;
                    while (!isAtEnd()) {
                        char c = in.charAt(pos);
                        if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '$') {
                            pos++;
                        } else {
                            break;
                        }
                    }
                    if (pos > nameStart) {
                        String className = in.substring(nameStart, pos);
                        skipWhitespace();
                        if (!isAtEnd() && in.charAt(pos) == ')') {
                            pos++; // consume ')'
                            skipWhitespace();
                            if (!isAtEnd() && in.charAt(pos) == '{') {
                                legacyClass = className;
                            } else {
                                // not followed by object literal -> revert
                                pos = savePos;
                            }
                        } else {
                            pos = savePos; // invalid, revert
                        }
                    } else {
                        pos = savePos; // empty name, revert
                    }
                }

                // If separator was '=' but no legacy cast detected, treat as error to avoid accepting '=' as general separator
                if (sep == '=' && legacyClass == null) {
                    throw new JsonParseException("Unexpected '=' separator at position " + (pos - 1));
                }

                JsonNode value = parseValue();

                // If legacy cast was used and value is an object, inject _class if not present
                if (legacyClass != null && value != null && value.getType() == JsonNodeType.OBJECT) {
                    Map<String, JsonNode> map = value.asObject();
                    if (!map.containsKey("_class")) {
                        value.put("_class", JsonNode.stringNode(legacyClass));
                    }
                }

                obj.put(key, value);
                skipWhitespace();
                if (isAtEnd()) {
                    throw new JsonParseException("Unexpected end in object at position " + pos);
                }
                char ch = in.charAt(pos);
                if (ch == ',') {
                    pos++;
                    skipWhitespace();
                    if (!isAtEnd() && in.charAt(pos) == '}') {
                        pos++;
                        break;
                    } else {
                        continue;
                    }
                }
                if (ch == '}') {
                    pos++;
                    break;
                }
                // permissive: allow missing commas between object entries (legacy/config style)
                if (ch == '"' || Character.isLetter(ch) || ch == '_' || ch == '$') {
                    // treat as start of next key without requiring a comma
                    continue;
                }
                throw new JsonParseException("Expected ',' or '}' in object at position " + pos);
            }
            return obj;
        }

        JsonNode parseArray() throws JsonParseException {
            pos++; // consume '['
            JsonNode arr = JsonNode.arrayNode();
            skipWhitespace();
            if (!isAtEnd() && in.charAt(pos) == ']') {
                pos++;
                return arr;
            }
            while (true) {
                skipWhitespace();
                JsonNode elem = parseValue();
                arr.add(elem);
                skipWhitespace();
                if (isAtEnd()) {
                    throw new JsonParseException("Unexpected end in array at position " + pos);
                }
                char ch = in.charAt(pos);
                if (ch == ',') {
                    pos++;
                    skipWhitespace();
                    if (!isAtEnd() && in.charAt(pos) == ']') {
                        pos++;
                        break;
                    } else {
                        continue;
                    }
                }
                if (ch == ']') {
                    pos++;
                    break;
                }
                throw new JsonParseException("Expected ',' or ']' in array at position " + pos);
            }
            return arr;
        }

        String parseString() throws JsonParseException {
            // assumes current char is '"'
            pos++; // skip opening
            StringBuilder sb = new StringBuilder();
            while (!isAtEnd()) {
                char c = in.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (isAtEnd()) {
                        throw new JsonParseException("Unterminated escape in string");
                    }
                    char e = in.charAt(pos++);
                    switch (e) {
                        case '"':
                            sb.append('"');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '/':
                            sb.append('/');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'u':
                            if (pos + 4 > in.length()) {
                                throw new JsonParseException("Invalid unicode escape");
                            }
                            String hex = in.substring(pos, pos + 4);
                            try {
                                int code = Integer.parseInt(hex, 16);
                                sb.append((char) code);
                                pos += 4;
                            } catch (NumberFormatException ex) {
                                throw new JsonParseException("Invalid unicode escape: " + hex);
                            }
                            break;
                        default:
                            throw new JsonParseException("Invalid escape character: " + e);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new JsonParseException("Unterminated string starting at position " + (pos - sb.length() - 1));
        }

        double parseNumber() throws JsonParseException {
            int start = pos;
            if (in.charAt(pos) == '-') {
                pos++;
            }
            if (isAtEnd()) {
                throw new JsonParseException("Unexpected end parsing number");
            }
            if (in.charAt(pos) == '0') {
                pos++;
            } else if (in.charAt(pos) >= '1' && in.charAt(pos) <= '9') {
                while (!isAtEnd() && Character.isDigit(in.charAt(pos))) {
                    pos++;
                }
            } else {
                throw new JsonParseException("Invalid number at position " + pos);
            }
            if (!isAtEnd() && in.charAt(pos) == '.') {
                pos++;
                if (isAtEnd() || !Character.isDigit(in.charAt(pos))) {
                    throw new JsonParseException("Invalid fractional part in number at " + pos);
                }
                while (!isAtEnd() && Character.isDigit(in.charAt(pos))) {
                    pos++;
                }
            }
            if (!isAtEnd() && (in.charAt(pos) == 'e' || in.charAt(pos) == 'E')) {
                pos++;
                if (!isAtEnd() && (in.charAt(pos) == '+' || in.charAt(pos) == '-')) {
                    pos++;
                }
                if (isAtEnd() || !Character.isDigit(in.charAt(pos))) {
                    throw new JsonParseException("Invalid exponent in number at " + pos);
                }
                while (!isAtEnd() && Character.isDigit(in.charAt(pos))) {
                    pos++;
                }
            }
            String numStr = in.substring(start, pos);
            try {
                return Double.parseDouble(numStr);
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Invalid number format: " + numStr);
            }
        }
    }
}
