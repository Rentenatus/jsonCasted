/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The tree diff engine, written once against {@link DifferableNode} (see {@code doc/TREE_DIFF.md}). One invocation
 * compares one representation only; it is a pure function — neither side is ever mutated, the result is a
 * {@link DiffReport}.
 *
 * <p>
 * Matching pipeline: Merkle hashes prune (validated — "hash filters, equals decides"), wood ids anchor changed
 * nodes, unique hash pairs become MOVEs, positional pairing catches the scalar remainder. Matching never uses
 * internal node identity or per-parse ids. REORDER stays sibling-level: pairwise equal children in different order
 * are one op, not N moves.
 * </p>
 *
 * <p>
 * v1 simplifications, consciously: similarity matching (GumTree-style) and local TED are deferred — similar but
 * unequal subtrees are reported as REMOVE + ADD (the concept's conservative fallback); a hash collision registers
 * itself by failing validation in every pairing attempt, an extended-hash registry is not yet needed for that.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class TreeDiffer {

    private final DiffReport report;

    private TreeDiffer(DiffReport report) {
        this.report = report;
    }

    /**
     * Diffs two trees and returns the pure delta report. Equal trees yield an empty report — this is also the
     * semantic no-op check: a purely cosmetic file change (reformat, whitespace) produces no deltas.
     *
     * @param base the base side (e.g. the cached resource), never {@code null}.
     * @param theirs the theirs side (e.g. a fresh parse), never {@code null}.
     * @return the delta report, never {@code null}.
     */
    public static DiffReport diff(DifferableNode base, DifferableNode theirs) {
        final DiffReport result = new DiffReport();
        if (base == theirs) {
            return result;
        }
        if (base.getMerkleHash() == theirs.getMerkleHash() && NodeValidator.isEqual(base, theirs)) {
            return result; // root short-circuit, validated — never on hash alone
        }
        final TreeDiffer differ = new TreeDiffer(result);
        differ.matchNode(base, theirs, "$", null, null);
        differ.pairMoves();
        return result;
    }

    private void matchNode(DifferableNode b, DifferableNode t, String path, String parentPath, String fieldOrIndex) {
        if (b.getKind() != t.getKind()) {
            // kind change = different node species; the old one is gone, a new one appeared
            report.add(DeltaOp.removeOp(path, b, hasObjektId(b)));
            if (parentPath != null) {
                report.add(DeltaOp.addOp(parentPath, fieldOrIndex, t));
            }
            return;
        }
        if (b.getMerkleHash() == t.getMerkleHash() && NodeValidator.isEqual(b, t)) {
            return; // identical subtree, validated — prune, no delta
        }
        if (!equalsNullable(b.getObjektId(), t.getObjektId())) {
            // identity change is never a MODIFY: it changes every qualified reference to the node
            report.add(DeltaOp.removeOp(path, b, true));
            if (parentPath != null) {
                report.add(DeltaOp.addOp(parentPath, fieldOrIndex, t));
            }
            return;
        }
        if (!equalsNullable(b.getLinkTarget(), t.getLinkTarget())) {
            report.add(DeltaOp.modifyOp(path, DeltaProperty.LINK_TARGET, b.getLinkTarget(), t.getLinkTarget()));
        }
        if (!equalsNullable(b.getName(), t.getName())) {
            report.add(DeltaOp.modifyOp(path, DeltaProperty.NAME, b.getName(), t.getName()));
        }
        if (!equalsNullable(b.getCastName(), t.getCastName())) {
            report.add(DeltaOp.modifyOp(path, DeltaProperty.CLASS, b.getCastName(), t.getCastName()));
        }
        switch (b.getKind()) {
            case OBJECT:
            case PROPERTY:
                matchKeyedChildren(b, t, path);
                break;
            case ARRAY:
                matchArrayChildren(b, t, path);
                break;
            default:
                if (!sameValue(b, t)) {
                    report.add(DeltaOp.modifyOp(path, DeltaProperty.VALUE, valueText(b), valueText(t)));
                }
                break;
        }
    }

    /**
     * Matches keyed members: equal member key means same member (recurse), base-only keys are REMOVE candidates,
     * theirs-only keys are ADD candidates. A key change with equal subtree content is turned into a MOVE by the
     * post-pass {@link #pairMoves()}.
     */
    private void matchKeyedChildren(DifferableNode b, DifferableNode t, String path) {
        final Map<String, DifferableNode> bMembers = keyMap(b);
        final Map<String, DifferableNode> tMembers = keyMap(t);
        for (Map.Entry<String, DifferableNode> entry : bMembers.entrySet()) {
            final String key = entry.getKey();
            final DifferableNode tChild = tMembers.get(key);
            if (tChild != null) {
                matchNode(entry.getValue(), tChild, DeltaOp.childPath(path, key), path, key);
            } else {
                report.add(DeltaOp.removeOp(DeltaOp.childPath(path, key), entry.getValue(),
                        hasObjektId(entry.getValue())));
            }
        }
        for (Map.Entry<String, DifferableNode> entry : tMembers.entrySet()) {
            if (!bMembers.containsKey(entry.getKey())) {
                report.add(DeltaOp.addOp(path, entry.getKey(), entry.getValue()));
            }
        }
    }

    /**
     * Matches array children: wood-id pairs first (identity anchor), then unique-hash pairs (MOVE candidates,
     * validated), then the scalar remainder positionally. Pairwise equal children in different order collapse into
     * one REORDER.
     */
    private void matchArrayChildren(DifferableNode b, DifferableNode t, String path) {
        final List<DifferableNode> bChildren = b.children();
        final List<DifferableNode> tChildren = t.children();
        final Integer[] pairOfBase = new Integer[bChildren.size()]; // base index -> theirs index
        final boolean[] theirsTaken = new boolean[tChildren.size()];

        // step 3: qualified wood-id matches
        final Map<String, Integer> theirsById = new HashMap<>();
        for (int j = 0; j < tChildren.size(); j++) {
            final String id = tChildren.get(j).getObjektId();
            if (id != null) {
                theirsById.putIfAbsent(id, j);
            }
        }
        for (int i = 0; i < bChildren.size(); i++) {
            final String id = bChildren.get(i).getObjektId();
            if (id != null && theirsById.containsKey(id)) {
                final int j = theirsById.get(id);
                pairOfBase[i] = j;
                theirsTaken[j] = true;
            }
        }

        // step 5 preparation: unique-hash matches over the remainder (duplicates are copies, not moves)
        final List<Integer> bRest = restIndexes(pairOfBase);
        final List<Integer> tRest = restIndexes(theirsTaken);
        final Map<Long, List<Integer>> tRestByHash = groupByHash(tChildren, tRest);
        for (int i : bRest) {
            final List<Integer> candidates = tRestByHash.get(bChildren.get(i).getMerkleHash());
            if (candidates != null && candidates.size() == 1) {
                final int j = candidates.get(0);
                if (countByHash(tRestByHash, bChildren.get(i).getMerkleHash()) == 1
                        && NodeValidator.isEqual(bChildren.get(i), tChildren.get(j))) {
                    pairOfBase[i] = j;
                    theirsTaken[j] = true;
                }
            }
        }

        // step 4/6 residue: positional pairing of the remainder — after id and hash pairing consumed the
        // identifiable children, the leftover pairs in order (same kind: scalars and containers alike, so deep
        // paths like $.content[2].ref survive instead of degrading to whole-subtree REMOVE + ADD)
        final List<Integer> bRest2 = restIndexes(pairOfBase);
        final List<Integer> tRest2 = restIndexes(theirsTaken);
        for (int k = 0; k < Math.min(bRest2.size(), tRest2.size()); k++) {
            final int i = bRest2.get(k);
            final int j = tRest2.get(k);
            if (bChildren.get(i).getKind() == tChildren.get(j).getKind()) {
                pairOfBase[i] = j;
                theirsTaken[j] = true;
            }
        }

        // unpaired remainder -> REMOVE / ADD
        for (int i : restIndexes(pairOfBase)) {
            final DifferableNode child = bChildren.get(i);
            report.add(DeltaOp.removeOp(DeltaOp.childPath(path, String.valueOf(i)), child, hasObjektId(child)));
        }
        for (int j : restIndexes(theirsTaken)) {
            report.add(DeltaOp.addOp(path, String.valueOf(j), tChildren.get(j)));
        }

        // paired children: content first, then position
        final List<DeltaOp> pendingMoves = new ArrayList<>();
        boolean identicalPairs = true;
        int lastT = -1;
        boolean monotonic = true;
        for (int i = 0; i < pairOfBase.length; i++) {
            final Integer j = pairOfBase[i];
            if (j == null) {
                identicalPairs = false;
                continue;
            }
            if (j < lastT) {
                monotonic = false;
            }
            lastT = j;
            final DifferableNode bChild = bChildren.get(i);
            final DifferableNode tChild = tChildren.get(j);
            final boolean identical = bChild.getMerkleHash() == tChild.getMerkleHash()
                    && NodeValidator.isEqual(bChild, tChild);
            if (!identical) {
                identicalPairs = false;
            }
            if (i != j && (bChild.getObjektId() != null || identical)) {
                // same qualified id / same content, other position
                pendingMoves.add(DeltaOp.moveOp(DeltaOp.childPath(path, String.valueOf(i)), bChild,
                        path, j, null, hasObjektId(bChild)));
            }
            matchNode(bChild, tChild, DeltaOp.childPath(path, String.valueOf(i)), path, String.valueOf(i));
        }

        if (identicalPairs && pairOfBase.length == tChildren.size() && !monotonic && pairOfBase.length > 1) {
            // REORDER stays sibling-level: pairwise equal children in different order — one op, not N moves
            final int[] baseIndexOf = new int[tChildren.size()];
            for (int i = 0; i < pairOfBase.length; i++) {
                baseIndexOf[pairOfBase[i]] = i;
            }
            final List<String> newOrder = new ArrayList<>();
            for (int j = 0; j < tChildren.size(); j++) {
                newOrder.add(label(tChildren.get(j), baseIndexOf[j]));
            }
            report.add(DeltaOp.reorderOp(path, newOrder));
        } else {
            pendingMoves.forEach(report::add);
        }
    }

    /**
     * Post-pass over the report: a REMOVE and an ADD with the same, on both sides unique Merkle hash is the same
     * subtree at another position — one MOVE instead of REMOVE + ADD. Copies (duplicated hashes) stay REMOVE + ADD
     * to keep the pairing stable.
     */
    private void pairMoves() {
        final List<DeltaOp> ops = report.getOpsInternal();
        final Map<Long, List<DeltaOp>> removesByHash = new LinkedHashMap<>();
        final Map<Long, List<DeltaOp>> addsByHash = new LinkedHashMap<>();
        for (DeltaOp op : ops) {
            if (op.getType() == DeltaType.REMOVE && op.getSubtree() != null) {
                removesByHash.computeIfAbsent(op.getSubtree().getMerkleHash(), k -> new ArrayList<>()).add(op);
            } else if (op.getType() == DeltaType.ADD && op.getSubtree() != null) {
                addsByHash.computeIfAbsent(op.getSubtree().getMerkleHash(), k -> new ArrayList<>()).add(op);
            }
        }
        final List<DeltaOp> result = new ArrayList<>();
        final Map<DeltaOp, DeltaOp> consumed = new LinkedHashMap<>(); // add op consumed by a remove op
        for (DeltaOp op : ops) {
            final DeltaOp paired = pairIntoMove(op, removesByHash, addsByHash, consumed);
            if (paired != null) {
                result.add(paired);
            } else if (!consumed.containsKey(op)) {
                result.add(op);
            }
        }
        ops.clear();
        ops.addAll(result);
    }

    private DeltaOp pairIntoMove(DeltaOp op, Map<Long, List<DeltaOp>> removesByHash,
            Map<Long, List<DeltaOp>> addsByHash, Map<DeltaOp, DeltaOp> consumed) {
        if (op.getType() != DeltaType.REMOVE || op.getSubtree() == null) {
            return null;
        }
        final long hash = op.getSubtree().getMerkleHash();
        final List<DeltaOp> removes = removesByHash.get(hash);
        final List<DeltaOp> adds = addsByHash.get(hash);
        if (removes == null || adds == null || removes.size() != 1 || adds.size() != 1) {
            return null; // not unique on both sides: copies, not moves
        }
        final DeltaOp add = adds.get(0);
        if (!NodeValidator.isEqual(op.getSubtree(), add.getSubtree())) {
            return null; // hash collision: the pair is deliberately not matched
        }
        consumed.put(add, op);
        final Integer newIndex = add.getFieldOrIndex() != null && add.getFieldOrIndex().matches("\\d+")
                ? Integer.valueOf(add.getFieldOrIndex()) : null;
        return DeltaOp.moveOp(op.getKey(), op.getSubtree(), add.getParentKey(), newIndex,
                add.getFieldOrIndex(), op.isRadiating());
    }

    private static Map<String, DifferableNode> keyMap(DifferableNode node) {
        final Map<String, DifferableNode> result = new LinkedHashMap<>();
        int i = 0;
        for (DifferableNode child : node.children()) {
            final String key = child.getMemberKey() != null ? child.getMemberKey() : "#" + i;
            result.putIfAbsent(key, child);
            i++;
        }
        return result;
    }

    private static List<Integer> restIndexes(Integer[] pairOfBase) {
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < pairOfBase.length; i++) {
            if (pairOfBase[i] == null) {
                result.add(i);
            }
        }
        return result;
    }

    private static List<Integer> restIndexes(boolean[] taken) {
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < taken.length; i++) {
            if (!taken[i]) {
                result.add(i);
            }
        }
        return result;
    }

    private static Map<Long, List<Integer>> groupByHash(List<DifferableNode> children, List<Integer> indexes) {
        final Map<Long, List<Integer>> result = new HashMap<>();
        for (int j : indexes) {
            result.computeIfAbsent(children.get(j).getMerkleHash(), k -> new ArrayList<>()).add(j);
        }
        return result;
    }

    private static int countByHash(Map<Long, List<Integer>> byHash, long hash) {
        final List<Integer> list = byHash.get(hash);
        return list == null ? 0 : list.size();
    }

    private static boolean hasObjektId(DifferableNode node) {
        return node.getObjektId() != null;
    }

    private static String label(DifferableNode node, int baseIndex) {
        if (node.getMemberKey() != null) {
            return node.getMemberKey();
        }
        return node.getObjektId() != null ? node.getObjektId() : String.valueOf(baseIndex);
    }

    private static boolean equalsNullable(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    /**
     * Scalar comparison is tag-aware: a type-only change (string "5" to long 5) is a VALUE change.
     */
    private static boolean sameValue(DifferableNode b, DifferableNode t) {
        return equalsNullable(b.getValueTag(), t.getValueTag()) && equalsNullable(b.getValue(), t.getValue());
    }

    private static String valueText(DifferableNode node) {
        final String tag = node.getValueTag();
        final String value = node.getValue();
        return value == null ? tag : tag + ":" + value;
    }
}
