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
import java.util.Set;

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
        strategie.writeStart(null, object, null, null, false, false, iString);
        boolean isFollowing = false;
        boolean hasFieldKeys = false;
        try {
            Set<String> keys = object.getParamSet();

            java.util.Iterator<String> it = keys.iterator();
            hasFieldKeys = it.hasNext();
            if (hasFieldKeys) {
                strategie.writeHasFieldKeys(null, object, iString);
            }

            WriteNodePath childIndent = iString.append("  ");
            while (it.hasNext()) {
                final String nextName = it.next();
                JsonItem attr = object.getParam(nextName);

                strategie.writeAttrName(null, isFollowing, nextName, childIndent);
                isFollowing = true;
                writeType(attr, childIndent);
            }
        } finally {
            strategie.writeEnd(null, object, isFollowing, hasFieldKeys, iString);
        }
    }

    protected void writeList(JsonList list, WriteNodePath iString) {
        strategie.writeStartArray(null, list, false, iString);
        boolean isFollowing = false;
        try {
            java.util.Iterator<JsonItem> it = list.listIterator();

            WriteNodePath childIndent = iString.append("  ");
            while (it.hasNext()) {
                JsonItem next = it.next();

                writeType(next, childIndent);
                if (it.hasNext()) {
                    strategie.writeArraySeparator(false, iString);
                }

                isFollowing = true;
            }
        } finally {
            strategie.writeEndArray(list, false, isFollowing, iString);
        }
    }

    protected void writeValue(JsonValue value, WriteNodePath iString) {
        Object val = extractValue(value);
        if (val != null) {
            strategie.writeNodeValue(val, iString);
        } else {
            strategie.writeAttrNull(iString);
        }
    }

    private Object extractValue(JsonValue value) {
        if (value.getStringValue() != null) {
            return '"' + escape(value.getStringValue()) + '"';
        }
        if (value.getBooleanValue() != null) {
            return value.getBooleanValue();
        }
        if (value.getLongValue() != null) {
            return value.getLongValue();
        }
        if (value.getNumberValue() != null) {
            return value.getNumberValue();
        }
        return null;
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
