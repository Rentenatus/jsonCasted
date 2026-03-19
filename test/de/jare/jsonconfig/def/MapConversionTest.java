/* Simple tests for Map converters */
package de.jare.jsonconfig.def;

import de.jare.jsoncasted.tools.JsonMapFacade;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class MapConversionTest {

    @Test
    public void testParseToMapAndBack() throws Exception {
        File configFile = new File("./assets/config/config1.json");
        Map<String, Object> map = JsonMapFacade.parseToMap(configFile.getPath() == null ? "" : new String(java.nio.file.Files.readAllBytes(configFile.toPath())), JsonConfigDefinition.getInstance(), JsonConfigDefinition.getInstance().getConfigRoot());
        assertNotNull(map);
        assertTrue(map.containsKey("profiles"));

        // Convert back to JsonItem and build instance
        Object built = JsonMapFacade.buildInstanceFromMap(map);
        assertNotNull(built);
    }
}
