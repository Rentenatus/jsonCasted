/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

/**
 * The merge policy of the 3-way merge (see doc/TREE_DIFF.md, U1).
 *
 * @author Janusch Rentenatus
 */
public enum MergePolicy {

    /**
     * Default: report conflicts, apply nothing without confirmation. The decision UI is out of scope of the merge
     * core.
     */
    ASK,
    /**
     * Auto-apply the non-overlapping theirs deltas, ask only on conflicts. Requires merge transactions (one undo
     * step per merge) to be usable.
     */
    SAFE_AUTO,
    /**
     * Never merge; only report (diff view).
     */
    NONE
}
