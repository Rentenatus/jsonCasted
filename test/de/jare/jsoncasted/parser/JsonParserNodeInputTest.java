package de.jare.jsoncasted.parser;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonDebugLevel;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsonconfig.def.JsonConfigDefinition;
import java.io.File;
import java.io.FileReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JsonParserNodeInputTest {

    private String assetPath(String name) {
        return System.getProperty("user.dir") + File.separator + "test_assets" + File.separator + "assets" + File.separator + "config" + File.separator + name;
    }

    @Test
    public void testParseFromJsonNode() throws Exception {
        File f = new File(assetPath("config_legacy_string.json"));
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            JsonClass rootClass = JsonConfigDefinition.getInstance().getConfigRoot();
            JsonItem item = JsonParser.parse(node, JsonConfigDefinition.getInstance(), rootClass, JsonDebugLevel.SIMPLE);
            Assert.assertNotNull(item);
            Assert.assertTrue(item instanceof JsonObject);
            JsonObject obj = (JsonObject) item;
            JsonItem cs = obj.getParam("castedString");
            Assert.assertNotNull(cs);
            Assert.assertTrue(cs instanceof JsonObject);
            // After conversion the backing JsonNode should have an attached JsonClass
            de.jare.jsoncasted.lang.JsonNode childNode = node.asObject().get("castedString");
            Assert.assertNotNull(childNode.getJsonClass());
            String cname = ((de.jare.jsoncasted.model.item.JsonClass) childNode.getJsonClass()).getcName();
            // Accept both simple and fully-qualified class names depending on model configuration
            Assert.assertTrue("java.lang.String".equals(cname) || "String".equals(cname));
        }
    }
}
