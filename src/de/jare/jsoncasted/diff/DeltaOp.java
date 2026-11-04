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
 * One delta operation of a tree diff, per the concept in {@code doc/TREE_DIFF.md}. Immutable value object; the diff
 * engine only emits ops — applying them (commands on the edit layer, keep/switch policy on the parse layer) stays
 * layer-specific.
 *
 * <p>
 * Paths are JSON-ish match keys built during traversal, e.g. {@code $.content[2].ref}. {@code key} always addresses
 * the base side; for ADD and MOVE the new position is carried by {@code parentKey}/{@code fieldOrIndex} resp.
 * {@code newParentKey}/{@code newIndex}.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public final class DeltaOp {

    private final DeltaType type;
    private final String key;
    private final DeltaProperty property;
    private final String oldValue;
    private final String newValue;
    private final DifferableNode subtree;
    private final String parentKey;
    private final String fieldOrIndex;
    private final String newParentKey;
    private final Integer newIndex;
    private final List<String> newOrder;
    private final boolean radiating;

    private DeltaOp(DeltaType type, String key, DeltaProperty property,
            String oldValue, String newValue, DifferableNode subtree,
            String parentKey, String fieldOrIndex,
            String newParentKey, Integer newIndex, List<String> newOrder,
            boolean radiating) {
        this.type = type;
        this.key = key;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.subtree = subtree;
        this.parentKey = parentKey;
        this.fieldOrIndex = fieldOrIndex;
        this.newParentKey = newParentKey;
        this.newIndex = newIndex;
        this.newOrder = newOrder == null ? List.of() : List.copyOf(newOrder);
        this.radiating = radiating;
    }

    /**
     * ADD of a subtree under a parent, addressed by member key or array index.
     *
     * @param parentKey the path of the new parent (theirs side).
     * @param fieldOrIndex the member key, or the array index as text.
     * @param subtree the added subtree (theirs side).
     * @return the op.
     */
    public static DeltaOp addOp(String parentKey, String fieldOrIndex, DifferableNode subtree) {
        return new DeltaOp(DeltaType.ADD, childPath(parentKey, fieldOrIndex), null,
                null, null, subtree, parentKey, fieldOrIndex, null, null, null, false);
    }

    /**
     * REMOVE of a base subtree. Radiating when the removed node carried a wood id that references may use.
     *
     * @param key the path of the removed node (base side).
     * @param subtree the removed subtree (base side).
     * @param radiating {@code true} when the removal radiates to dependents.
     * @return the op.
     */
    public static DeltaOp removeOp(String key, DifferableNode subtree, boolean radiating) {
        return new DeltaOp(DeltaType.REMOVE, key, null,
                null, null, subtree, null, null, null, null, null, radiating);
    }

    /**
     * MODIFY of one property of a matched node.
     *
     * @param key the path of the node (base side).
     * @param property the changed property.
     * @param oldValue the old value text.
     * @param newValue the new value text.
     * @return the op.
     */
    public static DeltaOp modifyOp(String key, DeltaProperty property, String oldValue, String newValue) {
        return new DeltaOp(DeltaType.MODIFY, key, property,
                oldValue, newValue, null, null, null, null, null, null, property.isRadiating());
    }

    /**
     * MOVE of a subtree to a new parent position. Radiating when the moved node carried a wood id.
     *
     * @param key the path of the node (base side).
     * @param subtree the moved subtree.
     * @param newParentKey the path of the new parent (theirs side).
     * @param newIndex the new array index, or {@code null} for a member key move.
     * @param newFieldOrIndex the new member key, or the new array index as text.
     * @param radiating {@code true} when the move radiates to dependents.
     * @return the op.
     */
    public static DeltaOp moveOp(String key, DifferableNode subtree,
            String newParentKey, Integer newIndex, String newFieldOrIndex, boolean radiating) {
        return new DeltaOp(DeltaType.MOVE, key, null,
                null, null, subtree, null, newFieldOrIndex, newParentKey, newIndex, null, radiating);
    }

    /**
     * REORDER of the children of one parent: the set of children is unchanged, the order is not.
     *
     * @param parentKey the path of the parent.
     * @param newOrder the theirs-side order, as member keys / ids / index texts.
     * @return the op.
     */
    public static DeltaOp reorderOp(String parentKey, List<String> newOrder) {
        return new DeltaOp(DeltaType.REORDER, parentKey, null,
                null, null, null, parentKey, null, null, null, newOrder, false);
    }

    /**
     * Builds the path of a child under a parent path.
     *
     * @param parentKey the parent path, e.g. {@code $} or {@code $.a[0]}.
     * @param fieldOrIndex the member key, or the array index as text.
     * @return the child path, e.g. {@code $.a[0].b} or {@code $.a[2]}.
     */
    public static String childPath(String parentKey, String fieldOrIndex) {
        if (fieldOrIndex == null) {
            return parentKey;
        }
        if (fieldOrIndex.matches("\\d+")) {
            return parentKey + "[" + fieldOrIndex + "]";
        }
        return parentKey + "." + fieldOrIndex;
    }

    /**
     * @return the op kind, never {@code null}.
     */
    public DeltaType getType() {
        return type;
    }

    /**
     * @return the match key (base-side path; for ADD the theirs-side path).
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the changed property for MODIFY, else {@code null}.
     */
    public DeltaProperty getProperty() {
        return property;
    }

    /**
     * @return the old value text for MODIFY, else {@code null}.
     */
    public String getOldValue() {
        return oldValue;
    }

    /**
     * @return the new value text for MODIFY, else {@code null}.
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * @return the subtree for ADD/REMOVE/MOVE, else {@code null}.
     */
    public DifferableNode getSubtree() {
        return subtree;
    }

    /**
     * @return the parent path for ADD (theirs side), else {@code null}.
     */
    public String getParentKey() {
        return parentKey;
    }

    /**
     * @return the member key or array index text for ADD, else {@code null}.
     */
    public String getFieldOrIndex() {
        return fieldOrIndex;
    }

    /**
     * @return the new parent path for MOVE (theirs side), else {@code null}.
     */
    public String getNewParentKey() {
        return newParentKey;
    }

    /**
     * @return the new array index for MOVE, else {@code null}.
     */
    public Integer getNewIndex() {
        return newIndex;
    }

    /**
     * @return the theirs-side order for REORDER, else an empty list.
     */
    public List<String> getNewOrder() {
        return newOrder;
    }

    /**
     * Tells whether this op radiates to dependents (identity or link target change, or a radiating removal).
     *
     * @return {@code true} when the decision must show affected dependents.
     */
    public boolean isRadiating() {
        return radiating;
    }

    @Override
    public String toString() {
        switch (type) {
            case ADD:
                return "ADD " + key + " (" + describe(subtree) + ")";
            case REMOVE:
                return "REMOVE " + key + (radiating ? " [RADIATING]" : "");
            case MODIFY:
                return "MODIFY " + key + "." + property.name()
                        + " (" + oldValue + " -> " + newValue + ")"
                        + (radiating ? " [RADIATING]" : "");
            case MOVE:
                return "MOVE " + key + " -> " + newParentKey
                        + (newIndex != null ? "[" + newIndex + "]" : "")
                        + (radiating ? " [RADIATING]" : "");
            case REORDER:
                return "REORDER " + key + " -> " + newOrder;
            default:
                return type + " " + key;
        }
    }

    private static String describe(DifferableNode node) {
        return node == null ? "?" : node.getKind().name();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key, property, oldValue, newValue, newParentKey, newIndex, newOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DeltaOp)) {
            return false;
        }
        final DeltaOp other = (DeltaOp) obj;
        return type == other.type && Objects.equals(key, other.key)
                && property == other.property && Objects.equals(oldValue, other.oldValue)
                && Objects.equals(newValue, other.newValue) && Objects.equals(newParentKey, other.newParentKey)
                && Objects.equals(newIndex, other.newIndex) && Objects.equals(newOrder, other.newOrder);
    }
}
