package de.jare.jsoncasted.parser;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import de.jare.jsoncasted.parserservice.JsonParserService;
import java.io.File;
import java.io.FileReader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for JSON parser node input functionality.
 * Tests parsing of JSON files with casting and type resolution.
 *
 * @author Janusch Rentenatus
 */
public class JsonParserNodeInputNGTest {

    /**
     * Tests parsing a JSON node with a casted string value.
     * Verifies that the _class field is correctly parsed and resolved.
     *
     * @throws Exception If parsing fails.
     */
    @Test
    public void testParseNodeCast1() throws Exception {
        File f = new File("./assets/legacy/config_legacy_string.json");
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr, f.getName(), JsonDebugLevel.INFO).getRoot();
            de.jare.jsoncasted.lang.JsonNode childNode = node.asObjectValues().get("castedString");
            Assert.assertNotNull(childNode);
            JsonNode cname = childNode.asObjectValues().get(TERM_CLASS);
            Assert.assertTrue("hallucinated.test.clazz".equals(cname.asText()));
        }
    }

    /**
     * Tests parsing a JSON node with a casted long value.
     * Verifies that the _class field is correctly parsed and resolved.
     *
     * @throws Exception If parsing fails.
     */
    @Test
    public void testParseNodeCast2() throws Exception {
        File f = new File("./assets/legacy/config_legacy_long.json");
        Assert.assertTrue(f.exists(), "asset file must exist: " + f.getAbsolutePath());
        JsonParserService svc = new JsonParserService();
        try (FileReader fr = new FileReader(f)) {
            JsonNode node = svc.parse(fr, f.getName(), JsonDebugLevel.INFO).getRoot();
            de.jare.jsoncasted.lang.JsonNode childNode = node.asObjectValues().get("castedValue");
            Assert.assertNotNull(childNode);
            JsonNode cname = childNode.asObjectValues().get(TERM_CLASS);
            Assert.assertTrue("hallucinated.test.clazz".equals(cname.asText()));
        }
    }
}
