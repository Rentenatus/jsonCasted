/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;

public class JsonBuilder {

    private final JsonItem rootItem;
    private BuilderService builderService;

    public static Object buildInstance(JsonModel model, boolean throwClassEx, JsonItem rootItem) throws JsonBuildException {
        BuilderService builderService = new BuilderService(model, throwClassEx);
        return builderService.build(rootItem);
    }

    public JsonBuilder(JsonItem rootItem) {
        this.rootItem = rootItem;
        this.builderService = null;
    }

    public Object buildInstance(JsonModel model, boolean throwClassEx) throws JsonBuildException {
        builderService = new BuilderService(model, throwClassEx);
        return builderService.build(rootItem);
    }

    public JsonItem getRootItem() {
        return rootItem;
    }

    public BuilderService getBuilderService() {
        return builderService;
    }

}
