/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The 3-way merge analysis of use case U1 (see doc/TREE_DIFF.md): what the user changed ({@code D_ours}) versus what
 * an external file change brought ({@code D_theirs}), both against the common base. The analysis is pure — it
 * produces a {@link MergePlan} and mutates nothing.
 *
 * <p>
 * Overlap detection runs on (matchKey, property) pairs:
 * </p>
 * <ul>
 * <li>both sides changed the same property to equal values — no-op;</li>
 * <li>both changed it differently — conflict ({@link ConflictReason#VALUE_DIVERGENT});</li>
 * <li>one side removed a node whose subtree the other side modified anywhere — conflict
 * ({@link ConflictReason#REMOVED_VS_MODIFIED});</li>
 * <li>both added a member under the same key, or a theirs add hits a user-removed node — conflict
 * ({@link ConflictReason#STRUCTURAL_COLLISION});</li>
 * <li>theirs moved or reordered — structural change, needs a decision, not auto-mergeable in v1
 * ({@link ConflictReason#MOVE_OR_REORDER}).</li>
 * </ul>
 *
 * <p>
 * Everything else of theirs is non-overlapping and may be applied (commands stay layer-specific). V1 simplification,
 * documented: MOVE/REORDER application is deferred; the conflict record keeps nothing from being decided later.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class ThreeWayDiffer {

    private ThreeWayDiffer() {
        // utility class, no instances
    }

    /**
     * Analyzes the three sides. All three trees must be the same representation, converted through the same import
     * path — no heterogeneous runs.
     *
     * @param base the import state both sides started from, never {@code null}.
     * @param ours the user's current tree, never {@code null}.
     * @param theirs the fresh parse of the externally changed file, never {@code null}.
     * @return the merge plan, never {@code null}.
     */
    public static MergePlan analyze(DifferableNode base, DifferableNode ours, DifferableNode theirs) {
        final DiffReport oursReport = TreeDiffer.diff(base, ours);
        final DiffReport theirsReport = TreeDiffer.diff(base, theirs);
        final MergePlan plan = new MergePlan(oursReport, theirsReport);

        final Map<String, DeltaOp> oursModifyByProperty = new HashMap<>();
        for (DeltaOp op : oursReport.getOps(DeltaType.MODIFY)) {
            oursModifyByProperty.put(modifyKey(op.getKey(), op.getProperty()), op);
        }
        final List<DeltaOp> oursRemoves = oursReport.getOps(DeltaType.REMOVE);
        final List<DeltaOp> oursAdds = oursReport.getOps(DeltaType.ADD);
        final List<DeltaOp> oursOthers = oursReport.getOps(DeltaType.MOVE);
        oursOthers.addAll(oursReport.getOps(DeltaType.REORDER));

        for (DeltaOp theirsOp : theirsReport.getOps()) {
            switch (theirsOp.getType()) {
                case MODIFY:
                    analyzeTheirsModify(plan, oursModifyByProperty, oursRemoves, theirsOp);
                    break;
                case REMOVE:
                    analyzeTheirsRemove(plan, oursModifyByProperty, oursRemoves, oursAdds, oursOthers, theirsOp);
                    break;
                case ADD:
                    analyzeTheirsAdd(plan, oursAdds, oursRemoves, theirsOp);
                    break;
                default:
                    // MOVE / REORDER from theirs: structural, needs a decision (v1)
                    plan.addConflict(new ConflictRecord(theirsOp.getKey(), null,
                            findOursOpTouching(oursReport, theirsOp.getKey()), theirsOp,
                            ConflictReason.MOVE_OR_REORDER, theirsOp.isRadiating()));
                    break;
            }
        }
        return plan;
    }

    private static void analyzeTheirsModify(MergePlan plan, Map<String, DeltaOp> oursModifyByProperty,
            List<DeltaOp> oursRemoves, DeltaOp theirs) {
        final DeltaOp ours = oursModifyByProperty.get(modifyKey(theirs.getKey(), theirs.getProperty()));
        if (ours != null) {
            if (sameTargetValue(ours, theirs)) {
                plan.addNoOp(theirs); // both sides made the same change
            } else {
                plan.addConflict(new ConflictRecord(theirs.getKey(), theirs.getProperty(), ours, theirs,
                        ConflictReason.VALUE_DIVERGENT,
                        ours.isRadiating() || theirs.isRadiating()));
            }
            return;
        }
        final DeltaOp removedHere = findCoveringRemove(oursRemoves, theirs.getKey());
        if (removedHere != null) {
            // the user removed the containing node, the file changed inside it
            plan.addConflict(new ConflictRecord(theirs.getKey(), theirs.getProperty(), removedHere, theirs,
                    ConflictReason.REMOVED_VS_MODIFIED, theirs.isRadiating()));
            return;
        }
        plan.addApply(theirs);
    }

    private static void analyzeTheirsRemove(MergePlan plan, Map<String, DeltaOp> oursModifyByProperty,
            List<DeltaOp> oursRemoves, List<DeltaOp> oursAdds, List<DeltaOp> oursOthers, DeltaOp theirs) {
        // both sides removed the same subtree: the user already did it
        if (oursRemoves.stream().anyMatch(op -> op.getKey().equals(theirs.getKey()))) {
            plan.addNoOp(theirs);
            return;
        }
        // the user changed anything inside the subtree the file removed
        final DeltaOp oursInside = findOursOpInside(oursModifyByProperty.values(), oursAdds, oursOthers, theirs.getKey());
        if (oursInside != null) {
            plan.addConflict(new ConflictRecord(theirs.getKey(), oursInside.getProperty(), oursInside, theirs,
                    ConflictReason.REMOVED_VS_MODIFIED,
                    oursInside.isRadiating() || theirs.isRadiating()));
            return;
        }
        plan.addApply(theirs);
    }

    private static void analyzeTheirsAdd(MergePlan plan, List<DeltaOp> oursAdds, List<DeltaOp> oursRemoves,
            DeltaOp theirs) {
        for (DeltaOp ours : oursAdds) {
            final boolean sameSpot = ours.getParentKey().equals(theirs.getParentKey())
                    && equalsNullable(ours.getFieldOrIndex(), theirs.getFieldOrIndex());
            if (sameSpot) {
                if (sameSubtree(ours, theirs)) {
                    plan.addNoOp(theirs); // both sides added the same member
                } else {
                    plan.addConflict(new ConflictRecord(theirs.getKey(), null, ours, theirs,
                            ConflictReason.STRUCTURAL_COLLISION, theirs.isRadiating()));
                }
                return;
            }
        }
        // theirs re-adds what the user removed (same member key under the same parent)
        for (DeltaOp ours : oursRemoves) {
            if (ours.getKey().equals(theirs.getKey())) {
                plan.addConflict(new ConflictRecord(theirs.getKey(), null, ours, theirs,
                        ConflictReason.STRUCTURAL_COLLISION, ours.isRadiating() || theirs.isRadiating()));
                return;
            }
        }
        plan.addApply(theirs);
    }

    private static boolean sameTargetValue(DeltaOp ours, DeltaOp theirs) {
        // same property: equal new value texts mean both sides made the same change
        return equalsNullable(ours.getNewValue(), theirs.getNewValue());
    }

    private static boolean sameSubtree(DeltaOp ours, DeltaOp theirs) {
        return ours.getSubtree() != null && theirs.getSubtree() != null
                && ours.getSubtree().getMerkleHash() == theirs.getSubtree().getMerkleHash()
                && NodeValidator.isEqual(ours.getSubtree(), theirs.getSubtree());
    }

    private static DeltaOp findCoveringRemove(List<DeltaOp> oursRemoves, String key) {
        for (DeltaOp op : oursRemoves) {
            if (covers(op.getKey(), key)) {
                return op;
            }
        }
        return null;
    }

    private static DeltaOp findOursOpInside(Iterable<DeltaOp> oursModify, List<DeltaOp> oursAdds,
            List<DeltaOp> oursOthers, String removedKey) {
        for (DeltaOp op : oursModify) {
            if (covers(removedKey, op.getKey())) {
                return op;
            }
        }
        for (DeltaOp op : oursAdds) {
            if (covers(removedKey, op.getKey())) {
                return op;
            }
        }
        for (DeltaOp op : oursOthers) {
            if (covers(removedKey, op.getKey())) {
                return op;
            }
        }
        return null;
    }

    private static DeltaOp findOursOpTouching(DiffReport oursReport, String key) {
        for (DeltaOp op : oursReport.getOps()) {
            if (covers(op.getKey(), key) || covers(key, op.getKey())) {
                return op;
            }
        }
        return null;
    }

    /**
     * Tells whether the container path covers the member path, i.e. the member lies inside the container subtree.
     */
    private static boolean covers(String containerPath, String memberPath) {
        if (!memberPath.startsWith(containerPath)) {
            return false;
        }
        if (memberPath.length() == containerPath.length()) {
            return true;
        }
        final char next = memberPath.charAt(containerPath.length());
        return next == '.' || next == '[';
    }

    private static String modifyKey(String key, DeltaProperty property) {
        return key + "#" + property;
    }

    private static boolean equalsNullable(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
