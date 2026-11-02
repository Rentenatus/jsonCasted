/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.Objects;

/**
 * One conflict of the 3-way merge: a theirs delta that cannot be auto-applied because the user changed the same
 * spot (or the conflict is structural and needs a decision). Input for the later decision UI; the merge core only
 * produces records, it never resolves them.
 *
 * @author Janusch Rentenatus
 */
public final class ConflictRecord {

    private final String matchKey;
    private final DeltaProperty property;
    private final DeltaOp oursDelta;
    private final DeltaOp theirsDelta;
    private final ConflictReason reason;
    private final boolean radiating;

    /**
     * Creates a conflict record.
     *
     * @param matchKey the contested path.
     * @param property the contested property, or {@code null} for structural conflicts.
     * @param oursDelta the user's delta, or {@code null} when only theirs acted structurally.
     * @param theirsDelta the external delta.
     * @param reason why the deltas conflict.
     * @param radiating whether the decision must show affected dependents.
     */
    public ConflictRecord(String matchKey, DeltaProperty property, DeltaOp oursDelta, DeltaOp theirsDelta,
            ConflictReason reason, boolean radiating) {
        this.matchKey = matchKey;
        this.property = property;
        this.oursDelta = oursDelta;
        this.theirsDelta = theirsDelta;
        this.reason = reason;
        this.radiating = radiating;
    }

    /**
     * @return the contested path.
     */
    public String getMatchKey() {
        return matchKey;
    }

    /**
     * @return the contested property, or {@code null} for structural conflicts.
     */
    public DeltaProperty getProperty() {
        return property;
    }

    /**
     * @return the user's delta, or {@code null}.
     */
    public DeltaOp getOursDelta() {
        return oursDelta;
    }

    /**
     * @return the external delta, never {@code null}.
     */
    public DeltaOp getTheirsDelta() {
        return theirsDelta;
    }

    /**
     * @return why the deltas conflict.
     */
    public ConflictReason getReason() {
        return reason;
    }

    /**
     * Tells whether this conflict radiates to dependents (identity or link target change) — the decision must show
     * them.
     *
     * @return {@code true} when dependents are affected.
     */
    public boolean isRadiating() {
        return radiating;
    }

    @Override
    public String toString() {
        return "CONFLICT " + matchKey + (property != null ? "." + property.name() : "")
                + " [" + reason + "]"
                + " ours: " + (oursDelta == null ? "-" : oursDelta)
                + " / theirs: " + theirsDelta
                + (radiating ? " [RADIATING]" : "");
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchKey, property, oursDelta, theirsDelta, reason, radiating);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConflictRecord)) {
            return false;
        }
        final ConflictRecord other = (ConflictRecord) obj;
        return Objects.equals(matchKey, other.matchKey) && property == other.property
                && Objects.equals(oursDelta, other.oursDelta) && Objects.equals(theirsDelta, other.theirsDelta)
                && reason == other.reason && radiating == other.radiating;
    }
}
