/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_OBJECT_ID;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_PROVIDERS;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Phase 8 of the project view concept (CONCEPT_ProjectView.md, section 7.1): the identity refresh
 * tool RerollID - the scan of a tree into its identity inventory, the plan with freshly generated
 * ids and synonyms disjoint from {@code disjunct_to_bundles}, the consistent rewrite of internal
 * references and the refusal of dangling ones.
 *
 * @author Janusch Rentenatus
 */
public class BundleRerollerNGTest {

    /**
     * The scan collects the tree's identity inventory: object ids, array identities, provider
     * synonyms and the link targets - self references qualify against the tree's own provider
     * name, other prefixes stay as written.
     */
    @Test
    public void testScanCollectsIdsSynonymsAndLinks() {
        final JsonNode provider = JsonNode.objectNode()
                .put("synonym", JsonNode.stringNode("shared"))
                .put("filename", JsonNode.stringNode("sub/shared.json"));
        final JsonNode providersArray = JsonNode.arrayNode(Arrays.asList(provider));
        final JsonNode nestedObject = JsonNode.objectNode()
                .put(TERM_WOOD_OBJECT_ID, JsonNode.stringNode("77"))
                .put(TERM_WOOD_LINK, JsonNode.stringNode("shared::77"));
        final JsonNode arrayElement = JsonNode.arrayNode(Arrays.asList(nestedObject));
        final JsonNode root = JsonNode.objectNode()
                .put(TERM_WOOD_OBJECT_ID, JsonNode.stringNode("5"))
                .put(TERM_WOOD_PROVIDERS, providersArray)
                .put(TERM_WOOD_LINK, JsonNode.stringNode("this::5"))
                .put("nested", arrayElement);
        final JsonNode identitiedArray = JsonNode.arrayNode();
        identitiedArray.setWoodObjectId("88");
        root.put("list", identitiedArray);

        final BundleReroller.TreeIds ids = BundleReroller.scan(root, "cfg");

        assertEquals(ids.getRawIds(), setOf("5", "77", "88"));
        assertEquals(ids.getSynonyms(), setOf("shared"));
        assertEquals(ids.getLinks(), setOf("cfg::5", "shared::77"));
    }

    /**
     * The plan generates fresh values disjoint from everything: the r-prefixed ids skip every id
     * and synonym the disjoint trees carry (a tree that already owns "r1" pushes the generator to
     * the next free number), the synonyms chain a suffix until they are free too.
     */
    @Test
    public void testFreshValuesAreDisjointFromTheWholeProject() {
        final BundleReroller.TreeIds disjoint = asTreeIds(setOf("r1", "9"), setOf("shared"), Collections.emptySet());

        final BundleReroller.RerollPlan plan = BundleReroller.plan(
                setOf("5", "9"), setOf("shared", "core"), setOf("ext"),
                setOf("shared::5", "core::9", "ext::9", "this::5"),
                Arrays.asList(disjoint));

        assertFalse(plan.isRefused(), "nothing dangles: " + plan.getProblems());
        assertEquals(plan.getIdCount(), 2, "both bundle ids reroll");
        assertEquals(plan.getIdMap().get("5"), "r2", "r1 is taken by the disjoint tree - the generator skips");
        assertEquals(plan.getIdMap().get("9"), "r3");
        assertEquals(plan.getSynonymMap().get("shared"), "shared_1");
        assertTrue(plan.getSynonymMap().get("core").startsWith("core_"),
                "every bundle synonym rerolls: " + plan.getSynonymMap());
        // nothing fresh collides with any tree of the project
        for (String fresh : plan.getIdMap().values()) {
            assertFalse(fresh.equals("r1") || fresh.equals("9"));
        }
        assertTrue(plan.getSynonymMap().get("shared").startsWith("shared_"));
        assertFalse("shared".equals(plan.getSynonymMap().get("shared")));
    }

    /**
     * The reference rewrite is consistent: internal links carry the new synonym and the new id,
     * self references keep their prefix with the new id, external references stay untouched, and
     * the rewritten link resolves under the new identities.
     */
    @Test
    public void testLinksRewriteConsistently() {
        final BundleReroller.RerollPlan plan = BundleReroller.plan(
                setOf("5"), setOf("shared"), setOf("ext"),
                setOf("shared::5", "ext::5", "this::5"),
                Collections.emptyList());

        assertFalse(plan.isRefused(), "nothing dangles: " + plan.getProblems());
        final Map<String, String> links = plan.getLinkMap();
        final String newShared = plan.getSynonymMap().get("shared");
        final String newId = plan.getIdMap().get("5");
        assertEquals(links.get("shared::5"), newShared + "::" + newId);
        assertEquals(links.get("this::5"), "this::" + newId);
        assertFalse(links.containsKey("ext::5"), "external references stay untouched");
        assertEquals(plan.getLinkCount(), 2);
    }

    /**
     * Dangling references refuse the plan with a clear message instead of producing half-rewired
     * files: a link to an id the bundle does not define, and a link naming an unknown synonym.
     */
    @Test
    public void testDanglingReferencesRefuse() {
        final BundleReroller.RerollPlan missingId = BundleReroller.plan(
                setOf("5"), setOf("shared"), setOf("ext"),
                setOf("shared::42"), Collections.emptyList());
        assertTrue(missingId.isRefused());
        assertFalse(missingId.getProblems().isEmpty());
        assertTrue(missingId.getProblems().get(0).contains("shared::42"));

        final BundleReroller.RerollPlan unknownSynonym = BundleReroller.plan(
                setOf("5"), setOf("shared"), setOf("ext"),
                setOf("ghost::5"), Collections.emptyList());
        assertTrue(unknownSynonym.isRefused());
        assertTrue(unknownSynonym.getProblems().get(0).contains("ghost::5"));
    }

    /**
     * A bundle without identities gets a valid empty plan - "nothing to reroll" is a state, not an
     * error; the dialog reports it instead of executing nothing silently.
     */
    @Test
    public void testEmptyBundlePlansEmptyAndValid() {
        final BundleReroller.RerollPlan plan = BundleReroller.plan(
                setOf(), setOf(), setOf(), setOf(), Collections.emptyList());
        assertFalse(plan.isRefused());
        assertEquals(plan.getIdCount(), 0);
        assertEquals(plan.getSynonymCount(), 0);
        assertEquals(plan.getLinkCount(), 0);
    }

    //
    // helpers
    //

    private static BundleReroller.TreeIds asTreeIds(Set<String> rawIds, Set<String> synonyms, Set<String> links) {
        return BundleReroller.TreeIds.of(rawIds, synonyms, links);
    }

    private static Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
