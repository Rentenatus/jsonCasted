/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.pconvertservice.JsonNodeConverter;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserservice.JsonParserService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonParser {

    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(s);
        return parse(res.getRoot(), descriptor, root, debbugLevel);
    }

    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(file);
        return parse(res.getRoot(), descriptor, root, debbugLevel);
    }

    public static JsonItem parse(final URL url, JsonModelDescriptor descriptor, String root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(url);
        return parse(res.getRoot(), descriptor, root, debbugLevel);
    }

    public static JsonItem parse(JsonNode rootNode, JsonModelDescriptor descriptor, String root, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        return JsonNodeConverter.convert(rootNode, root, descriptor, debbugLevel);
    }

    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass, JsonDebugLevel debbugLevel) throws JsonParseException, IOException {
        return parse(file, descriptor, aClass.getTypeName(), debbugLevel);
    }

    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return parse(s, descriptor, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return parse(file, descriptor, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(final URL url1, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return parse(url1, descriptor, root, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass) throws JsonParseException, IOException {
        return parse(file, descriptor, aClass, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(JsonNode rootNode, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return JsonNodeConverter.convert(rootNode, root, descriptor, JsonDebugLevel.SIMPLE);
    }

}
