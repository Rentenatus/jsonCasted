package de.jare.jsoncasted.diff;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.parserservice.JsonParserService;
import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_LINK;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_WOOD_OBJECT_ID;
import java.io.StringReader;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Test class for the layer-1 tree diff: the DifferableNode projection, the Merkle hash and the TreeDiffer engine
 * (see doc/TREE_DIFF.md). All diff runs are pure — neither side may be mutated.
 *
 * @author Janusch Rentenatus
 */
public class TreeDifferNGTest {

    private static JsonNode obj() {
        return JsonNode.objectNode();
    }

    private static JsonNode arr() {
        return JsonNode.arrayNode();
    }

    private static JsonNode idNode(String id) {
        return JsonNode.stringNode(id);
    }

    private static JsonNode identified(String id) {
        return obj().put(TERM_WOOD_OBJECT_ID, idNode(id));
    }

    /**
     * Two independently built, equal trees yield an empty report (validated root short-circuit).
     */
    @Test
    public void testIdenticalTreesEmptyReport() {
        JsonNode base = obj().put("name", JsonNode.stringNode("demo")).put("size", JsonNode.longNode(3))
                .put("tags", arr().add(JsonNode.stringNode("a")).add(JsonNode.stringNode("b")));
        JsonNode theirs = obj().put("name", JsonNode.stringNode("demo")).put("size", JsonNode.longNode(3))
                .put("tags", arr().add(JsonNode.stringNode("a")).add(JsonNode.stringNode("b")));

        assertTrue(base.getMerkleHash() == theirs.getMerkleHash(), "equal trees must hash equally");
        DiffReport report = TreeDiffer.diff(base, theirs);
        assertTrue(report.isEmpty(), "no deltas expected: " + report);
    }

    /**
     * Merkle hashes are deterministic across instances: equal trees hash equally, any change changes the root hash.
     */
    @Test
    public void testMerkleHashCrossInstanceDeterminism() {
        JsonNode base = obj().put("a", JsonNode.longNode(1)).put("b", JsonNode.stringNode("x"));
        JsonNode theirs = obj().put("a", JsonNode.longNode(1)).put("b", JsonNode.stringNode("x"));
        JsonNode changed = obj().put("a", JsonNode.longNode(2)).put("b", JsonNode.stringNode("x"));

        assertEquals(base.getMerkleHash(), theirs.getMerkleHash());
        assertTrue(base.getMerkleHash() != changed.getMerkleHash());
        // member order is part of the parent hash (REORDER must change it)
        JsonNode reordered = obj().put("b", JsonNode.stringNode("x")).put("a", JsonNode.longNode(1));
        assertTrue(base.getMerkleHash() != reordered.getMerkleHash());
    }

