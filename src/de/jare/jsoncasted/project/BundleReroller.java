/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.COLONCOLON;
import static de.jare.jsoncasted.lang.JsonTerms.PREFIX_SELF;
import static de.jare.jsoncasted.lang.JsonTerms.PREFIX_THIS;
import static de.jare.jsoncasted.lang.JsonTerms.SELF_SYNONYM;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_OBJECT_ID;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_PROVIDERS;
import static de.jare.jsoncasted.lang.JsonTerms.THIS_SYNONYM;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * The identity refresh tool RerollID (concept WoodJsonJack CONCEPT_ProjectView.md, section 7.1 -
 * implementation plan phase 8): the machinery of the rejected Strategy A survives as an explicit,
 * on-demand action for bundles that are about to enter one shared resolution scope. The provider
 * synonyms of the bundle are replaced by freshly generated ones (the default - the wiring names
 * must be fresh per tree); the wood object ids reroll only on explicit request (the exception -
 * same ids across bundle copies are a strong hint for the compare). Every internal reference is
 * rewritten consistently. The rewrite scope is the bundle itself -
 * well-defined precisely because ids are bundle-scoped (Strategy B).
 *
 * <p>
 * This is the engine half: pure data, no editor, no io. It scans parsed trees into identity
 * inventories and computes the reroll plan - the new values are disjoint from the whole project
 * ({@code disjunct_to_bundles}), dangling internal references refuse the plan with a clear
 * message instead of producing half-rewired files. The execution lanes live above: the editor
 * applies the plan as one composite command (one undo step), other callers work on parsed trees.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class BundleReroller {

    /**
     * The prefix of freshly generated identities: {@code r1}, {@code r2}, ... - short, readable and
     * recognizable as rerolled.
     */
    public static final String NEW_ID_PREFIX = "r";

    private BundleReroller() {
        // static tool - no instances
    }

    /**
     * The identity inventory of one tree (a json file): the raw wood ids it defines, the provider
     * synonyms it declares and the qualified link targets it references.
     */
    public static final class TreeIds {

        private final Set<String> rawIds;
        private final Set<String> synonyms;
        private final Set<String> links;

        private TreeIds(Set<String> rawIds, Set<String> synonyms, Set<String> links) {
            this.rawIds = new LinkedHashSet<>(rawIds);
            this.synonyms = new LinkedHashSet<>(synonyms);
            this.links = new LinkedHashSet<>(links);
        }

        /**
         * A tree inventory out of ready sets (tests and callers that already collected their tree's
         * identities by other means).
         *
         * @param rawIds the raw ids the tree defines
         * @param synonyms the provider synonyms it declares
         * @param links the qualified link targets it references
         * @return the inventory, never null
         */
        public static TreeIds of(Set<String> rawIds, Set<String> synonyms, Set<String> links) {
            return new TreeIds(rawIds, synonyms, links);
        }

        public Set<String> getRawIds() {
            return rawIds;
        }

        public Set<String> getSynonyms() {
            return synonyms;
        }

        public Set<String> getLinks() {
            return links;
        }
    }

    /**
     * A reroll plan: the old-to-new maps every lane applies, plus the problems that refuse it. An
     * empty plan (no ids, no synonyms, no links) is valid - "nothing to reroll" is a state, not an
     * error.
     */
    public static final class RerollPlan {

        private final Map<String, String> idMap;
        private final Map<String, String> synonymMap;
        private final Map<String, String> linkMap;
        private final List<String> problems;

        private RerollPlan(Map<String, String> idMap, Map<String, String> synonymMap,
                Map<String, String> linkMap, List<String> problems) {
            this.idMap = idMap;
            this.synonymMap = synonymMap;
            this.linkMap = linkMap;
            this.problems = problems;
        }

        public Map<String, String> getIdMap() {
            return idMap;
        }

        public Map<String, String> getSynonymMap() {
            return synonymMap;
        }

        public Map<String, String> getLinkMap() {
            return linkMap;
        }

        public List<String> getProblems() {
            return problems;
        }

        /**
         * @return true when the plan must not execute - dangling references refuse it instead of
         * producing half-rewired files
         */
        public boolean isRefused() {
            return !problems.isEmpty();
        }

        public int getIdCount() {
            return idMap.size();
        }

        public int getSynonymCount() {
            return synonymMap.size();
        }

        public int getLinkCount() {
            return linkMap.size();
        }
    }

    /**
     * Scans a parsed tree into its identity inventory. The link targets are qualified like the
     * editor normalizes them ({@code this::}/{@code self::} prefixes resolve against the given
     * provider name) so the plan sees exactly the strings the execution lanes rewrite - links
     * pointing outside the bundle keep their external prefix.
     *
     * @param root the parsed root node, not null
     * @param providerName the provider name of the tree (its own synonym), or null to keep
     * this/self prefixes as written
     * @return the tree's identity inventory, never null
     */
    public static TreeIds scan(JsonNode root, String providerName) {
        Objects.requireNonNull(root, "root must not be null");
        final Set<String> rawIds = new LinkedHashSet<>();
        final Set<String> synonyms = new LinkedHashSet<>();
        final Set<String> links = new LinkedHashSet<>();
        final Deque<JsonNode> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            final JsonNode node = queue.poll();
            final Map<String, JsonNode> members = node.asObjectValues();
            if (members != null) {
                final JsonNode id = members.get(TERM_WOOD_OBJECT_ID);
                if (id != null && id.asText() != null && !id.asText().isEmpty()) {
                    rawIds.add(id.asText());
                }
                final JsonNode link = members.get(TERM_WOOD_LINK);
                if (link != null && link.asText() != null && !link.asText().isEmpty()) {
                    links.add(qualified(link.asText(), providerName));
                }
                final JsonNode providers = members.get(TERM_WOOD_PROVIDERS);
                if (providers != null && providers.asArray() != null) {
                    for (JsonNode entry : providers.asArray()) {
                        final Map<String, JsonNode> entryMembers = entry != null
                                ? entry.asObjectValues() : null;
                        final JsonNode synonym = entryMembers != null
                                ? entryMembers.get("synonym") : null;
                        if (synonym != null && synonym.asText() != null
                                && !synonym.asText().isEmpty()) {
                            synonyms.add(synonym.asText());
                        }
                    }
                }
                queue.addAll(members.values());
                continue;
            }
            final String arrayId = node.getWoodObjectId();
            if (arrayId != null && !arrayId.isEmpty()) {
                rawIds.add(arrayId);
            }
            final List<JsonNode> array = node.asArray();
            if (array != null) {
                queue.addAll(array);
            }
        }
        return new TreeIds(rawIds, synonyms, links);
    }

    /**
     * Computes the reroll plan (concept section 7.1, binding default): the provider synonyms
     * reroll, the wood object ids stay - see
     * {@link #plan(Set, Set, Set, Set, List, boolean, boolean)}. Same ids in a copied bundle are a
     * strong hint for the compare, the synonyms are the wiring names that must be fresh per tree.
     *
     * @param bundleRawIds every raw id defined anywhere in the bundle, not null
     * @param bundleSynonyms synonyms whose referenced file lies inside the bundle (rerolled), not null
     * @param externalSynonyms synonyms whose referenced file lies outside the bundle (kept), not null
     * @param bundleLinks every qualified link target referenced inside the bundle, not null
     * @param disjunctToBundles the trees to stay disjoint from, not null - the UI passes every
     * tree of the project (binding)
     * @return the plan - check {@link RerollPlan#isRefused()} before applying
     */
    public static RerollPlan plan(Set<String> bundleRawIds, Set<String> bundleSynonyms,
            Set<String> externalSynonyms, Set<String> bundleLinks, List<TreeIds> disjunctToBundles) {
        return plan(bundleRawIds, bundleSynonyms, externalSynonyms, bundleLinks, disjunctToBundles, false, true);
    }

    /**
     * Computes the reroll plan with both switches (concept section 7.1, amended: provider
     * synonyms reroll by default - the wiring names must be fresh per tree; the wood object ids
     * reroll only as the exception, because same ids in a copied bundle are a strong hint for the
     * compare. The internal references always rewrite consistently - they carry the new synonym
     * and/or the new id, whichever the switches produced). Input: the bundle's own identity
     * inventory (its raw ids, the synonyms that reference files INSIDE the bundle, the synonyms
     * that reference files outside it and its qualified links) plus the disjoint trees - the new
     * values collide with none of their id sets or synonyms. External references stay untouched,
     * dangling internal references refuse the plan.
     *
     * @param bundleRawIds every raw id defined anywhere in the bundle, not null
     * @param bundleSynonyms synonyms whose referenced file lies inside the bundle, not null
     * @param externalSynonyms synonyms whose referenced file lies outside the bundle (kept), not null
     * @param bundleLinks every qualified link target referenced inside the bundle, not null
     * @param disjunctToBundles the trees to stay disjoint from, not null
     * @param rerollIds true rerolls the wood object ids too (the exception, default off); false
     * keeps the ids - they stay a compare hint across bundle copies
     * @param rerollSynonyms true rerolls the provider synonyms (the binding default); false keeps
     * the wiring names
     * @return the plan - check {@link RerollPlan#isRefused()} before applying
     */
    public static RerollPlan plan(Set<String> bundleRawIds, Set<String> bundleSynonyms,
            Set<String> externalSynonyms, Set<String> bundleLinks, List<TreeIds> disjunctToBundles,
            boolean rerollIds, boolean rerollSynonyms) {
        Objects.requireNonNull(bundleRawIds, "bundleRawIds must not be null");
        Objects.requireNonNull(bundleSynonyms, "bundleSynonyms must not be null");
        Objects.requireNonNull(externalSynonyms, "externalSynonyms must not be null");
        Objects.requireNonNull(bundleLinks, "bundleLinks must not be null");
        Objects.requireNonNull(disjunctToBundles, "disjunctToBundles must not be null");

        final Set<String> takenIds = new TreeSet<>(bundleRawIds);
        final Set<String> freshIds = new TreeSet<>();
        for (TreeIds disjoint : disjunctToBundles) {
            takenIds.addAll(disjoint.getRawIds());
        }
        final Set<String> takenSynonyms = new TreeSet<>(bundleSynonyms);
        takenSynonyms.addAll(externalSynonyms);
        for (TreeIds disjoint : disjunctToBundles) {
            takenSynonyms.addAll(disjoint.getSynonyms());
        }

        // the new identities: r1, r2, ... - the first value free of every id set wins. Only with
        // the id switch on (the exception); by default the ids stay - same numbers across bundle
        // copies are a strong hint for the compare.
        final Map<String, String> idMap = new LinkedHashMap<>();
        if (rerollIds) {
            for (String oldId : new TreeSet<>(bundleRawIds)) {
                idMap.put(oldId, firstFreeId(takenIds, freshIds));
            }
        }
        // the new synonyms: old_1, old_2, ... - the first value free of every synonym set wins.
        // With the switch off the bundle keeps its wiring names.
        final Map<String, String> synonymMap = new LinkedHashMap<>();
        if (rerollSynonyms) {
            for (String oldSynonym : new TreeSet<>(bundleSynonyms)) {
                String candidate = null;
                int count = 1;
                while (candidate == null || takenSynonyms.contains(candidate)) {
                    candidate = oldSynonym + "_" + count;
                    count++;
                }
                synonymMap.put(oldSynonym, candidate);
                takenSynonyms.add(candidate);
            }
        }

        // the link rewrites: internal references carry the new synonym and/or the new id,
        // whichever the switches produced; a link that would not change is not a rewrite and stays
        // out of the map. External references stay as they are; dangling ones refuse the plan.
        final Map<String, String> linkMap = new LinkedHashMap<>();
        final List<String> problems = new ArrayList<>();
        for (String link : new TreeSet<>(bundleLinks)) {
            final int split = link.indexOf(COLONCOLON);
            if (split < 0) {
                problems.add("Malformed link '" + link + "' - a qualified syn::id reference is expected.");
                continue;
            }
            final String synonym = link.substring(0, split);
            final String id = link.substring(split + COLONCOLON.length());
            if (externalSynonyms.contains(synonym)) {
                continue; // external reference - not the reroll's business
            }
            // a self reference keeps its prefix (this::/self:: stay the tree's own point of view);
            // only the id part can reroll
            if (THIS_SYNONYM.equals(synonym) || SELF_SYNONYM.equals(synonym)) {
                if (!bundleRawIds.contains(id)) {
                    problems.add("Dangling reference: the link '" + link
                            + "' points at an id the bundle does not define.");
                    continue;
                }
                putIfChanged(linkMap, link, synonym + COLONCOLON + rerolledId(idMap, id));
                continue;
            }
            if (!bundleSynonyms.contains(synonym)) {
                problems.add("Dangling reference: the link '" + link
                        + "' names a synonym that is neither a bundle provider nor an external one.");
                continue;
            }
            if (!bundleRawIds.contains(id)) {
                problems.add("Dangling reference: the link '" + link
                        + "' points at an id the bundle does not define.");
                continue;
            }
            putIfChanged(linkMap, link,
                    rerolledSynonym(synonymMap, synonym) + COLONCOLON + rerolledId(idMap, id));
        }
        return new RerollPlan(idMap, synonymMap, linkMap, problems);
    }

    /**
     * The id part of a rewritten internal link: the rerolled id when the plan has one (id switch
     * on), the id as it was otherwise (id switch off - the default).
     */
    private static String rerolledId(Map<String, String> idMap, String id) {
        final String fresh = idMap.get(id);
        return fresh != null ? fresh : id;
    }

    /**
     * Registers a link rewrite - unless the rewritten target equals the original, then the link
     * needs no rewrite at all (both switches off) and stays out of the map.
     */
    private static void putIfChanged(Map<String, String> linkMap, String link, String rewritten) {
        if (!rewritten.equals(link)) {
            linkMap.put(link, rewritten);
        }
    }

    /**
     * The synonym part of a rewritten internal link: the rerolled name when the plan has one
     * (switch on), the wiring name as it was otherwise (switch off).
     */
    private static String rerolledSynonym(Map<String, String> synonymMap, String synonym) {
        final String fresh = synonymMap.get(synonym);
        return fresh != null ? fresh : synonym;
    }

    /**
     * The next free rerolled id: the {@code r}-prefixed running number none of the trees took.
     */
    private static String firstFreeId(Set<String> takenIds, Set<String> freshIds) {
        int count = 1;
        String candidate = NEW_ID_PREFIX + count;
        while (takenIds.contains(candidate) || freshIds.contains(candidate)) {
            count++;
            candidate = NEW_ID_PREFIX + count;
        }
        freshIds.add(candidate);
        return candidate;
    }

    /**
     * The qualification of a link like the editor's converter does it: {@code this::} and
     * {@code self::} resolve against the tree's own provider name; every other prefix stays.
     */
    private static String qualified(String link, String providerName) {
        if (providerName != null && !providerName.isBlank()
                && !THIS_SYNONYM.equals(providerName) && !SELF_SYNONYM.equals(providerName)) {
            if (link.startsWith(PREFIX_THIS) || link.startsWith(PREFIX_SELF)) {
                final int splitAt = link.indexOf(COLONCOLON) + COLONCOLON.length();
                return providerName + COLONCOLON + link.substring(splitAt);
            }
        }
        return link;
    }

}
