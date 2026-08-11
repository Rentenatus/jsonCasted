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
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import static de.jare.jsoncasted.lang.JsonTerms.SELF_SYNONYM;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.wood.WoodProvider;
import de.jare.jsoncasted.wood.WoodProviderBox;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * Resolves wood references for already ordered resources. This resolver does
 * not load additional resources. It walks through the resources in order and
 * collects object IDs.
 *
 * @author Janusch Rentenatus
 */
public final class WoodProxyResolver {

    private WoodProxyResolver() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Recursively loads all provider files referenced in the main resource's
     * provider box and merges their linking sets into the main resource's
     * linking set using topological sorting to ensure correct dependency order.
     *
     * <p>
     * This method ensures that all object IDs and links from dependent provider
     * files are available in the main resource before resolution begins. It
     * detects cycles and throws an exception if circular dependencies exist.
     * Multiple dependencies on the same provider are supported.</p>
     *
     * <p>
     * The loading order is determined by the following algorithm:</p>
     * <ol>
     * <li><b>Graph Construction:</b> All provider synonyms are collected from
     * the main resource and all existing resources. For each resource, its
     * provider box defines dependencies: if resource R (with provider name PN)
     * references provider P in its box, an edge P -> PN is added to the
     * dependency graph.</li>
     * <li><b>Topological Sorting:</b> Uses Kahn's algorithm to compute a valid
     * loading order:
     * <ol>
     * <li>Compute in-degree (number of dependencies) for each provider
     * node</li>
     * <li>Start with all nodes having in-degree 0 (no dependencies)</li>
     * <li>Process nodes, reducing in-degree of their dependents</li>
     * <li>Add nodes to the result when their in-degree reaches 0</li>
     * </ol>
     * </li>
     * <li><b>Cycle Detection:</b> If the sorted list contains fewer nodes than
     * the total number of provider synonyms, a cycle exists and a
     * {@code JsonParseException} is thrown with the unsortable providers.</li>
     * <li><b>Ordered Loading:</b> Providers are loaded in the computed
     * topological order, ensuring that all dependencies of a provider are
     * available before the provider itself is loaded.</li>
     * <li><b>LinkingSet Merging:</b> After loading each provider, its linking
     * set (containing object IDs and links) is merged into the main resource's
     * linking set.</li>
     * </ol>
     *
     * <p>
     * Note: This method modifies the main resource's linking set by merging in
     * all object IDs and links from loaded provider resources. Already loaded
     * resources are skipped.</p>
     *
     * @param sys The JsonSystem containing the main resource with provider box.
     * @param debugLevel The debug level for controlling debug output.
     * @throws IOException If I/O errors occur during loading.
     * @throws JsonParseException If parsing fails for any provider file, or if
     * a cycle is detected in provider dependencies.
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
     * Merges the object IDs and links from the source linking set into the
     * target.
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

}
