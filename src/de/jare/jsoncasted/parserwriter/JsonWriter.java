/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.writer.inner.RootObjectWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonWriter {

    /**
     *
     * @param ob das Objekt.
     * @param definition
     * @param root
     * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
     *         charset}. Zum Beispiel "UTF-8".
     * @return Json String.
     * @throws JsonParseException
     * @throws IOException
     */
    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root, String charsetName) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, root);
        return new String(out.toByteArray(), charsetName);
    }

    public static String writeToString(Object ob, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, root);
        return new String(out.toByteArray());
    }

    public static void write(Object ob, File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, root);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String writeToString(Object ob, JsonItemDefinition definition, String charsetName) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null);
        return new String(out.toByteArray(), charsetName);
    }

    public static String writeToString(Object ob, JsonItemDefinition definition) throws JsonParseException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(ob, out, definition, null);
        return new String(out.toByteArray());
    }

    public static void write(Object ob, File file, JsonItemDefinition definition) throws JsonParseException, IOException {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            write(ob, out, definition, null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void write(Object ob, OutputStream out, JsonItemDefinition definition, JsonClass root) throws IOException, JsonParseException {
        try (PrintWriter prn = new PrintWriter(out)) {
            new RootObjectWriter(definition, root).write(prn, ob);
            prn.flush();
        }
    }

}
