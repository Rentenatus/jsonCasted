package de.jare.jsoncasted.parser;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.parserservice.JsonParserService;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonParserServiceTest {

    private String assetPath(String name) {
        return System.getProperty("user.dir") + File.separator + "test_assets" + File.separator + "assets" + File.separator + "config" + File.separator + name;
    }

    @Test
    public void testLegacyLongCast() throws Exception {
        File f = new File(assetPath("config_legacy_long.json"));
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            Assert.assertEquals(node.getType(), JsonNodeType.OBJECT);
            Map<String, JsonNode> root = node.asObjectValues();
            JsonNode casted = root.get("castedValue");
            Assert.assertNotNull(casted);
            Assert.assertEquals(casted.getType(), JsonNodeType.OBJECT);
            JsonNode cls = casted.asObjectValues().get("_class");
            Assert.assertNotNull(cls);
            Assert.assertEquals(cls.asText(), "java.lang.Long");
            // also check inner value
            JsonNode val = casted.asObjectValues().get("value");
            Assert.assertNotNull(val);
            Assert.assertEquals(val.getType(), JsonNodeType.NUMBER);
        }
    }

    @Test
    public void testLegacyStringCast() throws Exception {
        File f = new File(assetPath("config_legacy_string.json"));
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            Map<String, JsonNode> root = node.asObjectValues();
            JsonNode casted = root.get("castedString");
            Assert.assertNotNull(casted);
            Assert.assertEquals(casted.getType(), JsonNodeType.OBJECT);
            JsonNode cls = casted.asObjectValues().get("_class");
            Assert.assertNotNull(cls);
            Assert.assertEquals(cls.asText(), "java.lang.String");
            JsonNode val = casted.asObjectValues().get("value");
            Assert.assertNotNull(val);
            Assert.assertEquals(val.getType(), JsonNodeType.STRING);
            Assert.assertEquals(val.asText(), "hello");
        }
    }

}
