/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

/**
 *
 * @author Janusch Renteantus
 */
public record WriteNodePath(String path) {

    public WriteNodePath append(String... text) {
        StringBuilder sb = new StringBuilder(path);
        for (String item : text) {
            sb.append(item);
        }
        return new WriteNodePath(sb.toString());
    }

    @Override
    public String toString() {
        return path;
    }

    /**
     * Escapes special characters in a string for JSON output. Handles backslash, quote, newline, carriage return, and
     * tab characters.
     *
     * @param s The string to escape.
     * @return The escaped string safe for JSON output.
     */
    static String escape(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
