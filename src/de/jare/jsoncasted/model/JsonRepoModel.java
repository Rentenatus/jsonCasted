/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import static de.jare.jsoncasted.model.JsonCollectionType.LIST;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonInter;
import de.jare.jsoncasted.model.item.JsonUnknown;
import java.util.Iterator;

/**
 * Repository model that extends JsonModel and implements JsonRepoEntity.
 *
 * <p>
 * This model is used for managing types from external JSON resources, ensuring that all registered classes implement
 * the JsonRepoEntity interface for cross-resource referencing support.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonRepoModel extends JsonModel {

    private JsonClass repo;

    /**
     * Constructs a JsonRepoModel with the specified model name. Creates a repository model that can only contain
     * JsonClass instances whose underlying classes implement JsonRepoEntity.
     *
     * @param mName The name of the repository model.
     */
    public JsonRepoModel(String mName) {
        super(mName);
    }

    /**
     * Recursively adds a JSON type and all its referenced types to the model. Handles JsonEnum, JsonInter, and
     * JsonClass types appropriately.
     *
     * @param parent Parent model that knows recursive JsonType.
     * @param jType The JSON type to add recursively.
     */
    public void addRecursive(JsonModel parent, final JsonType jType) {
        final String cName = jType.getcName();

        final JsonClass parentClass = parent.getJsonClass(cName);
        if (parentClass != null) {
            if (getJsonClass(cName) == null) {
                addClass(parentClass);
            }
            Iterator<JsonField> it = parentClass.fieldsIterator();
            while (it.hasNext()) {
                JsonField next = it.next();
                JsonType jt = next.getjType();
                if (jt != null) {
                    addRecursive(parent, jt);
                }
            }
            return;
        }

        final JsonClass parentEnum = parent.getJsonEnum(cName);
        if (parentEnum != null) {
            if (getJsonEnum(cName) == null) {
                enums.put(cName, parentEnum);
            }
            return;
        }

        final JsonInter parentInter = parent.getJsonInter(cName);
        if (parentInter != null) {
            if (getJsonInter(cName) == null) {
                addInterface(parentInter);
            }
            for (JsonClass jt : parentInter.iterable()) {
                addRecursive(parent, jt);
            }
        }
    }

    /**
     * Creates a new JsonClass for representing a JsonRepo.
     *
     * @return A JsonClass configured for JsonRepo serialization.
     */
    public JsonClass getOrCreateRepo() {
        if (repo != null) {
            return repo;
        }
        JsonInter asObject = getJsonInter("Object");
        if (asObject == null) {
            addInterface(asObject = new JsonUnknown("Object"));
        }
        repo = newJsonReflectIndividually(JsonRepo.class, "JsonRepo");
        repo.addCParam("repoName", getJsonClass("String"));
        repo.addField("contents", asObject, LIST);
        repo.addField("subRepos", repo, LIST);
        repo.setDefinitional(true);
        return repo;
    }

}
