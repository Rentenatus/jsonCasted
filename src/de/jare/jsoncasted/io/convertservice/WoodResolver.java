/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.convertservice;

import de.jare.debug.DebugTuple;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.parserservice.ParseStreamReader;
import de.jare.jsoncasted.io.parserservice.RootParser;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.jsoncasted.lang.JsonTerms;
import static de.jare.jsoncasted.lang.JsonTerms.SELF_SYNONYM;
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.wood.WoodProvider;
import de.jare.jsoncasted.wood.WoodProviderBox;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * The WoodResolver class handles the resolution of wood (object reference) structures in JSON resources. It manages the
 * loading of external resources referenced via wood providers and coordinates the resolution process across multiple
 * resources.
 *
 * <p>
 * This class uses repository descriptors from the main descriptor to resolve types for external resources, without
 * requiring direct access to the JsonModel instances.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class WoodResolver {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException Always thrown as this is a utility class.
     */
    private WoodResolver() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Resolves all wood references in a JsonSystem. This method attempts to resolve all object references within the
     * system, loading external resources as needed when providers are not found.
     *
     * @param sys The JsonSystem containing resources to resolve.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @return The WoodResolution containing resolved objects, unresolved keys, and exceptions.
     */
    public static WoodResolution resolve(
            JsonSystem sys,
            JsonModelDescriptor descriptor,
            JsonDebugLevel debugLevel) {

        Objects.requireNonNull(sys, "container must not be null");
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        Objects.requireNonNull(debugLevel, "debugLevel must not be null");

        WoodResolution resolution = attempt(sys, descriptor, debugLevel);
        if (resolution.hasExceptions()) {
            return resolution;
        }
        if (resolution.isFullyResolved()) {
            return resolution;
        }

        boolean wasSomething = false;

        for (String synonym : resolution.unresolvedProvider()) {
            WoodProvider provider = sys.getProviderBox().findBySynonym(synonym);
            if (provider == null) {
                resolution.addException(
                        new JsonParseException("The provider with the synonym " + synonym + " could not be found."));
                continue;
            }
            JsonResource imp = sys.findResourcesBySynonym(synonym);
            if (imp == null) {
                try {
                    JsonResource loaded = load(provider, debugLevel);
                    sys.addResource(loaded);
                    sys.getProviderBox().mergeBox(loaded.getExpectedBox());
                    wasSomething = true;
                } catch (FileNotFoundException ex) {
                    resolution.addException(new JsonParseException(
                            "The resource with the synonym " + synonym + " cannot be found.", ex));
                } catch (IOException | JsonParseException ex) {
                    resolution.addException(new JsonParseException(
                            "The resource with the synonym " + synonym + " cannot be loaded.", ex));
                }
            }
        }
        if (wasSomething) {
            return resolve(sys, descriptor, debugLevel);
        }

        return resolution;
    }

    /**
     * Loads a JSON resource from a wood provider.
     *
     * @param provider The wood provider containing the file information.
     * @param debugLevel The debug level for controlling debug output.
     * @return The loaded JSON resource.
     * @throws FileNotFoundException If the provider file cannot be found.
     * @throws IOException If I/O errors occur during loading.
     * @throws JsonParseException If parsing fails.
     */
    public static JsonResource load(WoodProvider provider, JsonDebugLevel debugLevel)
            throws FileNotFoundException, IOException, JsonParseException {
        File file = new File(provider.getFilename());
        ParseStreamReader psr = new ParseStreamReader(new FileReader(file), debugLevel);
        JsonResource subContainer = JsonResource.forFile(provider.getFilename());
        subContainer.setProviderName(provider.getSynonym());
        return RootParser.parse(psr, subContainer, debugLevel);
    }

    /**
     * Recursively loads all provider files referenced in the main resource's provider box and merges their linking sets
     * into the main resource's linking set using topological sorting to ensure correct dependency order.
     *
     * <p>
     * This method ensures that all object IDs and links from dependent provider files are available in the main
     * resource before resolution begins. It detects cycles and throws an exception if circular dependencies exist.
     * Multiple dependencies on the same provider are supported.</p>
     *
     * <p>
     * The loading order is determined by the following algorithm:</p>
     * <ol>
     * <li><b>Graph Construction:</b> All provider synonyms are collected from the main resource and all existing
     * resources. For each resource, its provider box defines dependencies: if resource R (with provider name PN)
     * references provider P in its box, an edge P -> PN is added to the dependency graph.</li>
     * <li><b>Topological Sorting:</b> Uses Kahn's algorithm to compute a valid loading order:
     * <ol>
     * <li>Compute in-degree (number of dependencies) for each provider node</li>
     * <li>Start with all nodes having in-degree 0 (no dependencies)</li>
     * <li>Process nodes, reducing in-degree of their dependents</li>
     * <li>Add nodes to the result when their in-degree reaches 0</li>
     * </ol>
     * </li>
     * <li><b>Cycle Detection:</b> If the sorted list contains fewer nodes than the total number of provider synonyms, a
     * cycle exists and a {@code JsonParseException} is thrown with the unsortable providers.</li>
     * <li><b>Ordered Loading:</b> Providers are loaded in the computed topological order, ensuring that all
     * dependencies of a provider are available before the provider itself is loaded.</li>
     * <li><b>LinkingSet Merging:</b> After loading each provider, its linking set (containing object IDs and links) is
     * merged into the main resource's linking set.</li>
     * </ol>
     *
     * <p>
     * Note: This method modifies the main resource's linking set by merging in all object IDs and links from loaded
     * provider resources. Already loaded resources are skipped.</p>
     *
     * @param sys The JsonSystem containing the main resource with provider box.
     * @param debugLevel The debug level for controlling debug output.
     * @throws IOException If I/O errors occur during loading.
     * @throws JsonParseException If parsing fails for any provider file, or if a cycle is detected in provider
     * dependencies.
     */
    public static void resolveProviders(JsonSystem sys, JsonDebugLevel debugLevel)
            throws IOException, JsonParseException {
        Objects.requireNonNull(sys, "sys must not be null");
        Objects.requireNonNull(debugLevel, "debugLevel must not be null");

        JsonResource mainResource = sys.getMainResource();
        if (mainResource == null) {
            sys.setSortedSynonyms(Collections.singletonList(SELF_SYNONYM));
            return;
        }

        LinkingSet mainLinkingSet = mainResource.getLinkingSet();
        if (mainLinkingSet == null) {
            sys.setSortedSynonyms(Collections.singletonList(SELF_SYNONYM));
            return;
        }

        // Collect all provider synonyms and build synonym -> provider map
        Map<String, WoodProvider> allProvidersMap = new LinkedHashMap<>();
        Set<String> allSynonyms = new LinkedHashSet<>();

        // Collect from mainResource
        WoodProviderBox mainBox = mainResource.getExpectedBox();
        String mainProviderName = mainResource.getProviderName();
        if (mainProviderName != null) {
            allSynonyms.add(mainProviderName);
        }
        if (mainBox != null) {
            for (WoodProvider provider : mainBox.getProviders()) {
                String synonym = provider.getSynonym();
                allSynonyms.add(synonym);
                allProvidersMap.putIfAbsent(synonym, provider);
            }
        }

        // Collect from all resources
        for (JsonResource resource : sys.getResources()) {
            String providerName = resource.getProviderName();
            if (providerName != null) {
                allSynonyms.add(providerName);
            }
            WoodProviderBox box = resource.getExpectedBox();
            if (box != null) {
                for (WoodProvider provider : box.getProviders()) {
                    String synonym = provider.getSynonym();
                    allSynonyms.add(synonym);
                    allProvidersMap.putIfAbsent(synonym, provider);
                }
            }
        }

        // Build dependency graph: graph.get(P) = Set of resources that depend on P
        // If resource R depends on provider P, we add edge P -> R
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();

        // Initialize all nodes with empty adjacency lists and in-degree 0
        for (String synonym : allSynonyms) {
            graph.put(synonym, new LinkedHashSet<>());
            inDegree.put(synonym, 0);
        }

        // Add edges: if resource R has provider P in its box, then R depends on P
        // So we add edge P -> R (P must be loaded before R)
        for (JsonResource resource : sys.getResources()) {
            String resourceProviderName = resource.getProviderName();
            WoodProviderBox box = resource.getExpectedBox();
            if (box != null && resourceProviderName != null) {
                for (WoodProvider provider : box.getProviders()) {
                    String depSynonym = provider.getSynonym();
                    // resourceProviderName depends on depSynonym
                    // Edge: depSynonym -> resourceProviderName
                    graph.computeIfAbsent(depSynonym, k -> new LinkedHashSet<>())
                            .add(resourceProviderName);
                    inDegree.merge(resourceProviderName, 1, Integer::sum);
                }
            }
        }

        // Topological sort using Kahn's algorithm
        Queue<String> queue = new LinkedList<>();
        for (String synonym : allSynonyms) {
            if (inDegree.getOrDefault(synonym, 0) == 0) {
                queue.add(synonym);
            }
        }

        List<String> sortedSynonyms = new LinkedList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sortedSynonyms.add(current);

            // For each node that depends on current, reduce its in-degree
            Set<String> dependents = graph.getOrDefault(current, Collections.emptySet());
            for (String dependent : dependents) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);
                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        // Cycle detection
        if (sortedSynonyms.size() != allSynonyms.size()) {
            Set<String> unsorted = new LinkedHashSet<>(allSynonyms);
            unsorted.removeAll(sortedSynonyms);
            throw new JsonParseException("Cycle detected in provider dependencies: " + unsorted);
        }

        // Load providers in topological order
        for (String synonym : sortedSynonyms) {
            JsonResource existing = sys.findResourcesBySynonym(synonym);
            if (existing != null) {
                continue;
            }

            WoodProvider provider = allProvidersMap.get(synonym);
            if (provider == null) {
                debugLevel.warning(() -> new DebugTuple("Provider not found for synonym: {0}", synonym));
                continue;
            }

            try {
                JsonResource loaded = load(provider, debugLevel);
                sys.addResource(loaded);

                LinkingSet loadedLinkingSet = loaded.getLinkingSet();
                if (loadedLinkingSet != null) {
                    mergeLinkingSets(mainLinkingSet, loadedLinkingSet);
                }
            } catch (FileNotFoundException ex) {
                debugLevel.warning(ex, () -> "Provider file not found: " + provider.getFilename());
            }
        }

        sys.setSortedSynonyms(sortedSynonyms);
    }

    /**
     * Merges the object IDs and links from the source linking set into the target.
     *
     * @param target The linking set to merge into.
     * @param source The linking set to merge from.
     */
    private static void mergeLinkingSets(LinkingSet target, LinkingSet source) {
        if (target == null || source == null) {
            return;
        }
        target.getObjectIdMap().putAll(source.getObjectIdMap());
        target.getLinkMap().putAll(source.getLinkMap());
    }

    /**
     * Attempts to resolve all wood references in a JsonSystem. This method processes the main resource and all
     * registered resources, resolving object references in an iterative manner.
     *
     * <p>
     * Repository descriptors are retrieved directly from the main descriptor's repoDescriptor map, without accessing
     * the JsonModel instances.
     * </p>
     *
     * @param sys The JsonSystem containing resources to resolve.
     * @param descriptor The model descriptor containing type definitions.
     * @param debugLevel The debug level for controlling debug output.
     * @return The WoodResolution containing resolved objects, unresolved keys, and exceptions.
     */
    public static WoodResolution attempt(JsonSystem sys, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) {
        JsonResource container = sys.getMainResource();
        LinkingSet linkingSet = Objects.requireNonNull(container.getLinkingSet(),
                "container.linkingSet must not be null");
        WoodResolution resolution = new WoodResolution();
        Set<String> remainingKeys = new LinkedHashSet<>(linkingSet.getLinkMap().keySet());
        boolean progress = !remainingKeys.isEmpty();
        final List<JsonResource> resources = sys.getResources();
        ConvertService[] services = new ConvertService[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            JsonResource resource = resources.get(i);
            String providerSynonym = resource.getProviderName();
            JsonModelDescriptor resourceDescriptor = descriptor.getRepoDescriptor(providerSynonym);
            if (resourceDescriptor == null) {
                resourceDescriptor = descriptor;
            }
            services[i] = new ConvertService(resource, resourceDescriptor, resolution, debugLevel);
        }
        while (progress) {
            progress = resolveLoop(remainingKeys, services);
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
     * @param services The array of convert services for each resource.
     * @return true if any progress was made (objects were resolved), false otherwise.
     */
    private static boolean resolveLoop(Set<String> remainingKeys, ConvertService[] services) {
        boolean progress = false;
        Set<String> resolvedThisRound = new LinkedHashSet<>();

        int i = services.length;
        while (--i >= 0) {
            final ConvertService service = services[i];
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
                    resolvedThisRound.add(key);
                    progress = true;
                }
            }
        }
        remainingKeys.removeAll(resolvedThisRound);
        return progress;
    }

    /**
     * Checks if a JSON node can be converted now, i.e., all its dependencies are resolved.
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
            String linkKey = node.getLink(providerName);
            if (linkKey != null) {
                if (!resolution.getUnmodifiableResolvedObjects().containsKey(linkKey)) {
                    return false;
                }
            }
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
     * Resolves the context class for a JSON node by extracting the _class field.
     *
     * @param node The JSON node to resolve the class for.
     * @param descriptor The model descriptor for type lookup.
     * @return The resolved type descriptor.
     * @throws JsonParseException If the node is not an object, missing _class, or type not found.
     */
    private static JsonTypeDescriptor resolveContextClass(JsonNode node, JsonModelDescriptor descriptor)
            throws JsonParseException {

        if (node == null || !node.isObject()) {
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
