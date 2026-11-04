/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

/**
 * The property granularity of a {@link DeltaType#MODIFY} op: one MODIFY names one property, not the whole node —
 * the smaller the conflict surface, the better merges can auto-apply.
 *
 * <p>
 * An {@link #OBJECT_ID} change is never a MODIFY: a changed identity changes every qualified reference to the node,
 * so it is reported as REMOVE (old id) + ADD (new id), each flagged {@link #isRadiating() radiating}.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public enum DeltaProperty {

    /**
     * A scalar value change.
     */
    VALUE(false),
    /**
     * An object value / member key change.
     */
    NAME(false),
    /**
     * A type cast ({@code _class}) change.
     */
    CLASS(false),
    /**
     * A {@code _woodLink} target change — radiating: dependents are affected.
     */
    LINK_TARGET(true),
    /**
     * A wood id change — radiating: an identity change is reported as REMOVE + ADD, not MODIFY.
     */
    OBJECT_ID(true);

    private final boolean radiating;

    private DeltaProperty(boolean radiating) {
        this.radiating = radiating;
    }

    /**
     * Tells whether a change of this property radiates to dependent references.
     *
     * @return {@code true} when dependents are affected and the decision UI must show them.
     */
    public boolean isRadiating() {
        return radiating;
    }
}
