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
        // If the created JsonItem has no JsonClass associated, wrap it into a JsonObject
        // with a generic JsonMap class so buildInstance() can produce a usable instance.
        if (it instanceof de.jare.jsoncasted.item.JsonObject) {
            // If the JsonObject already has a class (unlikely from map conversion), use it.
            // Otherwise assign a JsonMap class that builds generic map-based instances.
            de.jare.jsoncasted.item.JsonObject jo = (de.jare.jsoncasted.item.JsonObject) it;
            if (jo.getPrintClassName().equals("null")) {
                // Create a JsonMap type that will build JsonInstance map-like containers.
                de.jare.jsoncasted.model.item.JsonMap jsonMapType = new de.jare.jsoncasted.model.item.JsonMap("Map", true, (Class) de.jare.jsoncasted.lang.JsonInstance.class, null, de.jare.jsoncasted.model.JsonCollectionType.NONE);
                // Wrap existing object into a new JsonObject with the jsonMapType class so buildInstance() will delegate to JsonMapBuilder.
                de.jare.jsoncasted.item.JsonObject wrapped = new de.jare.jsoncasted.item.JsonObject(jsonMapType);
                for (String key : jo.getParamSet()) {
                    wrapped.putParam(key, jo.getParam(key));
                }
                it = wrapped;
            }
        }
        return it.buildInstance();
    }

}
