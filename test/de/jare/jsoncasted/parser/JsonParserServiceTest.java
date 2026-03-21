package de.jare.jsoncasted.parser;

import de.jare.jsoncasted.lang.JsonNode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParserServiceTest {

    private String assetPath(String name) {
        return System.getProperty("user.dir") + File.separator + "test_assets" + File.separator + "assets" + File.separator + "config" + File.separator + name;
    }

    @Test
    public void testLegacyLongCast() throws Exception {
        File f = new File(assetPath("config_legacy_long.json"));
        assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            assertEquals(JsonNode.Type.OBJECT, node.getType());
            Map<String, JsonNode> root = node.asObject();
            JsonNode casted = root.get("castedValue");
            assertNotNull(casted);
            assertEquals(JsonNode.Type.OBJECT, casted.getType());
            JsonNode cls = casted.asObject().get("_class");
            assertNotNull(cls);
            assertEquals("java.lang.Long", cls.asText());
            // also check inner value
            JsonNode val = casted.asObject().get("value");
            assertNotNull(val);
            assertEquals(JsonNode.Type.NUMBER, val.getType());
        }
    }

    @Test
    public void testLegacyStringCast() throws Exception {
        File f = new File(assetPath("config_legacy_string.json"));
        assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            Map<String, JsonNode> root = node.asObject();
            JsonNode casted = root.get("castedString");
            assertNotNull(casted);
            assertEquals(JsonNode.Type.OBJECT, casted.getType());
            JsonNode cls = casted.asObject().get("_class");
            assertNotNull(cls);
            assertEquals("java.lang.String", cls.asText());
            JsonNode val = casted.asObject().get("value");
            assertNotNull(val);
            assertEquals(JsonNode.Type.STRING, val.getType());
            assertEquals("hello", val.asText());
        }
    }
}
