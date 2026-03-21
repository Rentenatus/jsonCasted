/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.tools.SimpleStringSplitter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class ParseStreamReader implements SimpleStringSplitter {

    private final BufferedReader in;
    private final JsonDebugLevel debbugLevel;
    private String line;
    private boolean end;
    private int pos;
    private int zeile;

    ParseStreamReader(Reader in, JsonDebugLevel debbugLevel) {
        this.in = new BufferedReader(in);
        this.line = null;
        this.end = false;
        this.pos = 0;
        this.zeile = 0;
        this.debbugLevel = debbugLevel == null ? JsonDebugLevel.SIMPLE : debbugLevel;
    }

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
                if ((simpleCount & 1) == 1 && debbugLevel.satisfyWarning()) {
                    Logger.getGlobal().log(Level.WARNING, "Line {0} has {1} quotation ''\"''.", new Object[]{zeile, simpleCount});
                }
            }
        }
        return true;
    }

    public int getZeile() {
        return zeile;
    }

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

    public char view() throws IOException, JsonParseException {
        if (!hasNext()) {
            throw new JsonParseException("End of stream.");
        }
        return line.charAt(pos);
    }

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

    public JsonDebugLevel getDebbugLevel() {
        return debbugLevel;
    }

}
