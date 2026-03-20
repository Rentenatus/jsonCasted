/* <copyright>
 * Test variants parsing via Map
 */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.tools.JsonMapFacade;
import de.jare.jsonconfig.JsonConfigHelper;
import de.jare.jsonconfig.item.ConfigFeature;
import de.jare.jsonconfig.item.ConfigRoot;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class JsonConfigFactoryNGTest_Map {

    @Test
    public void testModel1_Map() {
        System.out.println("getLangDef (Map)");

        File configFile = new File("./assets/config/config1.json");
        testModelFromMap(configFile, JsonConfigDefinition.getInstance());
    }

    @Test
    public void testModelSeed_Map() {
        System.out.println("getLangDef (Map)");

        File configFile = new File("./assets/config/seedConfigTemplate.json");
        JsonConfigHelper helper = testModelFromMap(configFile, JsonConfigDefinition.getInstance());

        ConfigFeature f = helper.getFeature("main", "ollama");
        assertNotNull(f);

        System.out.println("host1: " + helper.getSetting(f, "host1"));
        System.out.println("host2: " + helper.getSetting(f, "host2"));
        System.out.println("host3: " + helper.getSetting(f, "host3"));
        System.out.println("port: " + helper.getSetting(f, "port"));

        f = helper.getFeature("main", "prompt");
        assertNotNull(f);

        System.out.println("incisive: " + helper.getSetting(f, "incisive"));
        System.out.println("codegen: " + helper.getSetting(f, "codegen"));
        System.out.println("timeoutDelay: " + helper.getSetting(f, "timeoutDelay"));
        System.out.println("rewriteStory: " + helper.getEnablement(f, "rewriteStory"));
        System.out.println("user0: " + helper.getLabel(f, "user0"));
    }

    private JsonConfigHelper testModelFromMap(File configFile, JsonConfigDefinition definition) {
        System.out.println("=============================================== File (Map)");
        System.out.println(configFile.getAbsolutePath());

        JsonItem obj1 = null;
        try {
            Map<String, Object> map = JsonMapFacade.parseToMap(configFile, definition, definition.getConfigRoot());
            obj1 = JsonParser.parse(map, definition, definition.getConfigRoot());
        } catch (JsonParseException | IOException | NullPointerException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        assertNotNull(obj1);
        System.out.println("=============================================== Config Class");
        System.out.println(obj1.getClass());

        ConfigRoot root = null;
        try {
            final Object buildInstance1 = obj1.buildInstance();
            System.out.println(buildInstance1.getClass().getName());
            assertNotNull(root = (ConfigRoot) buildInstance1);

        } catch (Exception ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
            fail(ex.getMessage(), ex);
        }
        System.out.println("=============================================== Comment");
        for (String comment : root.getComments()) {
            System.out.println("comment  > " + comment);
        }
        System.out.println("===============================================");
        return new JsonConfigHelper(root);
    }
}
