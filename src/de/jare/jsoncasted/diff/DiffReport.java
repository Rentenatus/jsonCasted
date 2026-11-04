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
import java.util.stream.Collectors;

/**
 * The pure result of a tree diff: an ordered list of {@link DeltaOp}s plus the queries the layer-1 impact analysis
 * (use case U2 of the concept) needs — removed wood ids and radiating changes, ready to be intersected with the
 * linking set.
 *
 * @author Janusch Rentenatus
 */
public final class DiffReport {

    private final List<DeltaOp> ops;

    /**
     * Creates an empty report.
     */
    public DiffReport() {
        this.ops = new ArrayList<>();
    }

    /**
     * Adds one op to the report.
     *
     * @param op the op, never {@code null}.
     * @return this, for chaining.
     */
    public DiffReport add(DeltaOp op) {
        ops.add(op);
        return this;
    }

    /**
     * Tells whether the two trees were semantically identical — no deltas at all. This is also the semantic no-op
     * check of the concept: a purely cosmetic file change yields an empty report.
     *
     * @return {@code true} when nothing changed.
     */
    public boolean isEmpty() {
        return ops.isEmpty();
    }

    /**
     * Returns all delta ops in traversal order.
     *
     * @return unmodifiable list of ops.
     */
    public List<DeltaOp> getOps() {
        return Collections.unmodifiableList(ops);
    }

    /**
     * Internal mutable view for the engine's post-passes.
     *
     * @return the live op list of this report.
     */
    List<DeltaOp> getOpsInternal() {
        return ops;
    }

    /**
     * Returns the ops of the given type.
     *
     * @param type the op kind.
     * @return the matching ops, never {@code null}.
     */
    public List<DeltaOp> getOps(DeltaType type) {
        return ops.stream().filter(op -> op.getType() == type).collect(Collectors.toList());
    }

    /**
     * Returns the raw wood ids of all removed subtrees — the primary input of the U2 impact analysis ("object
     * {@code dep::5} removed — referenced by ...").
     *
     * @return the removed ids, without duplicates, {@code null} ids skipped.
     */
    public List<String> getRemovedObjektIds() {
        return ops.stream()
                .filter(op -> op.getType() == DeltaType.REMOVE)
                .map(op -> op.getSubtree() == null ? null : op.getSubtree().getObjektId())
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns all radiating ops — identity or link target changes whose decision must show the affected dependents.
     *
     * @return the radiating ops, never {@code null}.
     */
    public List<DeltaOp> getRadiatingOps() {
        return ops.stream().filter(DeltaOp::isRadiating).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return ops.stream().map(DeltaOp::toString).collect(Collectors.joining("\n"));
    }
}
