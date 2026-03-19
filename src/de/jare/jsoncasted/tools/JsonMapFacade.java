package de.jare.jsoncasted.tools;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Facade providing Map-based parsing and loading helpers.
 */
public class JsonMapFacade {

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(String s, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        JsonItem item = JsonParser.parse(s, definition, root);
        Object o = JsonToMapConverter.convert(item);
        if (o instanceof Map) return (Map<String, Object>) o;
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("value", o);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(File f, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        JsonItem item = JsonParser.parse(f, definition, root);
        Object o = JsonToMapConverter.convert(item);
        if (o instanceof Map) return (Map<String, Object>) o;
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("value", o);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(Reader r, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        JsonItem item = JsonParser.parse(r, definition, root);
        Object o = JsonToMapConverter.convert(item);
        if (o instanceof Map) return (Map<String, Object>) o;
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("value", o);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseToMap(URL u, JsonItemDefinition definition, JsonClass root) throws JsonParseException, IOException {
        JsonItem item = JsonParser.parse(u, definition, root);
        Object o = JsonToMapConverter.convert(item);
        if (o instanceof Map) return (Map<String, Object>) o;
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("value", o);
        return wrapper;
    }

    /**
     * Loads a Map into the library flow by converting it to a JsonItem. The
     * returned JsonItem can be used to call buildInstance() etc.
     */
    public static JsonItem mapToJsonItem(Map<String, Object> map) {
        return MapToJsonItemConverter.fromObject(map);
    }

    /**
     * Convenience: build instance directly from Map using the existing model
     */
    public static Object buildInstanceFromMap(Map<String, Object> map) throws Exception {
        JsonItem it = mapToJsonItem(map);
        return it.buildInstance();
    }

}
