/*
 * Copyright (C) 2025 Janusch Rentenatus
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.descriptor.JsonDefinitionsDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Repository-like container for JSON type definitions.
 * <p>
 * A {@code JsonDefinitions} instance can contain multiple {@link JsonType} definitions as well as nested child
 * {@code JsonDefinitions}. This allows hierarchical definition spaces that can be used to collect and resolve
 * referenced types during serialization.
 * </p>
 *
 * <p>
 * Typical responsibilities include:
 * </p>
 * <ul>
 * <li>Holding locally defined {@link JsonType}s</li>
 * <li>Organizing nested definition scopes</li>
 * <li>Providing traversal and lookup methods</li>
 * <li>Serving as a model anchor for reference collection</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class JsonDefinitions {

    private final String name;
    private JsonDefinitions parent;
    private final ArrayList<JsonType> types;
    private final ArrayList<JsonDefinitions> children;

    /**
     * Creates a new unnamed definitions container.
     */
    public JsonDefinitions() {
        this(null);
    }

    /**
     * Creates a new definitions container with the given name.
     *
     * @param name The optional name of this definition scope.
     */
    public JsonDefinitions(String name) {
        this.name = name;
        this.parent = null;
        this.types = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    /**
     * Returns the name of this definitions container.
     *
     * @return The name, or {@code null} if unnamed.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent definitions container.
     *
     * @return The parent, or {@code null} if this is a root container.
     */
    public JsonDefinitions getParent() {
        return parent;
    }

    /**
     * Returns whether this definitions container has a parent.
     *
     * @return {@code true} if a parent exists, otherwise {@code false}.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns whether this definitions container contains any local types.
     *
     * @return {@code true} if local types exist, otherwise {@code false}.
     */
    public boolean hasTypes() {
        return !types.isEmpty();
    }

    /**
     * Returns whether this definitions container contains any child containers.
     *
     * @return {@code true} if child definitions exist, otherwise {@code false}.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Returns whether this definitions container is empty.
     *
     * @return {@code true} if it has neither types nor children.
     */
    public boolean isEmpty() {
        return types.isEmpty() && children.isEmpty();
    }

    /**
     * Returns the number of locally contained types.
     *
     * @return The local type count.
     */
    public int typeCount() {
        return types.size();
    }

    /**
     * Returns the number of direct child definition containers.
     *
     * @return The child count.
     */
    public int childCount() {
        return children.size();
    }

    /**
     * Adds a type to this definitions container.
     *
     * @param type The type to add.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    public void addType(JsonType type) {
        if (type == null) {
            throw new NullPointerException("JsonType is null.");
        }
        types.add(type);
    }

    /**
     * Adds a type only if it is not already contained by identity or by equivalent name/node type combination.
     *
     * @param type The type to add.
     * @return {@code true} if the type was added, otherwise {@code false}.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    public boolean addTypeIfAbsent(JsonType type) {
        if (type == null) {
            throw new NullPointerException("JsonType is null.");
        }
        if (containsLocalType(type)) {
            return false;
        }
        types.add(type);
        return true;
    }

    /**
     * Removes the given local type.
     *
     * @param type The type to remove.
     * @return {@code true} if removed, otherwise {@code false}.
     */
    public boolean removeType(JsonType type) {
        return types.remove(type);
    }

    /**
     * Removes the first local type with the given canonical name.
     *
     * @param cName The canonical name.
     * @return The removed type, or {@code null} if not found.
     */
    public JsonType removeType(String cName) {
        for (int i = 0; i < types.size(); i++) {
            JsonType next = types.get(i);
            if (equalsName(next, cName)) {
                return types.remove(i);
            }
        }
        return null;
    }

    /**
     * Adds a child definitions container.
     *
     * @param child The child to add.
     * @throws NullPointerException if {@code child} is {@code null}.
     * @throws IllegalArgumentException if {@code child == this}, if the child already has a different parent, or if
     * adding it would create a cycle.
     */
    public void addChild(JsonDefinitions child) {
        if (child == null) {
            throw new NullPointerException("JsonDefinitions child is null.");
        }
        if (child == this) {
            throw new IllegalArgumentException("A JsonDefinitions cannot contain itself as child.");
        }
        if (isAncestorOf(child)) {
            throw new IllegalArgumentException("Adding this child would create a circular definitions hierarchy.");
        }
        if (child.parent != null && child.parent != this) {
            throw new IllegalArgumentException("The child JsonDefinitions already has another parent.");
        }
        if (!children.contains(child)) {
            child.parent = this;
            children.add(child);
        }
    }

    /**
     * Removes the given direct child definitions container.
     *
     * @param child The child to remove.
     * @return {@code true} if removed, otherwise {@code false}.
     */
    public boolean removeChild(JsonDefinitions child) {
        if (child == null) {
            return false;
        }
        boolean removed = children.remove(child);
        if (removed) {
            child.parent = null;
        }
        return removed;
    }

    /**
     * Removes the first direct child with the given name.
     *
     * @param name The child name.
     * @return The removed child, or {@code null} if not found.
     */
    public JsonDefinitions removeChild(String name) {
        for (int i = 0; i < children.size(); i++) {
            JsonDefinitions next = children.get(i);
            if (equalsString(next.getName(), name)) {
                children.remove(i);
                next.parent = null;
                return next;
            }
        }
        return null;
    }

    /**
     * Returns whether this container contains the given type locally.
     *
     * @param type The type to check.
     * @return {@code true} if locally contained, otherwise {@code false}.
     */
    public boolean containsLocalType(JsonType type) {
        if (type == null) {
            return false;
        }
        for (JsonType next : types) {
            if (next == type) {
                return true;
            }
            if (sameType(next, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether this container contains a local type with the given name.
     *
     * @param cName The canonical type name.
     * @return {@code true} if found locally, otherwise {@code false}.
     */
    public boolean containsLocalType(String cName) {
        return getLocalType(cName) != null;
    }

    /**
     * Returns whether this container contains the given child directly.
     *
     * @param child The child to check.
     * @return {@code true} if contained directly, otherwise {@code false}.
     */
    public boolean containsChild(JsonDefinitions child) {
        return children.contains(child);
    }

    /**
     * Returns whether this container contains a direct child with the given name.
     *
     * @param name The child name.
     * @return {@code true} if found directly, otherwise {@code false}.
     */
    public boolean containsChild(String name) {
        return getChild(name) != null;
    }

    /**
     * Returns the first local type with the given canonical name.
     *
     * @param cName The canonical type name.
     * @return The local type, or {@code null} if not found.
     */
    public JsonType getLocalType(String cName) {
        for (JsonType next : types) {
            if (equalsName(next, cName)) {
                return next;
            }
        }
        return null;
    }

    /**
     * Returns the direct child with the given name.
     *
     * @param name The child name.
     * @return The child, or {@code null} if not found.
     */
    public JsonDefinitions getChild(String name) {
        for (JsonDefinitions next : children) {
            if (equalsString(next.getName(), name)) {
                return next;
            }
        }
        return null;
    }

    /**
     * Finds a type in this container or recursively in its children.
     *
     * @param cName The canonical type name.
     * @return The first matching type, or {@code null} if not found.
     */
    public JsonType findType(String cName) {
        JsonType local = getLocalType(cName);
        if (local != null) {
            return local;
        }
        for (JsonDefinitions child : children) {
            JsonType nested = child.findType(cName);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Finds a definitions container in this subtree by name.
     *
     * @param name The name to search for.
     * @return The matching definitions container, or {@code null} if not found.
     */
    public JsonDefinitions findDefinitions(String name) {
        if (equalsString(this.name, name)) {
            return this;
        }
        for (JsonDefinitions child : children) {
            JsonDefinitions nested = child.findDefinitions(name);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Returns all local types as an immutable list.
     *
     * @return Immutable view of local types.
     */
    public List<JsonType> getTypes() {
        return Collections.unmodifiableList(types);
    }

    /**
     * Returns all direct children as an immutable list.
     *
     * @return Immutable view of child definitions.
     */
    public List<JsonDefinitions> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns an iterator over local types.
     *
     * @return Iterator over local types.
     */
    public Iterator<JsonType> typesIterator() {
        return getTypes().iterator();
    }

    /**
     * Returns an iterator over direct child definitions.
     *
     * @return Iterator over direct child definitions.
     */
    public Iterator<JsonDefinitions> childrenIterator() {
        return getChildren().iterator();
    }

    /**
     * Returns all types from this subtree in preorder as an immutable list.
     *
     * @return Immutable list of all nested types including local ones.
     */
    public List<JsonType> getTypesDeep() {
        ArrayList<JsonType> ret = new ArrayList<>();
        collectTypesDeep(ret);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns all child definitions from this subtree in preorder as an immutable list.
     *
     * @return Immutable list of all nested child definitions.
     */
    public List<JsonDefinitions> getChildrenDeep() {
        ArrayList<JsonDefinitions> ret = new ArrayList<>();
        collectChildrenDeep(ret);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns an iterator over all subtree types in preorder.
     *
     * @return Iterator over all nested types.
     */
    public Iterator<JsonType> typesDeepIterator() {
        return getTypesDeep().iterator();
    }

    /**
     * Returns an iterator over all subtree child definitions in preorder.
     *
     * @return Iterator over all nested child definitions.
     */
    public Iterator<JsonDefinitions> childrenDeepIterator() {
        return getChildrenDeep().iterator();
    }

    /**
     * Returns the root definitions container.
     *
     * @return The root of the current hierarchy.
     */
    public JsonDefinitions getRoot() {
        JsonDefinitions current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Returns the depth of this definitions container in the hierarchy.
     *
     * @return {@code 0} for a root container, otherwise the ancestor distance.
     */
    public int getDepth() {
        int ret = 0;
        JsonDefinitions current = parent;
        while (current != null) {
            ret++;
            current = current.parent;
        }
        return ret;
    }

    /**
     * Removes all local types.
     */
    public void clearTypes() {
        types.clear();
    }

    /**
     * Removes all child definitions and resets their parent references.
     */
    public void clearChildren() {
        for (JsonDefinitions child : children) {
            child.parent = null;
        }
        children.clear();
    }

    /**
     * Removes all local types and all child definitions.
     */
    public void clear() {
        clearTypes();
        clearChildren();
    }

    private void collectTypesDeep(List<JsonType> result) {
        result.addAll(types);
        for (JsonDefinitions child : children) {
            child.collectTypesDeep(result);
        }
    }

    private void collectChildrenDeep(List<JsonDefinitions> result) {
        for (JsonDefinitions child : children) {
            result.add(child);
            child.collectChildrenDeep(result);
        }
    }

    private boolean isAncestorOf(JsonDefinitions candidateChild) {
        JsonDefinitions current = this;
        while (current != null) {
            if (current == candidateChild) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    private static boolean equalsName(JsonType type, String cName) {
        if (type == null) {
            return cName == null;
        }
        return equalsString(type.getcName(), cName);
    }

    private static boolean sameType(JsonType left, JsonType right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return equalsString(left.getcName(), right.getcName())
                && left.getNodeType() == right.getNodeType();
    }

    private static boolean equalsString(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    public JsonDefinitionsDescriptor describe() {
        JsonDefinitionsDescriptor ret = new JsonDefinitionsDescriptor(name);

        for (JsonType type : types) {
            ret.addType(type.getcName());
        }
        for (JsonDefinitions child : children) {
            ret.addChild(child.describe());
        }
        return ret;
    }

    @Override
    public String toString() {
        return "JsonDefinitions{"
                + "name=" + name
                + ", typeCount=" + types.size()
                + ", childCount=" + children.size()
                + '}';
    }
}
