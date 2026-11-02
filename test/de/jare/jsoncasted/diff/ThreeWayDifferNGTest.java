package de.jare.jsoncasted.diff;

import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests for the pure 3-way analysis (U1 of doc/TREE_DIFF.md): overlap detection on (matchKey, property) pairs over
 * the TreeDiffer reports — apply, no-op or conflict, never a mutation.
 *
 * @author Janusch Rentenatus
 */
public class ThreeWayDifferNGTest {

    private static JsonNode obj() {
        return JsonNode.objectNode();
    }

    private static JsonNode obj2(String k1, long v1, String k2, long v2) {
        return obj().put(k1, JsonNode.longNode(v1)).put(k2, JsonNode.longNode(v2));
    }

    @Test
    public void testNonOverlappingEditsApply() {
        JsonNode base = obj2("a", 1, "b", 2);
        JsonNode ours = obj2("a", 10, "b", 2);
        JsonNode theirs = obj2("a", 1, "b", 99);

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertTrue(!plan.hasConflicts(), plan.getConflicts().toString());
        assertEquals(plan.getApplyOps().size(), 1);
        assertEquals(plan.getApplyOps().get(0).getKey(), "$.b");
        assertEquals(plan.getApplyOps().get(0).getProperty(), DeltaProperty.VALUE);
        assertTrue(plan.getNoOps().isEmpty());
    }

    @Test
    public void testSameChangeBothSidesIsNoOp() {
        JsonNode base = obj().put("a", JsonNode.longNode(1));
        JsonNode ours = obj().put("a", JsonNode.longNode(10));
        JsonNode theirs = obj().put("a", JsonNode.longNode(10));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertTrue(!plan.hasConflicts());
        assertTrue(plan.getApplyOps().isEmpty(), "the same change must not be applied twice");
        assertEquals(plan.getNoOps().size(), 1);
        assertEquals(plan.getNoOps().get(0).getKey(), "$.a");
    }

    @Test
    public void testDivergentValueConflict() {
        JsonNode base = obj().put("a", JsonNode.longNode(1));
        JsonNode ours = obj().put("a", JsonNode.longNode(10));
        JsonNode theirs = obj().put("a", JsonNode.longNode(20));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertEquals(plan.getConflicts().size(), 1);
        ConflictRecord conflict = plan.getConflicts().get(0);
        assertEquals(conflict.getReason(), ConflictReason.VALUE_DIVERGENT);
        assertEquals(conflict.getMatchKey(), "$.a");
        assertEquals(conflict.getProperty(), DeltaProperty.VALUE);
        assertTrue(plan.getApplyOps().isEmpty());
    }

    @Test
    public void testOursRemovedTheirsModifiedInside() {
        JsonNode base = obj().put("b", obj().put("c", JsonNode.longNode(1)));
        JsonNode ours = obj(); // user removed the whole member b
        JsonNode theirs = obj().put("b", obj().put("c", JsonNode.longNode(2)));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertEquals(plan.getConflicts().size(), 1);
        assertEquals(plan.getConflicts().get(0).getReason(), ConflictReason.REMOVED_VS_MODIFIED);
        assertEquals(plan.getConflicts().get(0).getMatchKey(), "$.b.c");
        assertTrue(plan.getApplyOps().isEmpty());
    }

    @Test
    public void testTheirsRemovedOursModifiedInside() {
        JsonNode base = obj().put("b", obj().put("c", JsonNode.longNode(1)));
        JsonNode ours = obj().put("b", obj().put("c", JsonNode.longNode(2)));
        JsonNode theirs = obj(); // file removed the whole member b

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertEquals(plan.getConflicts().size(), 1);
        assertEquals(plan.getConflicts().get(0).getReason(), ConflictReason.REMOVED_VS_MODIFIED);
        assertTrue(plan.getApplyOps().isEmpty());
    }

    @Test
    public void testBothRemovedIsNoOp() {
        JsonNode base = obj2("a", 1, "b", 2);
        JsonNode ours = obj().put("a", JsonNode.longNode(1));
        JsonNode theirs = obj().put("a", JsonNode.longNode(1));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertTrue(!plan.hasConflicts());
        assertEquals(plan.getNoOps().size(), 1);
        assertEquals(plan.getNoOps().get(0).getType(), DeltaType.REMOVE);
    }

