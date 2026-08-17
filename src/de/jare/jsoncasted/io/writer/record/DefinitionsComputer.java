/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.record;

import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonRepo;
import de.jare.jsoncasted.model.JsonRepoModel;
import de.jare.jsoncasted.model.JsonType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Janusch Renteantus
 */
public class DefinitionsComputer {

    public static JsonNode calculateOrphaned(DefinitionsContext context) {
        Collection<DefinitionsContextObjectRecord> values = context.getRecordMap().values();
        JsonModel model = context.getModel();
        JsonRepoModel repoModel = new JsonRepoModel("repo");
        repoModel.addBasicModel();
        Map<String, JsonRepo> repos = new HashMap<>();

        for (DefinitionsContextObjectRecord record : values) {
            if (record.isFinding() || record.getContainer() != null) {
                continue; // Skip records that are 'onlsy' findings or have 'alredy' a container
            }
            JsonType type = record.getJsonType();
            JsonRepo repo = repos.get(type.getcName());
            if (repo == null) {
                if (repoModel.getJsonClass(type.getcName()) == null) {
                    repoModel.addRecursive(model, type);
                }
                repo = new JsonRepo(type.getcName());
                repos.put(type.getcName(), repo);
                 

            }
            repo.addItem(record.getObject());

        }

        JsonNode ret = JsonNode.arrayNode();
        return ret;
    }

}
