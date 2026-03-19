package de.jare.jsoncasted.tools;

import de.jare.jsoncasted.item.JsonItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts JsonItem structures into generic Java Maps/Lists/Primitives.
 * Numbers are kept as strings per project decision.
 */
public class JsonToMapConverter {

    public static Object convert(JsonItem item) {
        if (item == null) {
            return null;
        }
        // List
        if (item.isList()) {
            List<Object> out = new ArrayList<>();
            Iterator<JsonItem> it = item.listIterator();
            while (it.hasNext()) {
                out.add(convert(it.next()));
            }
            return out;
        }
        // Object with params
        Set<String> keys = item.getParamSet();
        if (keys != null) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (String k : keys) {
                JsonItem val = item.getParam(k);
                out.put(k, convert(val));
            }
            return out;
        }
        // Primitive / value
        String v = item.getStringValue();
        if (v == null) {
            return null;
        }
        // boolean handling
        if ("true".equalsIgnoreCase(v)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(v)) return Boolean.FALSE;
        if ("null".equalsIgnoreCase(v)) return null;
        // Numbers are kept as strings (decision)
        return v;
    }
}
