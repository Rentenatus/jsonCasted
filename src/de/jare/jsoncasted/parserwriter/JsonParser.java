/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserservice.JsonParserService;
import de.jare.jsoncasted.pconvertservice.RootConverter;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonParser {

    public static JsonItem parse(String s, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(s, debugLevel);
        return parse(res, descriptor, root, debugLevel);
    }

    public static JsonItem parse(File file, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(file, debugLevel);
        return parse(res, descriptor, root, debugLevel);
    }

    public static JsonItem parse(final URL url, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        JsonResource res = JsonParserService.parse(url, debugLevel);
        return parse(res, descriptor, root, debugLevel);
    }

    public static JsonItem parse(JsonResource res, JsonModelDescriptor descriptor, String root, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        return RootConverter.convert(res, root, descriptor, debugLevel);
    }

    public static JsonItem parse(File file, JsonModelDescriptor descriptor, Class<?> aClass, JsonDebugLevel debugLevel) throws JsonParseException, IOException {
        return parse(file, descriptor, aClass.getTypeName(), debugLevel);
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

    public static JsonItem parse(JsonResource res, JsonModelDescriptor descriptor, String root) throws JsonParseException, IOException {
        return RootConverter.convert(res, root, descriptor, JsonDebugLevel.SIMPLE);
    }

    public static JsonItem parse(File file, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        return parse(file, definition.getDescriptor(), root.getcName());
    }
}
