package de.jare.jsoncasted.item.builder;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.JsonModel;

public class JsonBuilder {

    private final JsonItem rootItem;
    private BuilderService builderService;

    public static Object buildInstance(JsonModel model, JsonItem rootItem) throws JsonBuildException {
        BuilderService builderService = new BuilderService(model);
        return builderService.build(rootItem);
    }

    public JsonBuilder(JsonItem rootItem) {
        this.rootItem = rootItem;
        this.builderService = null;
    }

    public Object buildInstance(JsonModel model) throws JsonBuildException {
        builderService = new BuilderService(model);
        return builderService.build(rootItem);
    }

    public JsonItem getRootItem() {
        return rootItem;
    }

    public BuilderService getBuilderService() {
        return builderService;
    }

}
