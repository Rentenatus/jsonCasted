/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

/**
 * Why a theirs delta could not be auto-applied in the 3-way merge.
 *
 * @author Janusch Rentenatus
 */
public enum ConflictReason {

    /**
     * Both sides changed the same property, to different values.
     */
    VALUE_DIVERGENT,
    /**
     * One side removed a node whose subtree the other side modified.
     */
    REMOVED_VS_MODIFIED,
    /**
     * Both sides added a member under the same parent with the same key or index, or a theirs add hits a node the
     * user removed.
     */
    STRUCTURAL_COLLISION,
    /**
     * The theirs side moved or reordered; structural changes need a decision, they are not auto-mergeable in v1.
     */
    MOVE_OR_REORDER
}