    @Test
    public void testBothAddedSameMemberDiverges() {
        JsonNode base = obj().put("a", JsonNode.longNode(1));
        JsonNode ours = obj().put("a", JsonNode.longNode(1)).put("x", JsonNode.longNode(7));
        JsonNode theirs = obj().put("a", JsonNode.longNode(1)).put("x", JsonNode.longNode(8));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertEquals(plan.getConflicts().size(), 1);
        assertEquals(plan.getConflicts().get(0).getReason(), ConflictReason.STRUCTURAL_COLLISION);
        assertEquals(plan.getConflicts().get(0).getMatchKey(), "$.x");
    }

    @Test
    public void testBothAddedSameMemberIdentical() {
        JsonNode base = obj().put("a", JsonNode.longNode(1));
        JsonNode ours = obj().put("a", JsonNode.longNode(1)).put("x", JsonNode.longNode(7));
        JsonNode theirs = obj().put("a", JsonNode.longNode(1)).put("x", JsonNode.longNode(7));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertTrue(!plan.hasConflicts(), plan.getConflicts().toString());
        assertEquals(plan.getNoOps().size(), 1);
        assertTrue(plan.getApplyOps().isEmpty());
    }

    @Test
    public void testTheirsAddedDifferentMemberApplies() {
        JsonNode base = obj().put("a", JsonNode.longNode(1));
        JsonNode ours = obj().put("a", JsonNode.longNode(1)).put("x", JsonNode.longNode(7));
        JsonNode theirs = obj().put("a", JsonNode.longNode(1)).put("y", JsonNode.longNode(9));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertTrue(!plan.hasConflicts());
        assertEquals(plan.getApplyOps().size(), 1);
        assertEquals(plan.getApplyOps().get(0).getType(), DeltaType.ADD);
        assertEquals(plan.getApplyOps().get(0).getFieldOrIndex(), "y");
    }

    @Test
    public void testTheirsReorderNeedsDecision() {
        JsonNode base = JsonNode.arrayNode().add(JsonNode.stringNode("a")).add(JsonNode.stringNode("b"))
                .add(JsonNode.stringNode("c"));
        JsonNode ours = JsonNode.arrayNode().add(JsonNode.stringNode("a")).add(JsonNode.stringNode("b"))
                .add(JsonNode.stringNode("c"));
        JsonNode theirs = JsonNode.arrayNode().add(JsonNode.stringNode("c")).add(JsonNode.stringNode("b"))
                .add(JsonNode.stringNode("a"));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertEquals(plan.getConflicts().size(), 1);
        assertEquals(plan.getConflicts().get(0).getReason(), ConflictReason.MOVE_OR_REORDER);
        assertTrue(plan.getApplyOps().isEmpty());
    }

    @Test
    public void testDivergentLinkTargetConflictIsRadiating() {
        JsonNode base = obj().put("ref", obj().put(TERM_WOOD_LINK, JsonNode.stringNode("this::7")));
        JsonNode ours = obj().put("ref", obj().put(TERM_WOOD_LINK, JsonNode.stringNode("this::10")));
        JsonNode theirs = obj().put("ref", obj().put(TERM_WOOD_LINK, JsonNode.stringNode("this::11")));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertEquals(plan.getConflicts().size(), 1);
        ConflictRecord conflict = plan.getConflicts().get(0);
        assertEquals(conflict.getProperty(), DeltaProperty.LINK_TARGET);
        assertTrue(conflict.isRadiating(), "link target conflicts must show dependents");
    }

    @Test
    public void testMixedChangesSplitCleanly() {
        // ours touches a, theirs touches b and adds c: one apply, no conflicts, ours edit untouched
        JsonNode base = obj2("a", 1, "b", 2);
        JsonNode ours = obj2("a", 10, "b", 2);
        JsonNode theirs = obj2("a", 1, "b", 5).put("c", JsonNode.longNode(3));

        MergePlan plan = ThreeWayDiffer.analyze(base, ours, theirs);
        assertTrue(!plan.hasConflicts());
        List<DeltaOp> apply = plan.getApplyOps();
        assertEquals(apply.size(), 2);
        assertEquals(apply.get(0).getType(), DeltaType.MODIFY);
        assertEquals(apply.get(0).getKey(), "$.b");
        assertEquals(apply.get(1).getType(), DeltaType.ADD);
        assertEquals(apply.get(1).getFieldOrIndex(), "c");
    }
}
