package de.jare.jsoncasted.tools;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.item.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts generic Java Maps/Lists/Primitives into JsonItem structures
 * that the library can consume (with null JsonClass / JsonType).
 */
public class MapToJsonItemConverter {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static JsonItem fromObject(Object obj) {
        if (obj == null) {
            return new JsonValue("null", null);
        }
        if (obj instanceof Map) {
            JsonObject jo = new JsonObject(null);
            for (Object e : ((Map) obj).entrySet()) {
                Map.Entry me = (Map.Entry) e;
                String key = String.valueOf(me.getKey());
                Object val = me.getValue();
                jo.putParam(key, fromObject(val));
            }
            return jo;
        }
        if (obj instanceof List) {
            List listIn = (List) obj;
            ArrayList<JsonItem> items = new ArrayList<>(listIn.size());
            for (Object o : listIn) {
                items.add(fromObject(o));
            }
            return new JsonList(items, true, null);
        }
        // Primitive (Boolean / String). Numbers are kept as strings per decision.
        if (obj instanceof Boolean) {
            return new JsonValue(((Boolean) obj).toString(), null);
        }
        return new JsonValue(String.valueOf(obj), null);
    }
}
