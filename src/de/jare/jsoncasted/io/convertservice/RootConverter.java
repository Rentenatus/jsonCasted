/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.convertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The RootConverter class provides the main entry point for converting JSON
 * resources into JsonItem instances. It coordinates the creation of JsonSystem,
 * wood resolution, and the conversion process.
 *
 * @author Janusch Rentenatus
 */
public final class RootConverter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException Always thrown as this is a utility class.
     */
    private RootConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a JSON resource into a JsonItem using the specified context
     * class name and model.This is the primary method for converting JSON
     * resources to the internal JsonItem model.
     *
     * @param res The JSON resource to convert.
     * @param cName The name of the context/root class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @return The converted JsonItem, or null if the resource or its root is
     * null.
     * @throws JsonParseException If conversion fails.
     */
    public static JsonItem convert(JsonResource res, String cName, JsonModelDescriptor descriptor,
            JsonDebugLevel debugLevel) throws JsonParseException {
        if (res == null) {
            return null;
        }
        if (res.getRoot() == null) {
            return null;
        }

        JsonSystem sys = JsonSystem.of(res);
        try {
            WoodProxyResolver.resolveProviders(sys, debugLevel);
        } catch (IOException ex) {
            throw new JsonParseException("Failed to resolve the proxies.", ex);
        }

        // Reorder resources in topological order (dependencies first)
        List<String> sortedSynonyms = sys.getSortedSynonyms();
        List<JsonResource> sortedResources = new ArrayList<>();
        for (String synonym : sortedSynonyms) {
            JsonResource resource = sys.findResourcesBySynonym(synonym);
            if (resource != null && !sortedResources.contains(resource)) {
                sortedResources.add(resource);
            }
        }
        // Set the sorted resources back into the system
        sys.setResources(sortedResources);

        JsonItem converted = null;
        WoodResolution resolution = new WoodResolution();
        for (JsonResource itemRes : sortedResources) {
            String itemClassName = rootClassName(itemRes, cName);
            String resName = itemRes.getProviderName();
            JsonModelDescriptor repoDesc = descriptor.getRepoDescriptorOrThis(resName);
            WoodElementResolver.resolve(sys, itemRes, repoDesc, debugLevel);
            converted = JsonNodeConverter.convert(itemRes, itemClassName, repoDesc, resolution, debugLevel);

        }
        return converted;
    }

    /**
     * Ermittelt den Klassennamen für die Root-Konvertierung einer
     * JSON-Ressource.Priorisiert das _class-Feld des Root-Nodes, fällt zurück
     * auf den Kontext-Klassennamen.
     *
     * @param resource Die JSON-Ressource, deren Root-Klasse bestimmt werden
     * soll.
     * @param contextClassName Der Fallback-Klassenname (z. B. übergebener
     * cName).
     * @return Der zu verwendende Klassenname: zuerst _class-Feld des
     * Root-Nodes, sonst contextClassName.
     */
    public static String rootClassName(JsonResource resource, String contextClassName) {
        if (resource == null || resource.getRoot() == null || !resource.getRoot().isObject()) {
            return contextClassName;
        }

        JsonNode classNode = resource.getRoot().asObjectValues().get(JsonTerms.TERM_CLASS);
        return (classNode != null) ? classNode.asText() : contextClassName;
    }

}
