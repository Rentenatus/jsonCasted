/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

import java.util.List;

/**
 * The one canonical Merkle hash recipe for the whole diff machinery (see {@code doc/TREE_DIFF.md}, Merkle section).
 * Both representations ({@code JsonNode}, {@code EditNodeAbstract}) must agree bit for bit, therefore the mixer lives
 * exactly once, here.
 *
 * <p>
 * Recipe: a 64-bit FNV-1a mixer over a canonical field serialization of the node — kind, semantic fields (wood id,
 * link target, member key, name, tagged value, cast), the child count (cheap collision prophylaxis), then per child
 * in order the member key and the child's Merkle hash. Order-sensitive by design: a REORDER must change the parent
 * hash. Never covered: parse/edit state, per-parse ids, descriptor object identity, parent pointers.
 * </p>
 *
 * <p>
 * Deliberately out: the existing edit hash ({@code lastParsedHash}, 32-bit, local, {@code editId}-dependent) keeps its
 * own reparse-trigger role and is never conflated with this hash, neither by name nor by field.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class MerkleHasher {

    /**
     * FNV-1a 64-bit offset basis.
     */
    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    /**
     * FNV-1a 64-bit prime.
     */
    private static final long FNV_PRIME = 0x100000001b3L;
    /**
     * Separator sentinel between canonical fields (never occurs inside a mixed char pair).
     */
    private static final int SEPARATOR = 0x1f;

    private MerkleHasher() {
        // utility class, no instances
    }

    /**
     * Computes the canonical Merkle hash of the subtree rooted at the given node. Reads the children's
     * {@link DifferableNode#getMerkleHash()} — implementations memoize, so calling this bottom-up costs one visit
     * per node.
     *
     * <p>
     * The node's own member key is deliberately not mixed: it is the position the parent assigned, not content — a
     * subtree found under another key must hash equally, otherwise MOVE detection (hash bucket) is impossible. The
     * children's member keys are mixed (parent structure, order-sensitive).
     * </p>
     *
     * @param node the subtree root, never {@code null}.
     * @return the 64-bit Merkle hash.
     */
    public static long nodeHash(DifferableNode node) {
        long h = FNV_OFFSET;
        h = mixField(h, node.getKind() == null ? "" : node.getKind().name());
        h = mixField(h, node.getObjektId());
        h = mixField(h, node.getLinkTarget());
        h = mixField(h, node.getName());
        h = mixField(h, node.getValueTag());
        h = mixField(h, node.getValue());
        h = mixField(h, DifferableNode.contentCast(node)); // inferred casts are parse feedback, not content
        final List<DifferableNode> children = node.children();
        h = mixLong(h, children.size());
        for (DifferableNode child : children) {
            h = mixField(h, child.getMemberKey());
            h = mixLong(h, child.getMerkleHash());
        }
        return h;
    }

    /**
     * Mixes one optional text field with a presence flag, so {@code null} and {@code ""} stay distinguishable and
     * field boundaries cannot be forged across values.
     *
     * @param h the running hash.
     * @param text the field text, or {@code null}.
     * @return the updated hash.
     */
    public static long mixField(long h, String text) {
        if (text == null) {
            return mixByte(h, 0x00);
        }
        h = mixByte(h, 0x01);
        final int len = text.length();
        for (int i = 0; i < len; i++) {
            h = mixByte(h, text.charAt(i) & 0xff);
            h = mixByte(h, (text.charAt(i) >>> 8) & 0xff);
        }
        return mixByte(h, SEPARATOR);
    }

    /**
     * Mixes one 64-bit value as eight little-endian bytes, framed by sentinels.
     *
     * @param h the running hash.
     * @param value the value.
     * @return the updated hash.
     */
    public static long mixLong(long h, long value) {
        h = mixByte(h, SEPARATOR);
        for (int i = 0; i < 8; i++) {
            h = mixByte(h, (int) ((value >>> (8 * i)) & 0xff));
        }
        return mixByte(h, SEPARATOR);
    }

    private static long mixByte(long h, int b) {
        h ^= b & 0xff;
        h *= FNV_PRIME;
        return h;
    }
}
