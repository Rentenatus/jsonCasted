/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.List;
import java.util.Objects;

/**
 * The mandatory confirmation step of the matching pipeline: every hash-equal candidate is confirmed by a deep equals
 * traversal before any match, prune, move or reorder decision takes effect — "hash filters, equals decides".
 *
 * <p>
 * The validation is a parallel postorder double-descent: children first, then the node's own local fields. Once the
 * children are validated equal, the parent check reduces to its own local fields plus the (now proven) child
 * content — no hash recomputation, no matching machinery. One descent therefore checks all nodes of a supposedly
 * identical subtree at once and surfaces a divergence precisely at the colliding node pair.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class NodeValidator {

    private NodeValidator() {
        // utility class, no instances
    }

    /**
     * Deep structural equality of two nodes over the semantic projection of {@link DifferableNode}.
     *
     * <p>
     * The node's own member key is not compared — it is the position the parent assigned, not content; a MOVE
     * candidate (same content, other position) must validate as equal. The children's member keys are compared:
     * they are internal structure of the subtree.
     * </p>
     *
     * @param left the base side node, never {@code null}.
     * @param right the theirs side node, never {@code null}.
     * @return {@code true} when kind, all semantic fields and — recursively — all children in order are equal.
     */
    public static boolean isEqual(DifferableNode left, DifferableNode right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left.getKind() != right.getKind()) {
            return false;
        }
        if (!Objects.equals(left.getObjektId(), right.getObjektId())
                || !Objects.equals(left.getLinkTarget(), right.getLinkTarget())
                || !Objects.equals(left.getName(), right.getName())
                || !Objects.equals(left.getValueTag(), right.getValueTag())
                || !Objects.equals(left.getValue(), right.getValue())
                || !Objects.equals(left.getCastName(), right.getCastName())) {
            return false;
        }
        final List<DifferableNode> leftChildren = left.children();
        final List<DifferableNode> rightChildren = right.children();
        if (leftChildren.size() != rightChildren.size()) {
            return false;
        }
        for (int i = 0; i < leftChildren.size(); i++) {
            if (!Objects.equals(leftChildren.get(i).getMemberKey(), rightChildren.get(i).getMemberKey())) {
                return false;
            }
            if (!isEqual(leftChildren.get(i), rightChildren.get(i))) {
                return false;
            }
        }
        return true;
    }
}
