/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.List;

/**
 * Read-only semantic projection of a tree node, as required by the tree diff concept (see {@code doc/TREE_DIFF.md}).
 * Implemented by {@code JsonNode} (parse layer, in jsonCasted) and {@code EditNodeAbstract} (edit layer, in
 * jsonCasted_edit), so the diff engine is written once and runs on both representations — but never mixed within
 * one invocation.
 *
 * <p>
 * Design boundaries (binding, see concept):
 * </p>
 * <ul>
 * <li>Read-only: parse state, edit status, ranges, parents — none of that enters the projection. The diff sees the
 * semantic content and nothing else.</li>
 * <li>No mutation API: the engine emits {@link DeltaOp}s; applying them is layer-specific and stays outside the
 * engine.</li>
 * <li>Children are ordered, each with an optional member key ({@code null} for array elements). Object children of
 * keyed maps carry their member key on the child (set on insertion).</li>
 * <li>{@link #getMerkleHash()} is part of the contract: computed bottom-up at parse, maintained per edit on the edit
 * layer. The canonical mixer lives once in {@link MerkleHasher} — both sides must agree bit for bit.</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public interface DifferableNode {

    /**
     * Returns the semantic kind of this node.
     *
     * @return the kind, never {@code null}.
     */
    NodeKind getKind();

    /**
     * Returns the raw, unqualified wood identity of this node (no provider prefix). On objects the
     * {@code _woodObjectId} member wins; nodes that cannot carry members use their identity field.
     *
     * @return the raw id text, or {@code null} when the node has no identity.
     */
    String getObjektId();

    /**
     * Returns the raw link target of this node ({@code _woodLink} member text, without provider normalization).
     *
     * @return the raw link target text, or {@code null} when the node carries no link.
     */
    String getLinkTarget();

    /**
     * Returns the member key under which this node sits in its parent, or {@code null} for array elements and roots.
     *
     * @return the member key, or {@code null}.
     */
    String getMemberKey();

    /**
     * Returns the name of this node: the object value / cast name on the edit layer. The parse layer returns
     * {@code null} — there the member name is the position and is exposed by {@link #getMemberKey()} only, never
     * duplicated here, so a subtree under another key hashes and validates as the same content (MOVE detection).
     *
     * @return the name, or {@code null}.
     */
    String getName();

    /**
     * Returns the scalar value text of a VALUE node (raw text, without type tag — the tag is exposed by
     * {@link #getValueTag()}). Non-VALUE nodes return {@code null} on the parse layer; on the edit layer object
     * nodes return their object value, which is also exposed as {@link #getName()}.
     *
     * @return the raw value text, or {@code null}.
     */
    String getValue();

    /**
     * Returns the scalar type tag of this node: {@code "string"}, {@code "long"}, {@code "number"},
     * {@code "boolean"} or {@code "null"} for nodes carrying a scalar, {@code null} otherwise. The tag is part of
     * the Merkle hash and of the VALUE comparison, so a type-only change (string "5" to long 5) is a VALUE change,
     * never a semantic no-op.
     *
     * @return the scalar type tag, or {@code null}.
     */
    String getValueTag();

    /**
     * Returns the class/cast name of this node ({@code _class} member), or {@code null}.
     *
     * @return the cast name, or {@code null}.
     */
    String getCastName();

    /**
     * Returns the Merkle subtree hash of this node (see {@link MerkleHasher} for the canonical recipe). Equal hashes
     * are a filter candidate only — every hash-equal pairing must be confirmed by a deep equals validation before it
     * takes effect ("hash filters, equals decides").
     *
     * @return the 64-bit Merkle hash of the whole subtree rooted at this node.
     */
    long getMerkleHash();

    /**
     * Returns the children of this node in stable traversal order (insertion order on the parse layer). Array
     * elements have a {@code null} member key; object members carry theirs. Semantic identity, link and cast
     * members are exposed via the projection fields ({@link #getObjektId()}, {@link #getLinkTarget()},
     * {@link #getCastName()}) and are deliberately not duplicated as children.
     *
     * @return the ordered children, never {@code null}; empty for VALUE nodes.
     */
    List<DifferableNode> children();
}
