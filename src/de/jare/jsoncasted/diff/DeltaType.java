/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

/**
 * The delta operation kinds the diff engine emits. A diff never mutates either side — the ops are a pure report;
 * applying them is layer-specific.
 *
 * @author Janusch Rentenatus
 */
public enum DeltaType {

    /**
     * A subtree was added under a parent (identified by parent path and member key / array index).
     */
    ADD,
    /**
     * A subtree was removed (identified by its path in the base tree).
     */
    REMOVE,
    /**
     * One property of a matched node changed its value.
     */
    MODIFY,
    /**
     * A subtree changed position (same content and identity, other parent and/or member key / index).
     */
    MOVE,
    /**
     * The children of one parent are identical as a set but appear in a different order — reported as one op, not N
     * moves.
     */
    REORDER
}
