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
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The WoodResolver class handles the resolution of wood (object reference)
 * structures in JSON resources. It manages the loading of external resources
 * referenced via wood providers and coordinates the resolution process across
 * multiple resources.
 *
 * <p>
 * This class uses repository descriptors from the main descriptor to resolve
 * types for external resources, without requiring direct access to the
 * JsonModel instances.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class WoodElementResolver {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException Always thrown as this is a utility class.
     */
    private WoodElementResolver() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Resolves all wood references in a JsonSystem.This method attempts to
     * resolve all object references within the system, loading external
     * resources as needed when providers are not found.Attempts to resolve all
     * wood references in a JsonSystem.This method processes the main resource
     * and all registered resources, resolving object references in an iterative
     * manner.<p>
     * Repository descriptors are retrieved directly from the main descriptor's
     * repoDescriptor map, without accessing the JsonModel instances.
     * </p>
     *
     * @param sys The JsonSystem containing resources to resolve.
     * @param container
     * @param resourceDescriptor
     * @param debugLevel The debug level for controlling debug output.
     * @return The WoodResolution containing resolved objects, unresolved keys,
     * and exceptions.
     */
    public static WoodResolution resolve(JsonSystem sys, JsonResource container, JsonModelDescriptor resourceDescriptor, JsonDebugLevel debugLevel) {

        LinkingSet linkingSet = Objects.requireNonNull(container.getLinkingSet(),
                "container.linkingSet must not be null");
        WoodResolution resolution = new WoodResolution();
        Set<String> remainingKeys = new LinkedHashSet<>(linkingSet.getObjectIdMap().keySet());
        boolean progress = !remainingKeys.isEmpty();

        ConvertService service = new ConvertService(container, resourceDescriptor, resolution, debugLevel);

        while (progress) {
            progress = resolveLoop(remainingKeys, service);
            // `progress` is true only if at least one object could be resolved—that is, only if the set has shrunk.
        }
        for (String unresolved : remainingKeys) {
            resolution.addUnresolvedKey(unresolved);
        }
        return resolution;
    }

    /**
     * Performs one iteration of the resolution loop across all resources.
     *
     * @param remainingKeys The set of keys that still need to be resolved.
     * @param service The convert service for resolution.
     * @return true if any progress was made (objects were resolved == the set
     * has shrunk), false otherwise.
     */
    private static boolean resolveLoop(Set<String> remainingKeys, ConvertService service) {
        boolean progress = false;
        Set<String> resolvedThisRound = new LinkedHashSet<>();

        LinkingSet linkingSet = service.getLinkingSet();
        for (String key : remainingKeys) {
            LinkNodeEntry entry = linkingSet.getObjectIdMap().get(key);
            if (entry == null) {
                continue;
            }

            final JsonNode node = entry.getNode();
            if (!isConvertibleNow(node, linkingSet, service.getResolution())) {
                continue;
            }

            try {
                JsonTypeDescriptor typeDescriptor = resolveContextClass(node, service.getDescriptor());
                JsonItem convertedObject = JsonObjectConverter.convertObject(node, typeDescriptor, service);
                convertedObject.setWoodKey(key);
                service.getResolution().putResolvedObject(key, convertedObject);
                resolvedThisRound.add(key);
                progress = true;
            } catch (JsonParseException ex) {
                service.getResolution().addException(ex);
            }

        }
        remainingKeys.removeAll(resolvedThisRound);
        return progress;
    }

    /**
     * Checks if a JSON node can be converted now, i.e., all its dependencies
     * are resolved.
     *
     * @param node The JSON node to check.
     * @param linkingSet The linking set for resolving object references.
     * @param resolution The current resolution state.
     * @return true if the node can be converted, false otherwise.
     */
    private static boolean isConvertibleNow(JsonNode node,
            LinkingSet linkingSet,
            WoodResolution resolution) {

        if (node == null) {
            return false;
        }
        final String providerName = linkingSet.getProviderName();

        if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                if (!isConvertibleNow(child, linkingSet, resolution)) {
                    return false;
                }
            }
            return true;
        }

        if (!node.isObject()) {
            return true;
        }

        try {
//            String linkKey = node.getLink(providerName);
//            if (linkKey != null) {
//                if (!resolution.getUnmodifiableResolvedObjects().containsKey(linkKey)) {
//                    return false;
//                }
//            }
            String idKey = node.getObjectId(providerName);
            if (idKey != null) {
                if (!resolution.getUnmodifiableResolvedObjects().containsKey(idKey)) {
                    return isConvertibleBelow(node, linkingSet, resolution);
                }
            }
        } catch (JsonParseException ex) {
            resolution.addException(ex);
            return false;
        }

        return true;
    }

    /**
     * Checks if all children of a JSON node can be converted now.
     *
     * @param node The JSON node whose children to check.
     * @param linkingSet The linking set for resolving object references.
     * @param resolution The current resolution state.
     * @return true if all children can be converted, false otherwise.
     */
    public static boolean isConvertibleBelow(JsonNode node,
            LinkingSet linkingSet,
            WoodResolution resolution) {
        if (node.asObjectValues() == null) {
            return false;
        }
        for (Map.Entry<String, JsonNode> entry : node.asObjectValues().entrySet()) {
            String key = entry.getKey();
            if (JsonTerms.TERM_WOOD_OBJECT_ID.equals(key) || JsonTerms.TERM_WOOD_LINK.equals(key)
                    || JsonTerms.TERM_CLASS.equals(key) || JsonTerms.TERM_WOOD_PROVIDERS.equals(key)) {
                continue;
            }
            if (!isConvertibleNow(entry.getValue(), linkingSet, resolution)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resolves the context class for a JSON node by extracting the _class
     * field.
     *
     * @param node The JSON node to resolve the class for.
     * @param descriptor The model descriptor for type lookup.
     * @return The resolved type descriptor.
     * @throws JsonParseException If the node is not an object, missing _class,
     * or type not found.
     */
    private static JsonTypeDescriptor resolveContextClass(JsonNode node, JsonModelDescriptor descriptor)
            throws JsonParseException {

        if (node == null || !node.isObject() || node.asObjectValues() == null) {
            throw new JsonParseException("Context node must be an object.");
        }

        JsonNode classNode = node.asObjectValues().get(JsonTerms.TERM_CLASS);
        if (classNode == null) {
            throw new JsonParseException("Missing _class on containment root node.");
        }

        String className = classNode.toText();
        JsonTypeDescriptor typeDescriptor = descriptor.getTypePerceptive(className);

        if (typeDescriptor == null) {
            throw new JsonParseException("No JsonClass found in descriptor for _class=" + className);
        }

        return typeDescriptor;
    }
}
