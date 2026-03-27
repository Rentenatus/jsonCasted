package de.jare.jsoncasted.parser;

import de.jare.jsoncasted.parserservice.JsonParserService;
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

    @Test
    public void testParseNodeCast1() throws Exception {
        File f = new File("./assets/legacy/config_legacy_string.json");
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            de.jare.jsoncasted.lang.JsonNode childNode = node.asObjectValues().get("castedString");
            Assert.assertNotNull(childNode);
            JsonNode cname = childNode.asObjectValues().get("_class");
            Assert.assertTrue("hallucinated.test.clazz".equals(cname.asText()));
        }
    }
    
        @Test
    public void testParseNodeCast2() throws Exception {
        File f = new File("./assets/legacy/config_legacy_long.json");
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr);
            de.jare.jsoncasted.lang.JsonNode childNode = node.asObjectValues().get("castedValue");
            Assert.assertNotNull(childNode);
            JsonNode cname = childNode.asObjectValues().get("_class");
            Assert.assertTrue("hallucinated.test.clazz".equals(cname.asText()));
        }
    }
}
