/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonAnnotation;
import de.jare.jsoncasted.model.item.JsonClass;
import static de.jare.jsoncasted.model.JsonCollectionType.LIST;

/**
 * Model definition of the project layer (concept: CONCEPT_ProjectView.md, sections 1, 2 and 4):
 * the project document ({@code .wood/project.json}) and the bundle descriptor
 * ({@code .woodbundle.json}) in one model with two roots, parseable and writable like every
 * other JSON definition of this library.
 *
 * @author Janusch Rentenatus
 */
public class WoodProjectDefinition implements JsonItemDefinition {

    public static final WoodProjectDefinition INSTANCE = new WoodProjectDefinition();

    public static WoodProjectDefinition getInstance() {
        return INSTANCE;
    }

    private final JsonModel model;
    private final JsonClass projectRoot;
    private final JsonClass bundleRoot;

    public WoodProjectDefinition() {
        model = new JsonModel("WoodProject");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");

        JsonClass partitionType = model.newJsonEnumByName(PartitionType.class);

        JsonClass partition = model.newJsonReflect(Partition.class, true);
        partition.addField("name", asString).addAnnotation(JsonAnnotation.doc("Virtual label of the partition, also the virtual path segment."));
        partition.addField("type", partitionType).addAnnotation(JsonAnnotation.doc("Semantic role of the partition: src, test, assets, json, gen or target."));
        partition.addField("path", asString).addAnnotation(JsonAnnotation.doc("Physical location, relative to the project root or absolute for external mounts."));
        partition.addField("readonly", asBoolean).addAnnotation(JsonAnnotation.doc("Optional: files render, but no editor action saves into this mount."));
        partition.addAnnotation(JsonAnnotation.doc("A level-1 mount of a physical folder."));
        partition.addAnnotation(JsonAnnotation.editString("${name}"));

        JsonClass bundleImport = model.newJsonReflect(BundleImport.class, true);
        bundleImport.addField("bundle", asString).addAnnotation(JsonAnnotation.doc("Name of the imported bundle."));
        bundleImport.addField("path", asString).addAnnotation(JsonAnnotation.doc("Reactor import: relative path inside the project, live reference."));
        bundleImport.addField("version", asString).addAnnotation(JsonAnnotation.doc("Dependency import: pinned version from the local bundle repository."));
        bundleImport.addAnnotation(JsonAnnotation.doc("One import of the bundle, either path (reactor) or version (dependency)."));

        bundleRoot = model.newJsonReflect(BundleDescriptor.class, true);
        bundleRoot.addField("name", asString).addAnnotation(JsonAnnotation.doc("Display and import identity within the project, defaults to the folder name."));
        bundleRoot.addField("main", asString).addAnnotation(JsonAnnotation.doc("The entry point file a double-click opens."));
        bundleRoot.addField("files", asString, LIST).addAnnotation(JsonAnnotation.doc("The JSON files of this bundle, relative names only."));
        bundleRoot.addField("protected", asString, LIST).addAnnotation(JsonAnnotation.doc("Protected files of this bundle: temporary read-only fence per declared file, opens as a read-only view until removed."));
        bundleRoot.addField("imports", bundleImport, LIST).addAnnotation(JsonAnnotation.doc("Imports of this bundle, path or version based."));
        bundleRoot.addAnnotation(JsonAnnotation.doc("Bundle descriptor: a folder of JSON files with one identity."));
        bundleRoot.addAnnotation(JsonAnnotation.editString("${name}"));

        projectRoot = model.newJsonReflect(ProjectDocument.class, true);
        projectRoot.addField("name", asString).addAnnotation(JsonAnnotation.doc("Name of the project."));
        projectRoot.addField("roots", partition, LIST).addAnnotation(JsonAnnotation.doc("The partitions (level-1 mounts) of the project."));
        projectRoot.addAnnotation(JsonAnnotation.doc("Root of a wood project document."));
        projectRoot.addAnnotation(JsonAnnotation.editString("project ${name}"));

        model.setRootNodeCast(projectRoot.getcName());
    }

    public JsonClass getProjectRoot() {
        return projectRoot;
    }

    public JsonClass getBundleRoot() {
        return bundleRoot;
    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }

    /**
     * Project documents and bundle descriptors are plain documents: no wood id notation, no
     * shared-object resolution across files. This keeps the files free of {@code _woodObjectId}
     * members and the global bundle registry out of the parse.
     */
    @Override
    public de.jare.jsoncasted.io.JsonWoodIdLevel getWoodIdLevel() {
        return de.jare.jsoncasted.io.JsonWoodIdLevel.NEVER;
    }

}
