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
 * A long-lived project above {@link JsonResource} (concept doc/JSON_PROJECT.md): it guarantees {@code B == B'} -
 * every file of the project is parsed exactly once, held once and shared by all consuming resources, at parse
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
 * The project owns all resources for its lifetime (v1: no refcounting). It consumes the parser service for
 * loading but never the convert service - the io convert layer consumes the project (phase 2 of
 * doc/PLAN_JsonProject.md). Fingerprints are captured at load; the FRESH/STALE and REGISTERED/CONSUMED states
 * live on the {@link JsonResource} itself, the project only aggregates and coordinates.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class JsonProject {

    /**
     * One registry entry: the shared instance with its canonical path and its first (identity-winning) synonym.
     */
    private static final class Entry {

        private final String canonicalPath;
        private final String firstSynonym;
        private JsonResource resource;

        private Entry(String canonicalPath, String firstSynonym, JsonResource resource) {
            this.canonicalPath = canonicalPath;
            this.firstSynonym = firstSynonym;
            this.resource = resource;
        }
    }

    private final JsonDebugLevel debug;
    private final JsonResource mainResource;
    private final Map<String, Entry> byPath = new LinkedHashMap<>();
    private final Map<String, String> synonymToPath = new LinkedHashMap<>();
    private final List<String> warnings = new ArrayList<>();
    private boolean closed;

    /**
     * Opens the project of a main file: parses the main resource, walks its provider graph transitively, loads
     * and registers every referenced file exactly once, captures each fingerprint and merges the providers'
     * linking sets into the main resource's set (the shared instances keep their own clean per-resource sets -
     * concept hygiene rule).
     *
     * @param mainFile the main resource file, not null.
     * @param debug the debug level, not null.
     * @return the open project.
     * @throws IOException if a file cannot be read.
     * @throws JsonParseException if a file fails to parse, a provider synonym conflict or a dependency cycle
     * appears.
     */
    public static JsonProject open(File mainFile, JsonDebugLevel debug) throws IOException, JsonParseException {
        Objects.requireNonNull(mainFile, "mainFile must not be null");
        Objects.requireNonNull(debug, "debug must not be null");

        final JsonResource main = loadSelf(mainFile.getPath(), debug);
        final JsonProject project = new JsonProject(main, debug);
        project.register(main);
        project.loadProviderGraph(debug);
        return project;
    }

    /**
     * Loads one file as the given synonym - the single load rail of the project (same semantics the convert
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

    private JsonProject(JsonResource mainResource, JsonDebugLevel debug) {
        this.mainResource = mainResource;
        this.debug = debug;
    }

    /**
     * Returns the project's main resource.
     *
     * @return the main resource, not null while open.
     */
    public JsonResource getMainResource() {
        return mainResource;
    }

    /**
     * Closes the project: releases every held resource.
     */
    public void close() {
        byPath.clear();
        synonymToPath.clear();
        mainResource.setLinkingSet(null);
        closed = true;
    }

    /**
     * Returns the project's alias and reconciliation warnings (concept identity rules).
     *
     * @return unmodifiable warning lines, empty when none.
     */
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Registers a resource and returns the instance that owns its canonical path - the shared instance on a
     * path hit (the passed instance is discarded then), the passed instance itself on a miss. The identity
     * rules and provider-name reconciliation follow the concept matrix.
     *
     * @param resource the loaded resource, not null.
     * @return the registered instance for the resource's path.
     * @throws JsonParseException on a synonym conflict (another file owns the namespace) or on an alias for an
     * already consumed instance.
     */
    public JsonResource register(JsonResource resource) throws JsonParseException {
        Objects.requireNonNull(resource, "resource must not be null");
        requireOpen();

        final String path = canonicalPathOf(resource);
        final String synonym = resource.getProviderName();
        final Entry entry = path == null ? null : byPath.get(path);
        if (entry != null) {
            return aliasOrShare(entry, resource, synonym);
        }
        if (isSynonymTaken(synonym, null)) {
            final JsonResource owner = existingForSynonym(synonym);
            throw new JsonParseException("Synonym '" + synonym + "' is already taken by '"
                    + (owner != null ? owner.getResourceFile() : "<unknown>")
                    + "' - another file cannot own the ID namespace");
        }
        resource.captureFingerprint();
        final Entry fresh = new Entry(path, synonym, resource);
        if (path != null) {
            byPath.put(path, fresh);
        }
        synonymToPath.put(synonym, path);
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
        synonymToPath.put(requestedSynonym, entry.canonicalPath);
        warnings.add("Alias: '" + entry.canonicalPath + "' is registered as '" + entry.firstSynonym
                + "' and was also referenced as '" + requestedSynonym + "' - the linking set was rebuilt,"
                + " the first synonym wins");
        return shared;
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
    public JsonResource findBySynonym(String synonym) {
        requireOpen();
        final JsonResource resource = existingForSynonym(synonym);
        if (resource == null) {
            return null;
        }
        final String path = canonicalPathOf(resource);
        final Entry entry = path != null ? byPath.get(path) : null;
        return entry != null ? entry.resource : resource;
    }

    /**
     * Tells whether the synonym is already an owned ID namespace.
     *
     * @param synonym the synonym to check, not null.
     * @param exceptPath a canonical path that does not count as taken (the same file re-referencing itself),
     * may be null.
     * @return true when another file owns the synonym.
     */
    public boolean isSynonymTaken(String synonym, String exceptPath) {
        requireOpen();
        final String ownerPath = synonymToPath.get(synonym);
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
     * Bridges the project into the existing resolution: a {@link JsonSystem} for one resource with its
     * transitive provider closure (the resource itself plus every provider reachable through the boxes) -
     * the same set a per-resource parse session would hold, but built from the SHARED instances. The sorted
     * synonyms of the whole project graph serve as the order; synonyms outside the closure simply find no
     * resource in the system.
     *
     * @param main the resource to convert - the project's main normally, any sub resource in the editor.
     * @return the system, not null.
     * @throws JsonParseException when the provider graph contains a cycle.
     */
    public JsonSystem toSystem(JsonResource main) throws JsonParseException {
        Objects.requireNonNull(main, "main must not be null");
        requireOpen();
        final JsonSystem sys = JsonSystem.of(main.getExpectedBox(), main);
        final Set<String> queued = new LinkedHashSet<>();
        final Deque<String> queue = new ArrayDeque<>();
        enqueueSynonyms(main, queue, queued);
        while (!queue.isEmpty()) {
            final String synonym = queue.poll();
            final JsonResource resource = existingForSynonym(synonym);
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
     * The registry-first load lane for the convert layer (doc/JSON_PROJECT.md, integration step 2): a synonym
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
        Objects.requireNonNull(synonym, "synonym must not be null");
        Objects.requireNonNull(file, "file must not be null");
        requireOpen();

        JsonResource shared = findBySynonym(synonym);
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
        return register(load(file, synonym, debug));
    }

    /**
     * Every reuse verifies the fingerprint against the file (concept doc/JSON_PROJECT.md state model): the
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
        final Deque<WoodProvider> queue = new ArrayDeque<>();
        final Map<String, String> queuedFilenames = new LinkedHashMap<>();
        enqueueProviders(mainResource, queue, queuedFilenames);

        while (!queue.isEmpty()) {
            final WoodProvider provider = queue.poll();
            final String synonym = provider.getSynonym();

            JsonResource shared;
            try {
                shared = findBySynonym(synonym);
                if (shared == null) {
                    final String path = canonicalPathOf(new File(provider.getFilename()));
                    shared = path != null ? findByPath(path) : null;
                }
                if (shared == null) {
                    shared = register(load(new File(provider.getFilename()), synonym, debug));
                } else {
                    shared = register(forSynonym(shared, synonym));
                }
            } catch (FileNotFoundException ex) {
                // the same tolerance the convert layer lives by (WoodProxyResolver warns and continues): a
                // missing provider file is a warning, not a project failure - dangling references are a case
                // for the validator
                warnings.add("Provider file not found: " + provider.getFilename());
                continue;
            }
            mergeIntoMain(shared);
            enqueueProviders(shared, queue, queuedFilenames);
        }
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
     * DIFFERENT file is a conflict and throws.
     */
    private void enqueueProviders(JsonResource resource, Deque<WoodProvider> queue,
            Map<String, String> queuedFilenames) throws JsonParseException {
        final WoodProviderBox box = resource.getExpectedBox();
        if (box == null) {
            return;
        }
        for (WoodProvider provider : box.getProviders()) {
            final String filename = canonicalPathOf(new File(provider.getFilename()));
            final String known = queuedFilenames.putIfAbsent(provider.getSynonym(), filename);
            if (known == null) {
                queue.add(provider);
            } else if (!known.equals(filename)) {
                throw new JsonParseException("Synonym '" + provider.getSynonym() + "' is referenced with two "
                        + "files ('" + known + "' and '" + filename + "') - the ID namespace cannot be owned "
                        + "by two files");
            }
        }
    }

    /**
     * LinkingSet hygiene of the concept: foreign keys go into the main resource's set only - a shared instance
     * never accumulates another resource's keys.
     */
    private void mergeIntoMain(JsonResource shared) {
        final LinkingSet mainSet = mainResource.getLinkingSet();
        final LinkingSet sharedSet = shared.getLinkingSet();
        if (mainSet == null || sharedSet == null || shared == mainResource) {
            return;
        }
        mainSet.getObjectIdMap().putAll(sharedSet.getObjectIdMap());
        mainSet.getLinkMap().putAll(sharedSet.getLinkMap());
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

    private JsonResource existingForSynonym(String synonym) {
        final String path = synonymToPath.get(synonym);
        if (path == null) {
            return null;
        }
        final Entry entry = byPath.get(path);
        return entry != null ? entry.resource : null;
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
            throw new IllegalStateException("The project is closed");
        }
    }
}
