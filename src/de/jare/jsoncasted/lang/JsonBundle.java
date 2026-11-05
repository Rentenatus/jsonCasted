/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.parserservice.JsonParserService;
import de.jare.jsoncasted.io.parserservice.WoodIdFinder;
import de.jare.jsoncasted.wood.WoodProvider;
import de.jare.jsoncasted.wood.WoodProviderBox;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 * A long-lived bundle above {@link JsonResource} (concept doc/JSON_BUNDLE.md): it guarantees {@code B == B'} -
 * every file of the bundle is parsed exactly once, held once and shared by all consuming resources, at parse
 * time as well as later in the editor session.
 *
 * <p>
 * Identity is a two-key registry: the canonical path is the instance identity (same file, same instance); the
 * provider synonym is the ID namespace ({@code synonym::objectId}) and can be owned by exactly one file. The
 * registration matrix follows the concept: same path and synonym share; same path under a new synonym is an
 * alias (first synonym wins, the linking set is rebuilt under the requesting namespace, a warning is
 * collected - refused when the resource is already CONSUMED, because a rebuild would invalidate resolved
 * references); the same synonym on a different path throws a {@link JsonParseException}.
 * </p>
 *
 * <p>
 * The bundle owns all resources for its lifetime (v1: no refcounting). It consumes the parser service for
 * loading but never the convert service - the io convert layer consumes the bundle (phase 2 of
 * doc/PLAN_JsonBundle.md). Fingerprints are captured at load; the FRESH/STALE and REGISTERED/CONSUMED states
 * live on the {@link JsonResource} itself, the bundle only aggregates and coordinates.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class JsonBundle {

    /**
     * One registry entry: the shared instance with its canonical path, its first (identity-winning)
     * synonym and the OWN Wood ID keys of the resource at registration time (bundle scoping: the wire
     * supervision compares what a namespace OWNS, never the merged resolution set of a receiver).
     */
    private static final class Entry {

        private final String canonicalPath;
        private final String firstSynonym;
        private final Set<String> ownIdKeys;
        private JsonResource resource;

        private Entry(String canonicalPath, String firstSynonym, JsonResource resource) {
            this.canonicalPath = canonicalPath;
            this.firstSynonym = firstSynonym;
            this.resource = resource;
            this.ownIdKeys = ownIdKeysOf(resource);
        }

        private static Set<String> ownIdKeysOf(JsonResource resource) {
            final Set<String> keys = new LinkedHashSet<>();
            final LinkingSet set = resource.getLinkingSet();
            if (set != null) {
                keys.addAll(set.getObjectIdMap().keySet());
            }
            return keys;
        }
    }

    private final JsonDebugLevel debug;
    private final JsonResource mainResource;
    private final Map<String, Entry> byPath = new LinkedHashMap<>();
    // Bundle-scoped IDs (concept WoodJsonJack CONCEPT_ProjectView.md section 7, strategy B): the synonym
    // registry is namespaced - the label empty string is the session default (single-world behavior of a
    // session without a project layer), every bundle label owns its own synonym world.
    private final Map<String, Map<String, String>> synonymToPathByNamespace = new LinkedHashMap<>();
    // The import wiring of the descriptor input structure (concept section 6/7): the asking bundle may
    // resolve into the wired namespaces - cross-bundle resolution exists nowhere else (strategy B).
    private final Map<String, Set<String>> wiredNamespaces = new LinkedHashMap<>();
    private final List<String> warnings = new ArrayList<>();
    // The wiring layer's file-to-namespace decision (namespaced open lane); null in the legacy lane.
    private java.util.function.Function<File, String> providerNamespaceResolver;
    private boolean closed;

    /**
     * The key of the session default namespace: every session without a project layer lives here, and
     * resources without a namespace label belong to it. Sessions with the default namespace only behave
     * exactly as before bundle scoping.
     */
    public static final String DEFAULT_NAMESPACE = "";

    /**
     * Opens the bundle of a main file: parses the main resource, walks its provider graph transitively, loads
     * and registers every referenced file exactly once, captures each fingerprint and merges the providers'
     * linking sets into the main resource's set (the shared instances keep their own clean per-resource sets -
     * concept hygiene rule).
     *
     * @param mainFile the main resource file, not null.
     * @param debug the debug level, not null.
     * @return the open bundle.
     * @throws IOException if a file cannot be read.
     * @throws JsonParseException if a file fails to parse, a provider synonym conflict or a dependency cycle
     * appears.
     */
    public static JsonBundle open(File mainFile, JsonDebugLevel debug) throws IOException, JsonParseException {
        Objects.requireNonNull(mainFile, "mainFile must not be null");
        Objects.requireNonNull(debug, "debug must not be null");

        final JsonResource main = loadSelf(mainFile.getPath(), debug);
        final JsonBundle bundle = new JsonBundle(main, debug);
        bundle.register(main);
        bundle.loadProviderGraph(debug);
        return bundle;
    }

    /**
     * Opens the bundle in the namespaced mode (concept WoodJsonJack CONCEPT_ProjectView.md section 7,
     * strategy B - phase 5 engine prerequisite): the main file carries its bundle's namespace label and
     * every provider file is labeled by the caller's resolver before registration - files of the same
     * bundle return the same label (null falls back to the main namespace), files of other bundles return
     * their own. Registration, lookup and resolution are namespaced from here on; the legacy
     * {@link #open(File, JsonDebugLevel)} lane (all default namespace) stays byte-identical.
     *
     * @param mainFile the main resource file, not null.
     * @param mainNamespace the namespace label of the opened bundle (its bundle name), null forbidden.
     * @param providerNamespaceResolver decides the owning bundle of a provider file (the wiring layer -
     * the engine never guesses membership); null return means the same namespace as the main.
     * @param debug the debug level, not null.
     * @return the open bundle.
     * @throws IOException if a file cannot be read.
     * @throws JsonParseException if a file fails to parse, a provider synonym conflict within a namespace,
     * a supervised ID collision of wired namespaces or a dependency cycle appears.
     */
    public static JsonBundle open(File mainFile, String mainNamespace,
            java.util.function.Function<File, String> providerNamespaceResolver, JsonDebugLevel debug)
            throws IOException, JsonParseException {
        Objects.requireNonNull(mainFile, "mainFile must not be null");
        Objects.requireNonNull(mainNamespace, "mainNamespace must not be null");
        Objects.requireNonNull(providerNamespaceResolver, "providerNamespaceResolver must not be null");
        Objects.requireNonNull(debug, "debug must not be null");

        final JsonResource main = loadSelf(mainFile.getPath(), debug);
        main.setNamespace(mainNamespace);
        final JsonBundle bundle = new JsonBundle(main, debug);
        bundle.providerNamespaceResolver = providerNamespaceResolver;
        bundle.register(main);
        bundle.loadProviderGraph(debug);
        return bundle;
    }

    /**
     * Wires the import edge of the descriptor input structure (concept section 6/7): the importing
     * namespace may resolve resources of the imported namespace. Cross-bundle resolution exists only
     * through this explicit wiring - never implicitly (strategy B). The merge supervision guards every
     * join of two namespaces in one resolution scope against colliding Wood ID keys.
     *
     * @param importingNamespace the asking bundle's namespace label, not null.
     * @param importedNamespace the imported bundle's namespace label, not null.
     * @throws JsonParseException when both namespaces' registered ID keys collide - the explicit
     * {@code RerollID} preparation (concept section 7.1) is the remedy, never a silent overwrite.
     */
    public void wire(String importingNamespace, String importedNamespace) throws JsonParseException {
        Objects.requireNonNull(importingNamespace, "importingNamespace must not be null");
        Objects.requireNonNull(importedNamespace, "importedNamespace must not be null");
        requireOpen();
        superviseNamespaceJoin(importingNamespace, importedNamespace);
        wiredNamespaces.computeIfAbsent(importingNamespace, k -> new LinkedHashSet<>()).add(importedNamespace);
    }

    /**
     * Loads one file as the given synonym - the single load rail of the bundle (same semantics the convert
     * layer uses today: file becomes the resourceFile, synonym becomes the provider name).
     */
    private static JsonResource load(File file, String synonym, JsonDebugLevel debug)
            throws FileNotFoundException, IOException, JsonParseException {
        final JsonResource container = JsonResource.forFile(file.getPath());
        container.setProviderName(synonym);
        try (FileReader reader = new FileReader(file)) {
            return JsonParserService.parse(reader, container, debug);
        }
    }

    /**
     * Loads the main file - the resource keeps its default provider name (self), the concept's file truth.
     */
    private static JsonResource loadSelf(String filename, JsonDebugLevel debug)
            throws FileNotFoundException, IOException, JsonParseException {
        final JsonResource container = JsonResource.forFile(filename);
        try (FileReader reader = new FileReader(filename)) {
            return JsonParserService.parse(reader, container, debug);
        }
    }

    private JsonBundle(JsonResource mainResource, JsonDebugLevel debug) {
        this.mainResource = mainResource;
        this.debug = debug;
    }

    /**
     * Returns the bundle's main resource.
     *
     * @return the main resource, not null while open.
     */
    public JsonResource getMainResource() {
        return mainResource;
    }

    /**
     * Closes the bundle: releases every held resource.
     */
    public void close() {
        byPath.clear();
        synonymToPathByNamespace.clear();
        wiredNamespaces.clear();
        mainResource.setLinkingSet(null);
        closed = true;
    }

    /**
     * Returns the bundle's alias and reconciliation warnings (concept identity rules).
     *
     * @return unmodifiable warning lines, empty when none.
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Registers a resource and returns the instance that owns its canonical path - the shared instance on a
     * path hit (the passed instance is discarded then), the passed instance itself on a miss. The identity
     * rules and provider-name reconciliation follow the concept matrix; under bundle scoping (strategy B)
     * the synonym ownership check is namespaced: the same synonym may be owned by different files as long
     * as they belong to different bundles - within one namespace the matrix holds unchanged.
     *
     * @param resource the loaded resource, not null.
     * @return the registered instance for the resource's path.
     * @throws JsonParseException on a synonym conflict within the namespace (another file of the same
     * bundle owns the namespace) or on an alias for an already consumed instance.
     */
    public JsonResource register(JsonResource resource) throws JsonParseException {
        Objects.requireNonNull(resource, "resource must not be null");
        requireOpen();

        final String path = canonicalPathOf(resource);
        final String synonym = resource.getProviderName();
        final String namespaceKey = namespaceKey(resource);
        final Entry entry = path == null ? null : byPath.get(path);
        if (entry != null) {
            return aliasOrShare(entry, resource, synonym);
        }
        if (isSynonymTaken(namespaceKey, synonym, null)) {
            final JsonResource owner = existingForSynonym(namespaceKey, synonym);
            throw new JsonParseException("Synonym '" + synonym + "' is already taken by '"
                    + (owner != null ? owner.getResourceFile() : "<unknown>")
                    + "' in namespace '" + namespaceLabel(namespaceKey)
                    + "' - another file cannot own the ID namespace");
        }
        resource.captureFingerprint();
        final Entry fresh = new Entry(path, synonym, resource);
        if (path != null) {
            byPath.put(path, fresh);
        }
        synonymsOf(namespaceKey).put(synonym, path);
        return resource;
    }

    /**
     * Path hit, the file is already registered. Same synonym shares the instance (the normal case). A different
     * synonym is the alias case of the concept: the first synonym wins (instance identity and registry name
     * stay), the linking set is rebuilt under the requesting namespace - idempotent, scan-only - and a warning
     * is collected; an alias for an already CONSUMED instance is refused with an exception.
     */
    private JsonResource aliasOrShare(Entry entry, JsonResource requested, String requestedSynonym)
            throws JsonParseException {
        final JsonResource shared = entry.resource;
        if (entry.firstSynonym.equals(requestedSynonym)) {
            // shared path, shared synonym: both namespaces may reference the instance (bundle scoping - the
            // instance identity is the path, the synonym ownership is per namespace)
            synonymsOf(namespaceKey(requested)).put(requestedSynonym, entry.canonicalPath);
            return shared;
        }
        if (shared.isConsumed()) {
            throw new JsonParseException("Alias refused: '" + entry.canonicalPath + "' is already consumed by a "
                    + "resolution - rebuilding the linking set for synonym '" + requestedSynonym + "' would "
                    + "invalidate resolved references");
        }
        if (shared.hasRoot()) {
            shared.setLinkingSet(WoodIdFinder.buildLinkingSet(shared.getRoot(), requestedSynonym, debug));
        }
        synonymToPathPut(requested, requestedSynonym, entry.canonicalPath);
        warnings.add("Alias: '" + entry.canonicalPath + "' is registered as '" + entry.firstSynonym
                + "' and was also referenced as '" + requestedSynonym + "' - the linking set was rebuilt,"
                + " the first synonym wins");
        return shared;
    }

    /**
     * The synonym registration of the alias case under the requesting resource's namespace (strategy B).
     */
    private void synonymToPathPut(JsonResource requested, String synonym, String canonicalPath) {
        synonymsOf(namespaceKey(requested)).put(synonym, canonicalPath);
    }

    /**
     * Returns the registered instance of the canonical path.
     *
     * @param canonicalPath the canonical path, not null.
     * @return the shared instance, or null when unknown.
     */
    public JsonResource findByPath(String canonicalPath) {
        requireOpen();
        final Entry entry = byPath.get(canonicalPath);
        return entry != null ? entry.resource : null;
    }

    /**
     * Returns the registered instance owning the synonym (identity namespace, also via a reconciled alias).
     *
     * @param synonym the provider synonym, not null.
     * @return the instance owning the synonym, or null when unknown.
     */
    /**
     * Returns the registered instance owning the synonym in the session default namespace - the legacy
     * single-world lookup (identical behavior when no namespaces are in play).
     *
     * @param synonym the provider synonym, not null.
     * @return the instance owning the synonym, or null when unknown.
     */
    public JsonResource findBySynonym(String synonym) {
        return findBySynonym(DEFAULT_NAMESPACE, synonym);
    }

    /**
     * Returns the registered instance owning the synonym within a namespace (bundle scoping, strategy
     * B): {@code save::…} of bundle one and {@code save::…} of bundle two are different worlds.
     *
     * @param namespace the namespace label of the owning bundle, null for the session default.
     * @param synonym the provider synonym, not null.
     * @return the instance owning the synonym in that namespace, or null when unknown.
     */
    public JsonResource findBySynonym(String namespace, String synonym) {
        requireOpen();
        final JsonResource resource = existingForSynonym(namespaceKey(namespace), synonym);
        if (resource == null) {
            return null;
        }
        final String path = canonicalPathOf(resource);
        final Entry entry = path != null ? byPath.get(path) : null;
        return entry != null ? entry.resource : resource;
    }

    /**
     * Tells whether the synonym is already an owned ID namespace within the namespace of the registering
     * resource (strategy B: the check is namespaced).
     *
     * @param namespaceKey the internal namespace key of the registering resource, not null.
     * @param synonym the synonym to check, not null.
     * @param exceptPath a canonical path that does not count as taken (the same file re-referencing itself),
     * may be null.
     * @return true when another file of the same namespace owns the synonym.
     */
    public boolean isSynonymTaken(String namespaceKey, String synonym, String exceptPath) {
        requireOpen();
        final String ownerPath = synonymsOf(namespaceKey).get(synonym);
        if (ownerPath == null) {
            return false;
        }
        return !Objects.equals(ownerPath, exceptPath);
    }

    /**
     * Replaces the instance of a canonical path with a fresh parse (concept reload): the shared instance
     * changes for every future consumer, the state returns to REGISTERED + FRESH (fingerprint captures the new
     * load state). Consumers that already hold the instance keep their reference - coordinating them is the
     * CONSUMED+STALE policy, not the reload.
     *
     * @param canonicalPath the canonical path of the resource to reload, not null.
     * @param debug the debug level for the load.
     * @throws IOException if the file cannot be read.
     * @throws JsonParseException if the file fails to parse or is unknown to the registry.
     */
    public void reload(String canonicalPath, JsonDebugLevel debug) throws IOException, JsonParseException {
        requireOpen();
        final Entry entry = byPath.get(canonicalPath);
        if (entry == null) {
            throw new JsonParseException("Unknown resource: '" + canonicalPath + "' cannot be reloaded");
        }
        final File file = new File(canonicalPath);
        final JsonResource fresh;
        if (isMain(entry)) {
            fresh = loadSelf(mainResource.getResourceFile(), debug);
        } else {
            fresh = load(file, entry.firstSynonym, debug);
        }
        fresh.captureFingerprint();
        entry.resource = fresh;
    }

    private boolean isMain(Entry entry) {
        return entry != null && entry.resource == mainResource;
    }

    /**
     * Returns the topologically sorted provider synonyms (Kahn's algorithm, as the convert layer computes them
     * today): dependencies first, the main last. The order is computed from the registered resources' provider
     * boxes.
     *
     * @return the sorted synonyms, not null.
     * @throws JsonParseException when the provider graph contains a cycle.
     */
    public List<String> getSortedSynonyms() throws JsonParseException {
        requireOpen();

        final Set<String> allSynonyms = new LinkedHashSet<>();
        final Map<String, WoodProvider> allProviders = new LinkedHashMap<>();
        collectGraph(mainResource, allSynonyms, allProviders);
        for (Entry entry : byPath.values()) {
            collectGraph(entry.resource, allSynonyms, allProviders);
        }

        // edge dependency -> dependent: if R (provider box) references P, P is loaded before R
        final Map<String, Set<String>> graph = new LinkedHashMap<>();
        final Map<String, Integer> inDegree = new LinkedHashMap<>();
        for (String synonym : allSynonyms) {
            graph.put(synonym, new LinkedHashSet<>());
            inDegree.put(synonym, 0);
        }
        for (Entry entry : byPath.values()) {
            final String resourceSynonym = entry.resource.getProviderName();
            final WoodProviderBox box = entry.resource.getExpectedBox();
            if (box != null && resourceSynonym != null) {
                for (WoodProvider provider : box.getProviders()) {
                    final String dependency = provider.getSynonym();
                    if (graph.containsKey(dependency) && graph.containsKey(resourceSynonym)
                            && graph.get(dependency).add(resourceSynonym)) {
                        inDegree.merge(resourceSynonym, 1, Integer::sum);
                    }
                }
            }
        }

        final Queue<String> queue = new LinkedList<>();
        for (String synonym : allSynonyms) {
            if (inDegree.getOrDefault(synonym, 0) == 0) {
                queue.add(synonym);
            }
        }
        final List<String> sorted = new LinkedList<>();
        while (!queue.isEmpty()) {
            final String current = queue.poll();
            sorted.add(current);
            for (String dependent : graph.get(current)) {
                final int degree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, degree);
                if (degree == 0) {
                    queue.add(dependent);
                }
            }
        }
        if (sorted.size() != allSynonyms.size()) {
            final Set<String> unsorted = new LinkedHashSet<>(allSynonyms);
            unsorted.removeAll(sorted);
            throw new JsonParseException("Cycle detected in provider dependencies: " + unsorted);
        }
        return new ArrayList<>(sorted);
    }

    /**
     * Returns the synonyms that depend on the given synonym - the reverse dependency graph (concept
     * {@code getDependents}): who references resources of that namespace.
     *
     * @param synonym the provider synonym, not null.
     * @return the synonyms whose provider boxes reference the synonym, empty when none.
     */
    public Set<String> getDependents(String synonym) {
        requireOpen();
        final Set<String> dependents = new LinkedHashSet<>();
        for (Entry entry : byPath.values()) {
            final WoodProviderBox box = entry.resource.getExpectedBox();
            if (box == null) {
                continue;
            }
            for (WoodProvider provider : box.getProviders()) {
                if (synonym.equals(provider.getSynonym())) {
                    final String resourceSynonym = entry.resource.getProviderName();
                    if (resourceSynonym != null) {
                        dependents.add(resourceSynonym);
                    }
                }
            }
        }
        return dependents;
    }

    /**
     * Bridges the bundle into the existing resolution: a {@link JsonSystem} for one resource with its
     * transitive provider closure (the resource itself plus every provider reachable through the boxes) -
     * the same set a per-resource parse session would hold, but built from the SHARED instances. The sorted
     * synonyms of the whole bundle graph serve as the order; synonyms outside the closure simply find no
     * resource in the system.
     *
     * @param main the resource to convert - the bundle's main normally, any sub resource in the editor.
     * @return the system, not null.
     * @throws JsonParseException when the provider graph contains a cycle.
     */
    public JsonSystem toSystem(JsonResource main) throws JsonParseException {
        Objects.requireNonNull(main, "main must not be null");
        requireOpen();
        final JsonSystem sys = JsonSystem.of(main.getExpectedBox(), main);
        final Set<String> queued = new LinkedHashSet<>();
        final Deque<String> queue = new ArrayDeque<>();
        if (main.getNamespace() != null) {
            // bundle scoping (strategy B): resolution happens relative to the asking bundle - own
            // namespace first, then the explicitly wired import namespaces (concept section 6/7)
            enqueueScopedSynonyms(main.getNamespace(), main, queue, queued);
            while (!queue.isEmpty()) {
                final String synonym = queue.poll();
                final JsonResource resource = resolveInScope(main.getNamespace(), synonym);
                if (resource == null || sys.getResources().contains(resource)) {
                    continue;
                }
                sys.addResource(resource);
                enqueueScopedSynonyms(main.getNamespace(), resource, queue, queued);
            }
            sys.setSortedSynonyms(sortedSynonymsOf(sys.getResources()));
            return sys;
        }
        enqueueSynonyms(main, queue, queued);
        while (!queue.isEmpty()) {
            final String synonym = queue.poll();
            final JsonResource resource = existingForSynonym(DEFAULT_NAMESPACE, synonym);
            if (resource == null || sys.getResources().contains(resource)) {
                continue;
            }
            sys.addResource(resource);
            enqueueSynonyms(resource, queue, queued);
        }
        sys.setSortedSynonyms(getSortedSynonyms());
        return sys;
    }

    /**
     * Queues the resource's box provider synonyms in namespaced mode, deduped per system build. The queue
     * key stays the bare synonym - the scope lookup resolves it.
     */
    private void enqueueScopedSynonyms(String namespace, JsonResource resource, Deque<String> queue,
            Set<String> queued) {
        final WoodProviderBox box = resource.getExpectedBox();
        if (box == null) {
            return;
        }
        for (WoodProvider provider : box.getProviders()) {
            if (queued.add(provider.getSynonym())) {
                queue.add(provider.getSynonym());
            }
        }
    }

    /**
     * Resolution scope of the asking namespace (strategy B): the own namespace first, then the wired
     * import namespaces (breadth, cycle-safe). The default namespace resolves globally as before when the
     * asking resource carries no label.
     */
    private JsonResource resolveInScope(String asking, String synonym) {
        final JsonResource own = existingForSynonym(namespaceKey(asking), synonym);
        if (own != null) {
            return own;
        }
        final Set<String> visited = new LinkedHashSet<>();
        final Deque<String> pending = new ArrayDeque<>(wiredNamespaces.getOrDefault(
                namespaceKey(asking), Collections.emptySet()));
        while (!pending.isEmpty()) {
            final String candidate = pending.poll();
            if (!visited.add(candidate)) {
                continue;
            }
            final JsonResource found = existingForSynonym(candidate, synonym);
            if (found != null) {
                return found;
            }
            pending.addAll(wiredNamespaces.getOrDefault(candidate, Collections.emptySet()));
        }
        return null;
    }

    /**
     * Topological order over the given resources only (Kahn's algorithm, in-system order of the namespaced
     * lane): dependencies first, the asking resource's own provider name last.
     */
    private List<String> sortedSynonymsOf(List<JsonResource> resources) throws JsonParseException {
        final Set<String> allSynonyms = new LinkedHashSet<>();
        final Map<String, WoodProvider> allProviders = new LinkedHashMap<>();
        for (JsonResource resource : resources) {
            collectGraph(resource, allSynonyms, allProviders);
        }
        final Map<String, Set<String>> graph = new LinkedHashMap<>();
        final Map<String, Integer> inDegree = new LinkedHashMap<>();
        for (String synonym : allSynonyms) {
            graph.put(synonym, new LinkedHashSet<>());
            inDegree.put(synonym, 0);
        }
        for (JsonResource resource : resources) {
            final String resourceSynonym = resource.getProviderName();
            final WoodProviderBox box = resource.getExpectedBox();
            if (box != null && resourceSynonym != null) {
                for (WoodProvider provider : box.getProviders()) {
                    final String dependency = provider.getSynonym();
                    if (graph.containsKey(dependency) && graph.containsKey(resourceSynonym)
                            && graph.get(dependency).add(resourceSynonym)) {
                        inDegree.merge(resourceSynonym, 1, Integer::sum);
                    }
                }
            }
        }
        final Queue<String> queue = new LinkedList<>();
        for (String synonym : allSynonyms) {
            if (inDegree.getOrDefault(synonym, 0) == 0) {
                queue.add(synonym);
            }
        }
        final List<String> sorted = new LinkedList<>();
        while (!queue.isEmpty()) {
            final String current = queue.poll();
            sorted.add(current);
            for (String dependent : graph.get(current)) {
                final int degree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, degree);
                if (degree == 0) {
                    queue.add(dependent);
                }
            }
        }
        if (sorted.size() != allSynonyms.size()) {
            final Set<String> unsorted = new LinkedHashSet<>(allSynonyms);
            unsorted.removeAll(sorted);
            throw new JsonParseException("Cycle detected in provider dependencies: " + unsorted);
        }
        return sorted;
    }

    /**
     * Queues the resource's box provider synonyms, deduped per system build.
     */
    private void enqueueSynonyms(JsonResource resource, Deque<String> queue, Set<String> queued) {
        final WoodProviderBox box = resource.getExpectedBox();
        if (box == null) {
            return;
        }
        for (WoodProvider provider : box.getProviders()) {
            if (queued.add(provider.getSynonym())) {
                queue.add(provider.getSynonym());
            }
        }
    }

    /**
     * The registry-first load lane for the convert layer (doc/JSON_BUNDLE.md, integration step 2): a synonym
     * request is served from the registry when possible - first by synonym, then by path (the alias rules
     * decide), only a double miss actually parses the file and registers the fresh instance. The io layer
     * calls this instead of loading, so {@code B == B'} holds across resolutions.
     *
     * @param synonym the requesting provider synonym (the ID namespace), not null.
     * @param file the provider file, not null.
     * @param debug the debug level for the load.
     * @return the shared instance for the file, fresh on a double miss.
     * @throws IOException if the file cannot be read on a miss.
     * @throws JsonParseException if the parse or an identity rule fails (synonym conflict, consumed alias).
     */
    public JsonResource acquire(String synonym, File file, JsonDebugLevel debug)
            throws IOException, JsonParseException {
        return acquire(DEFAULT_NAMESPACE, synonym, file, debug);
    }

    /**
     * Namespaced variant of the registry-first load lane (bundle scoping, strategy B): the synonym request
     * is served from the requesting namespace, a fresh load registers under it. This is the correct lane
     * for provider files of the asking bundle (the descriptor files); files of OTHER bundles enter a
     * session only through explicit wiring - pre-registered with their own namespace by the wiring layer
     * (concept section 6/7).
     *
     * @param namespace the asking bundle's namespace label, null for the session default.
     * @param synonym the requesting provider synonym (the ID namespace), not null.
     * @param file the provider file, not null.
     * @param debug the debug level for the load.
     * @return the shared instance for the file, fresh on a double miss.
     * @throws IOException if the file cannot be read on a miss.
     * @throws JsonParseException if the parse or an identity rule fails (synonym conflict within the
     * namespace, consumed alias).
     */
    public JsonResource acquire(String namespace, String synonym, File file, JsonDebugLevel debug)
            throws IOException, JsonParseException {
        Objects.requireNonNull(synonym, "synonym must not be null");
        Objects.requireNonNull(file, "file must not be null");
        requireOpen();

        final String namespaceKey = namespaceKey(namespace);
        JsonResource shared = findBySynonym(namespaceKey, synonym);
        if (shared != null) {
            return reuseChecked(shared, synonym, debug);
        }
        final String canonicalPath = canonicalPathOf(file);
        if (canonicalPath != null) {
            final JsonResource byPath = findByPath(canonicalPath);
            if (byPath != null) {
                final JsonResource reconciled = byPath.getProviderName().equals(synonym)
                        ? byPath : register(forSynonym(byPath, synonym));
                return reuseChecked(reconciled, synonym, debug);
            }
        }
        final JsonResource fresh = load(file, synonym, debug);
        if (namespace != null) {
            fresh.setNamespace(namespace);
        }
        return register(fresh);
    }

    /**
     * Every reuse verifies the fingerprint against the file (concept doc/JSON_BUNDLE.md state model): the
     * combination decides. REGISTERED+STALE silently reloads - nobody consumed the old state, no resolution
     * can lose ground; the fresh instance returns to REGISTERED+FRESH. CONSUMED+STALE warns and KEEPS the
     * instance (decided open question 5: warn + keep) - a reload would silently invalidate already resolved
     * references. A vanished file keeps the cache too: there is nothing to reload, the registry stays usable
     * for the rest of the session.
     */
    private JsonResource reuseChecked(JsonResource shared, String synonym, JsonDebugLevel debug)
            throws IOException, JsonParseException {
        if (!shared.isStale()) {
            return shared;
        }
        if (shared.isConsumed()) {
            warnings.add("CONSUMED + STALE: the file of '" + synonym
                    + "' changed under a consumed cache - the shared instance stays, a reload would "
                    + "invalidate resolved references");
            return shared;
        }
        final String canonicalPath = shared.hasResourceFile()
                ? canonicalPathOf(new File(shared.getResourceFile())) : null;
        if (canonicalPath == null) {
            return shared;
        }
        try {
            reload(canonicalPath, debug);
        } catch (FileNotFoundException ex) {
            warnings.add("The file of '" + synonym
                    + "' vanished under the cache - the registered instance stays for this session");
            return shared;
        }
        final JsonResource fresh = findByPath(canonicalPath);
        return fresh != null ? fresh : shared;
    }

    /**
     * Walks the main resource's provider graph transitively: registry lookup first, load and register on a
     * miss, queue the loaded providers' own boxes - every file exactly once, alias rules on re-references.
     */
    private void loadProviderGraph(JsonDebugLevel debug) throws IOException, JsonParseException {
        final Deque<ProviderRef> queue = new ArrayDeque<>();
        final Map<String, String> queuedFilenames = new LinkedHashMap<>();
        enqueueProviders(mainResource, queue, queuedFilenames);
        final String mainNamespaceKey = namespaceKey(mainResource);

        while (!queue.isEmpty()) {
            final ProviderRef ref = queue.poll();
            final String synonym = ref.provider.getSynonym();
            final File file = providerFileOf(ref.owner, ref.provider);

            JsonResource shared;
            try {
                shared = findBySynonym(mainNamespaceKey, synonym);
                if (shared == null) {
                    final String path = canonicalPathOf(file);
                    shared = path != null ? findByPath(path) : null;
                }
                if (shared == null) {
                    shared = register(withNamespace(
                            load(file, synonym, debug), file));
                } else {
                    shared = register(forSynonym(shared, synonym));
                }
            } catch (FileNotFoundException ex) {
                // the same tolerance the convert layer lives by (WoodProxyResolver warns and continues): a
                // missing provider file is a warning, not a bundle failure - dangling references are a case
                // for the validator
                warnings.add("Provider file not found: " + file);
                continue;
            }
            mergeIntoMain(shared);
            enqueueProviders(shared, queue, queuedFilenames);
        }
    }

    /**
     * One queued provider entry with its declaring owner: the resolution base of the filename travels
     * with the entry (strict file-relative resolution, concept CONCEPT_ProjectView.md - the provider
     * filename belongs to the file that declares it, never to the working directory of the process).
     */
    private static final class ProviderRef {

        final WoodProvider provider;
        final JsonResource owner;

        ProviderRef(WoodProvider provider, JsonResource owner) {
            this.provider = provider;
            this.owner = owner;
        }
    }

    /**
     * The strict file-relative resolution of a provider filename (binding): a relative filename
     * resolves against the folder of the file that declares it; an absolute filename passes through
     * unchanged. Before this rule a relative filename resolved against the working directory - a
     * bundle only loaded completely when the editor happened to run in the right folder, and the
     * editor session silently missed provider trees elsewhere.
     *
     * @param owner the resource whose box declares the provider entry (the resolution base)
     * @param provider the provider entry with the filename
     * @return the provider file, absolute or relative-to-owner as declared
     */
    private static File providerFileOf(JsonResource owner, WoodProvider provider) {
        final String filename = provider.getFilename();
        final File raw = new File(filename);
        if (raw.isAbsolute() || !owner.hasResourceFile()) {
            return raw;
        }
        return new File(new File(owner.getResourceFile()).getParentFile(), filename);
    }

    /**
     * Namespaced labeling of the load lane: the wiring layer's resolver decides the owning bundle of a
     * fresh provider file (null = the same namespace as the main, the descriptor files case); in the
     * legacy lane no label is set and everything stays in the session default namespace.
     */
    private JsonResource withNamespace(JsonResource resource, File source) {
        if (providerNamespaceResolver != null && resource.getNamespace() == null) {
            final String resolved = providerNamespaceResolver.apply(source);
            resource.setNamespace(resolved != null ? resolved : mainResource.getNamespace());
        }
        return resource;
    }

    /**
     * The alias-compatible re-reference: a provider box entry references an already registered path under a
     * (possibly) new synonym - register() runs the reconciliation rules on it.
     */
    private JsonResource forSynonym(JsonResource shared, String synonym) {
        if (shared.getProviderName().equals(synonym)) {
            return shared;
        }
        final JsonResource request = JsonResource.forFile(shared.getResourceFile());
        request.setProviderName(synonym);
        return request;
    }

    /**
     * Queues the resource's box providers: deduped by synonym (a re-reference of the same file is the normal
     * share case), but the concept's namespace rule holds at queue time already - the same synonym owned by a
     * DIFFERENT file within the SAME namespace (strategy B) is a conflict and throws. Bundles of different
     * namespaces may own the same synonym - the definitive check runs at registration, per namespace.
     */
    private void enqueueProviders(JsonResource resource, Deque<ProviderRef> queue,
            Map<String, String> queuedFilenames) throws JsonParseException {
        final WoodProviderBox box = resource.getExpectedBox();
        if (box == null) {
            return;
        }
        for (WoodProvider provider : box.getProviders()) {
            final String filename = canonicalPathOf(providerFileOf(resource, provider));
            final String queueKey = namespaceKey(resource) + "\u0000" + provider.getSynonym();
            final String known = queuedFilenames.putIfAbsent(queueKey, filename);
            if (known == null) {
                queue.add(new ProviderRef(provider, resource));
            } else if (!known.equals(filename)) {
                throw new JsonParseException("Synonym '" + provider.getSynonym() + "' is referenced with two "
                        + "files ('" + known + "' and '" + filename + "') - the ID namespace cannot be owned "
                        + "by two files");
            }
        }
    }

    /**
     * LinkingSet hygiene of the concept: foreign keys go into the main resource's set only - a shared instance
     * never accumulates another resource's keys. Under bundle scoping (strategy B) the merge is the
     * supervision point of cross-bundle wiring: joining two namespaces in one resolution scope refuses on
     * colliding Wood ID keys ({@code syn::id} of both sides overlap) - no silent overwrite, the explicit
     * {@code RerollID} preparation (concept WoodJsonJack CONCEPT_ProjectView.md section 7.1) is the remedy.
     * Within one namespace the merge behaves as before.
     */
    private void mergeIntoMain(JsonResource shared) throws JsonParseException {
        mergeSupervised(mainResource, mainResource.getLinkingSet(), shared, shared.getLinkingSet());
    }

    /**
     * The supervised merge of the io layer too (bundle scoping, strategy B): keyset hygiene of the concept
     * - foreign keys go into the receiving set only - plus the collision refusal at the join of two
     * namespaces in one resolution scope. Within one namespace the merge behaves as before.
     *
     * @param targetOwner the resource whose set receives (the asking bundle), not null.
     * @param target the receiving linking set, null is a no-op.
     * @param sourceOwner the resource whose set is merged in, not null.
     * @param source the merged linking set, null is a no-op.
     * @throws JsonParseException when both namespaces are different and their Wood ID keys collide - the
     * explicit {@code RerollID} preparation (concept WoodJsonJack CONCEPT_ProjectView.md section 7.1)
     * is the remedy, never a silent overwrite.
     */
    public static void mergeSupervised(JsonResource targetOwner, LinkingSet target,
            JsonResource sourceOwner, LinkingSet source) throws JsonParseException {
        if (target == null || source == null || targetOwner == sourceOwner) {
            return;
        }
        final String targetKey = namespaceKeyOf(targetOwner);
        final String sourceKey = namespaceKeyOf(sourceOwner);
        if (!targetKey.equals(sourceKey)) {
            final Set<String> collisions = new LinkedHashSet<>();
            for (String key : source.getObjectIdMap().keySet()) {
                if (target.getObjectIdMap().containsKey(key)) {
                    collisions.add(key);
                }
            }
            if (!collisions.isEmpty()) {
                throw new JsonParseException("Wood ID collision of namespaces '" + targetKey + "' and '"
                        + sourceKey + "': " + collisions
                        + " - the bundles collide in one resolution scope; the explicit RerollID "
                        + "preparation is the remedy, not a silent overwrite");
            }
        }
        target.getObjectIdMap().putAll(source.getObjectIdMap());
        target.getLinkMap().putAll(source.getLinkMap());
    }

    /**
     * The wire-time supervision (concept section 7.1): joining the namespaces' registered ID key sets
     * refuses on overlap - the reroll is the explicit preparation.
     */
    private void superviseNamespaceJoin(String importing, String imported) throws JsonParseException {
        final Set<String> importingKeys = registeredIdKeys(importing);
        final Set<String> importedKeys = registeredIdKeys(imported);
        final Set<String> collisions = new LinkedHashSet<>(importingKeys);
        collisions.retainAll(importedKeys);
        if (!collisions.isEmpty()) {
            throw new JsonParseException("Wood ID collision of namespaces '" + importing + "' and '" + imported
                    + "': " + collisions + " - wire them collision-free first (explicit RerollID)");
        }
    }

    private Set<String> registeredIdKeys(String namespace) {
        final Set<String> keys = new LinkedHashSet<>();
        for (Entry entry : byPath.values()) {
            if (entry.resource != null && namespaceKey(entry.resource).equals(namespaceKey(namespace))) {
                keys.addAll(entry.ownIdKeys);
            }
        }
        return keys;
    }

    private void collectGraph(JsonResource resource, Set<String> synonyms,
            Map<String, WoodProvider> providers) {
        final String providerName = resource.getProviderName();
        if (providerName != null) {
            synonyms.add(providerName);
        }
        final WoodProviderBox box = resource.getExpectedBox();
        if (box != null) {
            for (WoodProvider provider : box.getProviders()) {
                synonyms.add(provider.getSynonym());
                providers.putIfAbsent(provider.getSynonym(), provider);
            }
        }
    }

    private JsonResource existingForSynonym(String namespaceKey, String synonym) {
        final String path = synonymsOf(namespaceKey).get(synonym);
        if (path == null) {
            return null;
        }
        final Entry entry = byPath.get(path);
        return entry != null ? entry.resource : null;
    }

    /**
     * The synonyms of one namespace (strategy B): every bundle label owns its own synonym world. The map
     * is created lazily; the registry as a whole is mutable during load only.
     */
    private Map<String, String> synonymsOf(String namespaceKey) {
        return synonymToPathByNamespace.computeIfAbsent(namespaceKey, k -> new LinkedHashMap<>());
    }

    /**
     * The internal namespace key of a resource: null means the session default namespace.
     */
    private static String namespaceKeyOf(JsonResource resource) {
        return resource != null && resource.getNamespace() != null ? resource.getNamespace() : DEFAULT_NAMESPACE;
    }

    private String namespaceKey(JsonResource resource) {
        return namespaceKeyOf(resource);
    }

    /**
     * The internal namespace key of a label: null means the session default namespace.
     */
    private static String namespaceKey(String namespace) {
        return namespace != null ? namespace : DEFAULT_NAMESPACE;
    }

    private String namespaceLabel(String namespaceKey) {
        return namespaceKey.isEmpty() ? "<default>" : namespaceKey;
    }

    private static String canonicalPathOf(JsonResource resource) {
        if (resource == null || !resource.hasResourceFile()) {
            return null;
        }
        return canonicalPathOf(new File(resource.getResourceFile()));
    }

    private static String canonicalPathOf(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            return file.getAbsolutePath();
        }
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("The bundle is closed");
        }
    }
}
