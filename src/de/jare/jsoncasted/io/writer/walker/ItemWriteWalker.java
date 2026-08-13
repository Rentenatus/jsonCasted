/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.JsonValue;

/**
 *
 * @author Janusch Renteantus
 */
public class ItemWriteWalker {

    final WriteNodePath intentPath;
    private final WriteStrategy strategie;

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     */
    public ItemWriteWalker(WriteStrategy strategie) {
        this.strategie = strategie;
        this.intentPath = new WriteNodePath("");
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param intentPath
     */
    public ItemWriteWalker(WriteStrategy strategie, WriteNodePath intentPath) {
        this.strategie = strategie;
        this.intentPath = intentPath;
    }

    /**
     * Constructs an ObjectWriter instance with a specified indentation string.
     *
     * @param strategie
     * @param intentString
     */
    public ItemWriteWalker(WriteStrategy strategie, String intentString) {
        this.strategie = strategie;
        this.intentPath = new WriteNodePath(intentString);
    }

    /**
     * Writes a JsonItem structure as JSON.
     *
     * @param item The JsonItem to write.
     */
    public void writeType(JsonItem item) {
        writeType(item, intentPath);
    }

    /**
     * Writes a JsonItem structure as JSON with a specified indentation.
     *
     * @param item The JsonItem to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeType(JsonItem item, WriteNodePath iString) {
        if (item == null) {
            strategie.writeAttrNull(iString);
            return;
        }
        if (item instanceof JsonObject object) {
            writeObject(object, iString);
        } else if (item instanceof JsonList list) {
            writeList(list, iString);
        } else if (item instanceof JsonValue value) {
            writeValue(value, iString);
        }
    }

    protected void writeObject(JsonObject object, WriteNodePath iString) {
        // Todo, see writeNodeObject
    }

    protected void writeList(JsonList list, WriteNodePath iString) {
        // Todo, see writeNodeObject
    }

    protected void writeValue(JsonValue value, WriteNodePath iString) {
        // Todo, see writeNodeObject
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
