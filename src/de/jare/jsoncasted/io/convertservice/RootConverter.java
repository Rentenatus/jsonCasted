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
import de.jare.jsoncasted.item.JsonItemStore;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The RootConverter class provides the main entry point for converting JSON resources into JsonItem instances. It
 * coordinates the creation of JsonSystem, wood resolution, and the conversion process.
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
     * Converts a JSON resource into a JsonItem using the specified context class name and model.
     * This is the primary method for converting JSON resources to the internal JsonItem model.
     *
     * @param res The JSON resource to convert.
     * @param cName The name of the context/root class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @return The converted JsonItem, or null if the resource or its root is null.
     * @throws JsonParseException If conversion fails.
     */
    public static JsonItem convert(JsonResource res, String cName, JsonModelDescriptor descriptor,
            JsonDebugLevel debugLevel) throws JsonParseException {
        // Create JsonItemStore for the new architecture
        JsonItemStore itemStore = new JsonItemStore();
        return convertWithStore(res, cName, descriptor, debugLevel, itemStore);
    }
    
    /**
     * Converts a JSON resource into a JsonItem using the specified context class name, model, and item store.
     * This method supports the new ItemStore architecture for proxy reference resolution.
     *
     * @param res The JSON resource to convert.
     * @param cName The name of the context/root class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @param itemStore The JsonItemStore for storing all JsonItems during conversion.
     * @return The converted JsonItem, or null if the resource or its root is null.
     * @throws JsonParseException If conversion fails.
     */
    public static JsonItem convertWithStore(JsonResource res, String cName, JsonModelDescriptor descriptor,
            JsonDebugLevel debugLevel, JsonItemStore itemStore) throws JsonParseException {
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
                // Register resource in itemStore
                if (itemStore != null) {
                    itemStore.addSourceResource(resource);
                }
            }
        }
        // Set the sorted resources back into the system
        sys.setResources(sortedResources);

        WoodResolution resolution = new WoodResolution();
        for (JsonResource itemRes : sortedResources) {
            String resName = itemRes.getProviderName();
            JsonModelDescriptor repoDesc = descriptor.getRepoDescriptorOrThis(resName);
            WoodElementResolver.resolve(sys, itemRes, repoDesc, debugLevel);
        }
        
        // Convert all nodes from all resources and register them in the itemStore
        if (itemStore != null) {
            convertAllNodesToItems(sortedResources, descriptor, resolution, debugLevel, itemStore);
        }
        
        return JsonNodeConverter.convert(res, cName, descriptor, resolution, debugLevel, itemStore);
    }
    
    /**
     * Converts all nodes from all resources into JsonItems and registers them in the itemStore.
     * This enables lazy resolution of proxy references.
     *
     * @param resources The list of sorted JSON resources.
     * @param descriptor The model descriptor containing type definitions.
     * @param resolution The wood resolution for handling object references.
     * @param debugLevel The debug level for controlling debug output.
     * @param itemStore The JsonItemStore to register all items in.
     * @throws JsonParseException If conversion fails.
     */
    private static void convertAllNodesToItems(List<JsonResource> resources, JsonModelDescriptor descriptor,
            WoodResolution resolution, JsonDebugLevel debugLevel, JsonItemStore itemStore) throws JsonParseException {
        for (JsonResource resource : resources) {
            JsonModelDescriptor repoDesc = descriptor.getRepoDescriptorOrThis(resource.getProviderName());
            ConvertService service = new ConvertService(resource, descriptor, resolution, debugLevel, itemStore);
            convertResourceNodeTree(resource.getRoot(), repoDesc, service, itemStore);
        }
    }
    
    /**
     * Recursively converts a node tree into JsonItems and registers them in the itemStore.
     *
     * @param node The JSON node to convert.
     * @param descriptor The model descriptor for the current resource.
     * @param service The convert service.
     * @param itemStore The JsonItemStore to register items in.
     * @return The converted JsonItem.
     * @throws JsonParseException If conversion fails.
     */
    private static JsonItem convertResourceNodeTree(JsonNode node, JsonModelDescriptor descriptor,
            ConvertService service, JsonItemStore itemStore) throws JsonParseException {
        if (node == null) {
            return null;
        }
        
        // Get the context class from the node's _class field if available
        JsonTypeDescriptor contextClass = null;
        if (node.isObject()) {
            Map<String, JsonNode> objectValues = node.asObjectValues();
            if (objectValues != null) {
                JsonNode classNode = objectValues.get(JsonTerms.TERM_CLASS);
                if (classNode != null) {
                    String className = classNode.asText();
                    contextClass = descriptor.getType(className);
                }
            }
        }
        
        // Fallback to Object if no context class found
        if (contextClass == null) {
            contextClass = descriptor.getType("java.lang.Object");
        }
        
        // Convert the node to a JsonItem
        JsonItem item = JsonNodeConverter.convert(node, contextClass, service);
        
        if (item == null) {
            return null;
        }
        
        // Generate item ID and register in store
        String providerName = service.getLinkingSet().getProviderName();
        String objectId = extractObjectId(node, providerName);
        if (objectId != null && providerName != null) {
            String itemId = JsonItemStore.buildItemId(providerName, objectId);
            itemStore.addItem(itemId, item);
            
            // Check if this node is a proxy reference (has _woodLink)
            String linkId = extractLinkId(node, providerName);
            if (linkId != null) {
                item.setLinkId(JsonItemStore.buildLinkId(providerName, linkId));
            }
        }
        
        // Recursively process children
        if (node.isObject()) {
            Map<String, JsonNode> children = node.asObjectValues();
            if (children != null) {
                for (JsonNode child : children.values()) {
                    convertResourceNodeTree(child, descriptor, service, itemStore);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                convertResourceNodeTree(child, descriptor, service, itemStore);
            }
        }
        
        return item;
    }
    
    /**
     * Extracts the raw object ID from a node (without provider prefix).
     *
     * @param node The JSON node.
     * @param providerName The provider name.
     * @return The raw object ID, or null if not found.
     */
    private static String extractObjectId(JsonNode node, String providerName) {
        if (node == null || providerName == null) {
            return null;
        }
        try {
            String fullId = node.getObjectId(providerName);
            if (fullId == null) {
                return null;
            }
            // Remove provider prefix (format: provider::objectId)
            int index = fullId.indexOf("::");
            if (index >= 0 && index < fullId.length() - 1) {
                return fullId.substring(index + 2);
            }
            return fullId;
        } catch (JsonParseException e) {
            return null;
        }
    }
    
    /**
     * Extracts the link ID from a node.
     *
     * @param node The JSON node.
     * @param providerName The provider name.
     * @return The link ID, or null if not found.
     */
    private static String extractLinkId(JsonNode node, String providerName) {
        if (node == null || providerName == null) {
            return null;
        }
        try {
            return node.getLink(providerName);
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * Ermittelt den Klassennamen für die Root-Konvertierung einer JSON-Ressource.Priorisiert das _class-Feld des
     * Root-Nodes, fällt zurück auf den Kontext-Klassennamen.
     *
     * @param resource Die JSON-Ressource, deren Root-Klasse bestimmt werden soll.
     * @param contextClassName Der Fallback-Klassenname (z. B. übergebener cName).
     * @return Der zu verwendende Klassenname: zuerst _class-Feld des Root-Nodes, sonst contextClassName.
     */
    public static String rootClassName(JsonResource resource, String contextClassName) {
        if (resource == null || resource.getRoot() == null || !resource.getRoot().isObject() || resource.getRoot().asObjectValues() == null) {
            return contextClassName;
        }

        JsonNode classNode = resource.getRoot().asObjectValues().get(JsonTerms.TERM_CLASS);
        return (classNode != null) ? classNode.asText() : contextClassName;
    }

}