    /**
     * A scalar value change is one MODIFY(VALUE) with the tagged value texts.
     */
    @Test
    public void testScalarValueChange() {
        JsonNode base = obj().put("size", JsonNode.longNode(3));
        JsonNode theirs = obj().put("size", JsonNode.longNode(4));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getType(), DeltaType.MODIFY);
        assertEquals(op.getProperty(), DeltaProperty.VALUE);
        assertEquals(op.getKey(), "$.size");
        assertEquals(op.getOldValue(), "long:3");
        assertEquals(op.getNewValue(), "long:4");
    }

    /**
     * A string "5" and a long 5 are different values (type-tagged comparison).
     */
    @Test
    public void testScalarTypeChangeIsAValueChange() {
        JsonNode base = obj().put("v", JsonNode.stringNode("5"));
        JsonNode theirs = obj().put("v", JsonNode.longNode(5));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1);
        assertEquals(report.getOps().get(0).getProperty(), DeltaProperty.VALUE);
    }

    /**
     * A member added on theirs side is an ADD with parent path and member key; a removed member is a REMOVE.
     */
    @Test
    public void testMemberAddedAndRemoved() {
        JsonNode base = obj().put("keep", JsonNode.longNode(1)).put("old", JsonNode.longNode(2));
        JsonNode theirs = obj().put("keep", JsonNode.longNode(1)).put("new", JsonNode.longNode(3));

        DiffReport report = TreeDiffer.diff(base, theirs);
        List<DeltaOp> adds = report.getOps(DeltaType.ADD);
        List<DeltaOp> removes = report.getOps(DeltaType.REMOVE);
        assertEquals(adds.size(), 1);
        assertEquals(removes.size(), 1);
        assertEquals(adds.get(0).getParentKey(), "$");
        assertEquals(adds.get(0).getFieldOrIndex(), "new");
        assertEquals(removes.get(0).getKey(), "$.old");
    }

    /**
     * The concept's orders-to-purchases case: same subtree content under a different member key is one MOVE, not
     * REMOVE + ADD.
     */
    @Test
    public void testMemberRenameBecomesMove() {
        JsonNode base = obj().put("orders", obj().put("count", JsonNode.longNode(7)));
        JsonNode theirs = obj().put("purchases", obj().put("count", JsonNode.longNode(7)));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1, "expected one MOVE: " + report);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getType(), DeltaType.MOVE);
        assertEquals(op.getKey(), "$.orders");
        assertEquals(op.getNewParentKey(), "$");
        assertEquals(op.getFieldOrIndex(), "purchases");
    }

    /**
     * Duplicated subtrees are copies, not moves: a REMOVE+ADD pair whose hash occurs twice stays REMOVE+ADD.
     */
    @Test
    public void testDuplicatedSubtreeStaysRemoveAndAdd() {
        JsonNode base = obj().put("a", obj().put("count", JsonNode.longNode(7)))
                .put("b", obj().put("count", JsonNode.longNode(7)));
        JsonNode theirs = obj().put("c", obj().put("count", JsonNode.longNode(7)))
                .put("d", obj().put("count", JsonNode.longNode(7)));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps(DeltaType.MOVE).size(), 0, "copies must not become moves: " + report);
        assertEquals(report.getOps(DeltaType.REMOVE).size(), 2);
        assertEquals(report.getOps(DeltaType.ADD).size(), 2);
    }

    /**
     * Pairwise equal array children in a different order collapse into one REORDER op, not N moves.
     */
    @Test
    public void testArrayReorderIsOneOp() {
        JsonNode base = arr().add(JsonNode.stringNode("a")).add(JsonNode.stringNode("b"))
                .add(JsonNode.stringNode("c"));
        JsonNode theirs = arr().add(JsonNode.stringNode("c")).add(JsonNode.stringNode("b"))
                .add(JsonNode.stringNode("a"));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1, "expected one REORDER: " + report);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getType(), DeltaType.REORDER);
        assertEquals(op.getKey(), "$");
        // labels mirror the tree renderer: primitive rows carry their value
        assertEquals(op.getNewOrder(), List.of("string:c", "string:b", "string:a"));
    }

    /**
     * An appended array element is an ADD addressed by its index.
     */
    @Test
    public void testArrayElementAdded() {
        JsonNode base = arr().add(JsonNode.longNode(1)).add(JsonNode.longNode(2));
        JsonNode theirs = arr().add(JsonNode.longNode(1)).add(JsonNode.longNode(2)).add(JsonNode.longNode(3));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getType(), DeltaType.ADD);
        assertEquals(op.getKey(), "$[2]");
        assertEquals(op.getParentKey(), "$");
    }

    /**
     * A dropped array element is a REMOVE.
     */
    @Test
    public void testArrayElementRemoved() {
        JsonNode base = arr().add(JsonNode.longNode(1)).add(JsonNode.longNode(2));
        JsonNode theirs = arr().add(JsonNode.longNode(1));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1);
        assertEquals(report.getOps().get(0).getType(), DeltaType.REMOVE);
        assertEquals(report.getOps().get(0).getKey(), "$[1]");
    }

    /**
     * The scalar remainder of an array pairs positionally: [1,2] against [1,3] is one MODIFY, not REMOVE+ADD.
     */
    @Test
    public void testArrayScalarRemainderIsPositional() {
        JsonNode base = arr().add(JsonNode.longNode(1)).add(JsonNode.longNode(2));
        JsonNode theirs = arr().add(JsonNode.longNode(1)).add(JsonNode.longNode(3));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1, "expected one positional MODIFY: " + report);
        assertEquals(report.getOps().get(0).getType(), DeltaType.MODIFY);
        assertEquals(report.getOps().get(0).getKey(), "$[1]");
    }

    /**
     * Wood-identified array elements pair by id: a changed element is MODIFY on its matched position, not
     * REMOVE+ADD.
     */
    @Test
    public void testArrayElementMatchedById() {
        JsonNode base = arr().add(identified("a").put("v", JsonNode.longNode(1)))
                .add(identified("b").put("v", JsonNode.longNode(2)));
        JsonNode theirs = arr().add(identified("a").put("v", JsonNode.longNode(9)))
                .add(identified("b").put("v", JsonNode.longNode(2)));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1, "expected one MODIFY: " + report);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getType(), DeltaType.MODIFY);
        assertEquals(op.getKey(), "$[0].v");
    }

    /**
     * A displaced wood-identified element is a MOVE; an order change with a content change is MOVE plus MODIFY —
     * never a silent loss.
     */
    @Test
    public void testArrayElementMoveAndChange() {
        JsonNode base = arr().add(identified("a").put("v", JsonNode.longNode(1)))
                .add(identified("b").put("v", JsonNode.longNode(2)));
        JsonNode theirs = arr().add(identified("b").put("v", JsonNode.longNode(2)))
                .add(identified("a").put("v", JsonNode.longNode(5)));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps(DeltaType.MOVE).size(), 2, "both elements changed position: " + report);
        assertEquals(report.getOps(DeltaType.MODIFY).size(), 1);
        assertEquals(report.getOps(DeltaType.MODIFY).get(0).getKey(), "$[0].v");
    }

    /**
     * An identity change is never a MODIFY: it is REMOVE (old id) + ADD (new id), both radiating.
     */
    @Test
    public void testWoodIdChangeIsRemoveAndAdd() {
        JsonNode base = obj().put("entry", identified("a").put("v", JsonNode.longNode(1)));
        JsonNode theirs = obj().put("entry", identified("b").put("v", JsonNode.longNode(1)));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 2, "expected REMOVE+ADD: " + report);
        assertEquals(report.getOps(DeltaType.REMOVE).get(0).getKey(), "$.entry");
        assertTrue(report.getOps(DeltaType.REMOVE).get(0).isRadiating());
        assertTrue(report.getOps(DeltaType.ADD).get(0).getKey().endsWith(".entry"));
        assertEquals(report.getRemovedObjektIds(), List.of("a"));
    }

    /**
     * A link target change is one radiating MODIFY(LINK_TARGET) — dependents are affected.
     */
    @Test
    public void testLinkTargetChangeIsRadiating() {
        JsonNode base = obj().put("ref", obj().put(TERM_WOOD_LINK, idNode("this::5")));
        JsonNode theirs = obj().put("ref", obj().put(TERM_WOOD_LINK, idNode("this::9")));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1, "expected one MODIFY: " + report);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getProperty(), DeltaProperty.LINK_TARGET);
        assertTrue(op.isRadiating());
        assertEquals(report.getRadiatingOps().size(), 1);
    }

    /**
     * A cast change is one MODIFY(CLASS), not radiating.
     */
    @Test
    public void testCastChange() {
        JsonNode base = obj().put("v", obj().put(TERM_CLASS, idNode("hallucinated.test.clazz")));
        JsonNode theirs = obj().put("v", obj().put(TERM_CLASS, idNode("other.test.Clazz")));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1, "expected one MODIFY: " + report);
        DeltaOp op = report.getOps().get(0);
        assertEquals(op.getProperty(), DeltaProperty.CLASS);
        assertTrue(!op.isRadiating());
    }

    /**
     * A kind change (string becomes object) replaces the node species: REMOVE + ADD.
     */
    @Test
    public void testKindChangeIsRemoveAndAdd() {
        JsonNode base = obj().put("v", JsonNode.stringNode("text"));
        JsonNode theirs = obj().put("v", obj().put("inner", JsonNode.longNode(1)));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 2);
        assertEquals(report.getOps(DeltaType.REMOVE).get(0).getKey(), "$.v");
        assertEquals(report.getOps(DeltaType.ADD).get(0).getFieldOrIndex(), "v");
    }

    /**
     * Deep change inside an array member: the match key addresses the exact position.
     */
    @Test
    public void testDeepPath() {
        JsonNode base = obj().put("content", arr().add(obj().put("ref", JsonNode.stringNode("x"))));
        JsonNode theirs = obj().put("content", arr().add(obj().put("ref", JsonNode.stringNode("y"))));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getOps().size(), 1);
        assertEquals(report.getOps().get(0).getKey(), "$.content[0].ref");
    }

    /**
     * The semantic no-op: two fresh parses of the same JSON text are identical, and a purely textual reformat of a
     * scalar member (here: long vs long spelling) yields no deltas. Also proves the parser fills the member keys
     * the diff projection relies on.
     */
    @Test
    public void testTwoParsesOfSameJsonAreSemanticNoOp() throws Exception {
        String json = "{\"name\":\"demo\",\"list\":[{\"_woodObjectId\":\"a\",\"v\":1},{\"_woodObjectId\":\"b\",\"v\":2}]}";
        JsonNode base = JsonParserService.parse(new StringReader(json), "no-op.json", JsonDebugLevel.INFO).getRoot();
        JsonNode theirs = JsonParserService.parse(new StringReader(json), "no-op.json", JsonDebugLevel.INFO).getRoot();

        assertTrue(base.getMerkleHash() == theirs.getMerkleHash(), "independent parses must hash equally");
        DiffReport report = TreeDiffer.diff(base, theirs);
        assertTrue(report.isEmpty(), "semantic no-op expected: " + report);

        String changedJson = "{\"name\":\"demo\",\"list\":[{\"_woodObjectId\":\"a\",\"v\":1},{\"_woodObjectId\":\"b\",\"v\":3}]}";
        JsonNode changed = JsonParserService.parse(new StringReader(changedJson), "no-op.json", JsonDebugLevel.INFO).getRoot();
        DiffReport changedReport = TreeDiffer.diff(base, changed);
        assertEquals(changedReport.getOps().size(), 1);
        assertEquals(changedReport.getOps().get(0).getKey(), "$.list[1].v");
    }

    /**
     * The U2 impact analysis query: removed wood ids of the report are the intersection input for the linking set.
     */
    @Test
    public void testRemovedObjektIdsForImpactAnalysis() {
        JsonNode base = obj().put("dep", identified("dep::5")).put("keep", identified("keep::1"));
        JsonNode theirs = obj().put("keep", identified("keep::1")).put("plain", JsonNode.longNode(1));

        DiffReport report = TreeDiffer.diff(base, theirs);
        assertEquals(report.getRemovedObjektIds(), List.of("dep::5"));
        assertEquals(report.getRadiatingOps().size(), 1);
    }
}
