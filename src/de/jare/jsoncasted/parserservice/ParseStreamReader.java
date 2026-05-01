/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.tools.SimpleStringSplitter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Character stream reader for JSON parsing with buffering and position tracking.
 *
 * <p>This class provides character-by-character reading from a text input stream
 * with the following features:</p>
 * <ul>
 *   <li>Line-by-line buffering</li>
 *   <li>Character position tracking within lines</li>
 *   <li>Line number tracking</li>
 *   <li>Lookahead capability ({@link #view()})</li>
 *   <li>Multi-character consumption ({@link #next(int)})</li>
 *   <li>Quotation mark validation for debugging</li>
 * </ul>
 *
 * <p>This class implements {@link SimpleStringSplitter} for integration with
 * the string splitting utilities.</p>
 *
 * @author Janusch Rentenatus
 */
public class ParseStreamReader implements SimpleStringSplitter {

    private final BufferedReader in;
    private final JsonDebugLevel debugLevel;
    private String line;
    private boolean end;
    private int pos;
    private int zeile;

    /**
     * Constructs a ParseStreamReader with the specified input reader and debug level.
     *
     * @param in the Reader to read from (will be wrapped in a BufferedReader).
     * @param debugLevel the debug level for logging, or {@code null} for default (SIMPLE).
     */
    public ParseStreamReader(Reader in, JsonDebugLevel debugLevel) {
        this.in = new BufferedReader(in);
        this.line = null;
        this.end = false;
        this.pos = 0;
        this.zeile = 0;
        this.debugLevel = debugLevel == null ? JsonDebugLevel.SIMPLE : debugLevel;
    }

    /**
     * Checks if there are more characters available in the stream.
     *
     * <p>This method loads the next non-empty line if the current line is exhausted.</p>
     *
     * @return {@code true} if there are more characters, {@code false} if end of stream.
     * @throws IOException if an I/O error occurs.
     */
    public boolean hasNext() throws IOException {
        if (end) {
            return false;
        }
        if (line == null) {
            line = "";
            while (line.isEmpty()) {
                line = in.readLine();
                zeile++;
                pos = 0;
                if (line == null) {
                    end = true;
                    return false;
                }
                final int simpleCount = simpleCount(line, "\"");
                if ((simpleCount & 1) == 1 && debugLevel.satisfyWarning()) {
                    Logger.getGlobal().log(Level.WARNING, "Line {0} has {1} quotation '\"'.", new Object[]{zeile, simpleCount});
                }
            }
        }
        return true;
    }

    /**
     * Returns the current line number.
     *
     * @return the 1-based line number.
     */
    public int getZeile() {
        return zeile;
    }

    /**
     * Consumes and returns the next character from the stream.
     *
     * @return the next character.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if at end of stream.
     */
    public char next() throws IOException, JsonParseException {
        if (!hasNext()) {
            throw new JsonParseException("End of stream.");
        }
        char ret = line.charAt(pos);
        pos++;
        if (pos >= line.length()) {
            line = null;
        }
        return ret;
    }

    /**
     * Returns the next character without consuming it (lookahead).
     *
     * @return the next character.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if at end of stream.
     */
    public char view() throws IOException, JsonParseException {
        if (!hasNext()) {
            throw new JsonParseException("End of stream.");
        }
        return line.charAt(pos);
    }

    /**
     * Consumes and returns the next specified number of characters.
     *
     * @param count the number of characters to consume.
     * @return a string containing the consumed characters.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if at end of stream.
     */
    public String next(int count) throws IOException, JsonParseException {
        if (!hasNext()) {
            throw new JsonParseException("End of stream.");
        }
        if (pos + count < line.length()) {
            pos += count;
            return line.substring(pos - count, pos);
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(next());
            }
            return sb.toString();
        }
    }

    /**
     * Returns the debug level for this reader.
     *
     * @return the JsonDebugLevel.
     */
    public JsonDebugLevel getDebugLevel() {
        return debugLevel;
    }

}
