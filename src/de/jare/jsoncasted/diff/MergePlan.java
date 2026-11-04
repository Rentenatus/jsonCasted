/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The pure result of the 3-way analysis (see doc/TREE_DIFF.md, U1): which theirs deltas may be applied to ours, which
 * are conflicts awaiting a decision, and which both sides already did identically. The plan is representation-free —
 * applying {@link #getApplyOps()} is layer-specific (commands on the edit layer).
 *
 * @author Janusch Rentenatus
 */
public final class MergePlan {

    private final DiffReport oursReport;
    private final DiffReport theirsReport;
    private final List<DeltaOp> applyOps;
    private final List<DeltaOp> noOps;
    private final List<ConflictRecord> conflicts;

    /**
     * Creates an empty plan over the two side reports.
     *
     * @param oursReport the diff base against ours, never {@code null}.
     * @param theirsReport the diff base against theirs, never {@code null}.
     */
    public MergePlan(DiffReport oursReport, DiffReport theirsReport) {
        this.oursReport = oursReport;
        this.theirsReport = theirsReport;
        this.applyOps = new ArrayList<>();
        this.noOps = new ArrayList<>();
        this.conflicts = new ArrayList<>();
    }

    /**
     * Marks a theirs delta as auto-applicable (no overlap with the user's changes).
     *
     * @param op the theirs delta.
     */
    public void addApply(DeltaOp op) {
        applyOps.add(op);
    }

    /**
     * Marks a theirs delta as a no-op: the user already made the same change.
     *
     * @param op the theirs delta.
     */
    public void addNoOp(DeltaOp op) {
        noOps.add(op);
    }

    /**
     * Records a conflict awaiting a decision.
     *
     * @param record the conflict record.
     */
    public void addConflict(ConflictRecord record) {
        conflicts.add(record);
    }

    /**
     * @return the theirs deltas that may be applied to ours, in diff order.
     */
    public List<DeltaOp> getApplyOps() {
        return Collections.unmodifiableList(applyOps);
    }

    /**
     * @return the theirs deltas the user already made identically.
     */
    public List<DeltaOp> getNoOps() {
        return Collections.unmodifiableList(noOps);
    }

    /**
     * @return the conflicts awaiting a decision.
     */
    public List<ConflictRecord> getConflicts() {
        return Collections.unmodifiableList(conflicts);
    }

    /**
     * Tells whether any conflict awaits a decision.
     *
     * @return {@code true} when the merge needs decisions.
     */
    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }

    /**
     * @return the full diff of base against ours (the user's changes).
     */
    public DiffReport getOursReport() {
        return oursReport;
    }

    /**
     * @return the full diff of base against theirs (the external changes).
     */
    public DiffReport getTheirsReport() {
        return theirsReport;
    }

    @Override
    public String toString() {
        return "merge plan: " + applyOps.size() + " apply, " + noOps.size() + " no-op, "
                + conflicts.size() + " conflict(s)";
    }
}
