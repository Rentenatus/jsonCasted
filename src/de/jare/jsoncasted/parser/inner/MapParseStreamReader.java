/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parser.inner;

import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.ParseStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * Adapter that exposes a Map<String,Object> as a ParseStreamReader by
 * serializing the Map to an in-memory JSON string. This allows reusing the
 * existing inner parsers (RootParser/ObjectParser/ListParser) without
 * duplicating parsing logic.
 */
public class MapParseStreamReader extends ParseStreamReader {

    public MapParseStreamReader(Map<String, Object> root, JsonDebugLevel debbugLevel) {
        super(new StringReader(mapToJson(root)), debbugLevel);
    }

    private static String mapToJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        appendJson(obj, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendJson(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
            return;
        }
        if (obj instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (java.util.Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"');
                sb.append(escapeJsonString(e.getKey()));
                sb.append('"');
                sb.append(':');
                appendJson(e.getValue(), sb);
            }
            sb.append('}');
            return;
        }
        if (obj instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object o : (List<Object>) obj) {
                if (!first) sb.append(',');
                first = false;
                appendJson(o, sb);
            }
            sb.append(']');
            return;
        }
        if (obj instanceof String) {
            sb.append('"');
            sb.append(escapeJsonString((String) obj));
            sb.append('"');
            return;
        }
        if (obj instanceof Number) {
            sb.append(obj.toString());
            return;
        }
        if (obj instanceof Boolean) {
            sb.append(obj.toString());
            return;
        }
        // Fallback: use toString quoted
        sb.append('"');
        sb.append(escapeJsonString(obj.toString()));
        sb.append('"');
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
