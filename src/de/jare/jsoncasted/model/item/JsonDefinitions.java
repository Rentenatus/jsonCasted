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
 * Hierarchical container that defines where referenced objects are written in a JSON structure.
 * <p>
 * A {@code JsonDefinitions} instance represents a definition scope within a model. It provides the structural place in
 * which objects can be collected and written as separate definitions instead of being serialized inline at every
 * occurrence.
 * </p>
 *
 * <p>
 * The actual object identity is intentionally not managed by this class. In the Wood-based setup, identity and
 * cross-file linking are handled by providers, for example by combining a provider synonym with an object id such as
 * {@code save::123456}. {@code JsonDefinitions} only supplies the hierarchical location in which such referenced
 * objects belong.
 * </p>
 *
 * <p>
 * This means the class is not an id registry and not a link resolver. It is a model-side description of definition
 * areas that can be used by serializers and editors to decide where provider-identified objects should be placed.
 * </p>
 *
 * <p>
 * A definition scope can contain multiple {@link JsonType}s as well as nested child {@code JsonDefinitions}. The stored
 * types describe which kinds of objects may appear in this definition area. The concrete instances and their ids are
 * handled outside this class by the surrounding serialization and provider infrastructure.
 * </p>
 *
 * <p>
 * Typical responsibilities include:
 * </p>
 * <ul>
 * <li>Defining a structural root or subtree for extracted object definitions</li>
 * <li>Grouping definition areas hierarchically</li>
 * <li>Associating definition areas with allowed JSON types</li>
 * <li>Providing a lightweight descriptor view for editor and tooling support</li>
 * </ul>
 *
 * <p>
 * In short:
 * </p>
 * <ul>
 * <li>{@link JsonType} describes what kind of object may appear here</li>
 * <li>the provider system determines the identity of the referenced object</li>
 * <li>{@code JsonDefinitions} determines where that object is stored</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class JsonDefinitions {

    private final String name;
    private JsonDefinitions parent;
    private final List<JsonType> types;
    private final List<JsonDefinitions> children;

    /**
     * Constructs a definition scope with the specified name.
     *
     * @param name the name of this definitions scope.
     */
    public JsonDefinitions(String name) {
        this.name = name;
        this.types = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    /**
     * Returns the name of this definitions scope.
     *
     * @return the scope name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent definitions scope.
     *
     * @return the parent scope, or {@code null} if this is the root.
     */
    public JsonDefinitions getParent() {
        return parent;
    }

    /**
     * Returns whether this definitions scope has a parent.
     *
     * @return {@code true} if a parent exists, otherwise {@code false}.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns whether this definitions scope contains local type entries.
     *
     * @return {@code true} if local types exist.
     */
    public boolean hasTypes() {
        return !types.isEmpty();
    }

    /**
     * Returns whether this definitions scope contains child scopes.
     *
     * @return {@code true} if child scopes exist.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Returns whether this definitions scope is empty.
     *
     * @return {@code true} if it contains neither local types nor child scopes.
     */
    public boolean isEmpty() {
        return types.isEmpty() && children.isEmpty();
    }

    /**
     * Returns the number of locally registered types.
     *
     * @return the number of local types.
     */
    public int size() {
        return types.size();
    }

    /**
     * Returns the number of direct child scopes.
     *
     * @return the number of children.
     */
    public int childCount() {
        return children.size();
    }

    /**
     * Returns the nesting depth of this scope.
     *
     * @return {@code 0} if this is the root scope.
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
     * Returns the root definitions scope of this tree.
     *
     * @return the root scope.
     */
    public JsonDefinitions getRoot() {
        JsonDefinitions current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    /**
     * Checks whether the specified type is contained locally.
     *
     * @param type the type to check.
     * @return {@code true} if the type is contained locally.
     */
    public boolean containsLocalType(JsonType type) {
        return type != null && types.contains(type);
    }

    /**
     * Checks whether a local type with the specified canonical name exists.
     *
     * @param cName the canonical type name.
     * @return {@code true} if a matching local type exists.
     */
    public boolean containsLocalType(String cName) {
        return getLocalType(cName) != null;
    }

    /**
     * Checks whether a direct child with the specified name exists.
     *
     * @param childName the child scope name.
     * @return {@code true} if such a child exists.
     */
    public boolean containsChild(String childName) {
        return getChild(childName) != null;
    }

    /**
     * Checks whether the specified definitions scope is a direct child.
     *
     * @param child the child scope to check.
     * @return {@code true} if it is a direct child.
     */
    public boolean containsChild(JsonDefinitions child) {
        return children.contains(child);
    }

    /**
     * Returns the local type with the specified canonical name.
     *
     * @param cName the canonical type name.
     * @return the matching local type, or {@code null} if not found.
     */
    public JsonType getLocalType(String cName) {
        if (cName == null) {
            return null;
        }
        for (JsonType next : types) {
            if (cName.equals(next.getcName())) {
                return next;
            }
        }
        return null;
    }

    /**
     * Returns the direct child scope with the specified name.
     *
     * @param childName the child scope name.
     * @return the child scope, or {@code null} if not found.
     */
    public JsonDefinitions getChild(String childName) {
        if (childName == null) {
            return null;
        }
        for (JsonDefinitions next : children) {
            if (childName.equals(next.getName())) {
                return next;
            }
        }
        return null;
    }

    /**
     * Finds a type recursively in this definitions subtree.
     *
     * @param cName the canonical type name.
     * @return the first matching type, or {@code null} if not found.
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
     * Finds a definitions scope recursively in this subtree.
     *
     * @param definitionsName the scope name.
     * @return the matching scope, or {@code null} if not found.
     */
    public JsonDefinitions findDefinitions(String definitionsName) {
        if (name.equals(definitionsName)) {
            return this;
        }
        for (JsonDefinitions child : children) {
            JsonDefinitions nested = child.findDefinitions(definitionsName);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Checks whether this scope is an ancestor of the specified candidate.
     *
     * @param candidate the possible descendant.
     * @return {@code true} if this scope is an ancestor of the candidate.
     */
    public boolean isAncestorOf(JsonDefinitions candidate) {
        if (candidate == null) {
            return false;
        }
        JsonDefinitions current = candidate.parent;
        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    /**
     * Adds a local type to this definitions scope.
     *
     * @param type the type to add.
     * @return true if success.
     */
    public boolean addType(JsonType type) {
        return types.add(type);
    }

    /**
     * Adds a local type if no local type with the same canonical name exists yet.
     *
     * @param type the type to add.
     * @return true if success.
     */
    public boolean addTypeIfAbsent(JsonType type) {
        if (type != null && !containsLocalType(type.getcName())) {
            return types.add(type);
        }
        return false;
    }

    /**
     * Adds multiple local types.
     *
     * @param types the types to add.
     */
    public void addAllTypes(Iterable<JsonType> types) {
        if (types == null) {
            return;
        }
        for (JsonType type : types) {
            addType(type);
        }
    }

    /**
     * Adds a child definitions scope.
     *
     * @param child the child scope to add.
     * @return true if success.
     * @throws IllegalArgumentException if adding the child would create a cycle.
     */
    public boolean addChild(JsonDefinitions child) {
        if (child == null) {
            return false;
        }
        if (child == this) {
            throw new IllegalArgumentException("A JsonDefinitions cannot contain itself as child.");
        }
        if (child.isAncestorOf(this)) {
            throw new IllegalArgumentException("Adding this child would create a circular definitions hierarchy.");
        }
        if (child.parent != null && child.parent != this) {
            throw new IllegalArgumentException("The child JsonDefinitions already has another parent.");
        }
        if (!children.contains(child)) {
            child.parent = this;
            return children.add(child);
        }
        return false;
    }

    /**
     * Adds a child definitions scope only if a direct child with the same name is absent.
     *
     * @param child the child scope to add.
     * @return true if success.
     */
    public boolean addChildIfAbsent(JsonDefinitions child) {
        if (child != null && !containsChild(child.getName())) {
            return addChild(child);
        }
        return false;
    }

    /**
     * Removes the specified local type.
     *
     * @param type the type to remove.
     * @return {@code true} if the type was removed.
     */
    public boolean removeType(JsonType type) {
        if (type == null) {
            return false;
        }
        return types.remove(type);
    }

    /**
     * Removes the first local type with the specified canonical name.
     *
     * @param cName the canonical type name.
     * @return the removed type, or {@code null} if not found.
     */
    public JsonType removeType(String cName) {
        if (cName == null) {
            return null;
        }
        for (int i = 0; i < types.size(); i++) {
            JsonType next = types.get(i);
            if (cName.equals(next.getcName())) {
                return types.remove(i);
            }
        }
        return null;
    }

    /**
     * Removes the specified direct child scope.
     *
     * @param child the child scope to remove.
     * @return {@code true} if the child was removed.
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
     * Removes the direct child scope with the specified name.
     *
     * @param childName the child scope name.
     * @return the removed child scope, or {@code null} if not found.
     */
    public JsonDefinitions removeChild(String childName) {
        if (childName == null) {
            return null;
        }
        for (int i = 0; i < children.size(); i++) {
            JsonDefinitions next = children.get(i);
            if (childName.equals(next.getName())) {
                children.remove(i);
                next.parent = null;
                return next;
            }
        }
        return null;
    }

    /**
     * Removes all local types from this scope.
     */
    public void clearTypes() {
        types.clear();
    }

    /**
     * Removes all direct child scopes from this scope.
     */
    public void clearChildren() {
        for (JsonDefinitions child : children) {
            child.parent = null;
        }
        children.clear();
    }

    /**
     * Clears local types and child scopes from this definitions scope.
     */
    public void clear() {
        clearTypes();
        clearChildren();
    }

    /**
     * Returns an unmodifiable view of the local types.
     *
     * @return the local types.
     */
    public List<JsonType> getTypes() {
        return Collections.unmodifiableList(types);
    }

    /**
     * Returns an iterator over the local types.
     *
     * @return an iterator over local types.
     */
    public Iterator<JsonType> typesIterator() {
        return types.iterator();
    }

    /**
     * Returns an unmodifiable view of the direct child scopes.
     *
     * @return the direct child scopes.
     */
    public List<JsonDefinitions> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns an iterator over the direct child scopes.
     *
     * @return an iterator over direct child scopes.
     */
    public Iterator<JsonDefinitions> childrenIterator() {
        return children.iterator();
    }

    /**
     * Returns all types in this subtree as an unmodifiable list.
     *
     * @return all local and nested types.
     */
    public List<JsonType> getTypesDeep() {
        List<JsonType> ret = new ArrayList<>();
        collectTypesDeep(ret);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns an iterator over all types in this subtree.
     *
     * @return an iterator over all local and nested types.
     */
    public Iterator<JsonType> typesDeepIterator() {
        return getTypesDeep().iterator();
    }

    /**
     * Returns all child scopes in this subtree as an unmodifiable list.
     *
     * @return all nested child scopes.
     */
    public List<JsonDefinitions> getChildrenDeep() {
        List<JsonDefinitions> ret = new ArrayList<>();
        collectChildrenDeep(ret);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns an iterator over all child scopes in this subtree.
     *
     * @return an iterator over all nested child scopes.
     */
    public Iterator<JsonDefinitions> childrenDeepIterator() {
        return getChildrenDeep().iterator();
    }

    private void collectTypesDeep(List<JsonType> target) {
        target.addAll(types);
        for (JsonDefinitions child : children) {
            child.collectTypesDeep(target);
        }
    }

    private void collectChildrenDeep(List<JsonDefinitions> target) {
        for (JsonDefinitions child : children) {
            target.add(child);
            child.collectChildrenDeep(target);
        }
    }

    /**
     * Creates a lightweight descriptor of this definitions scope.
     * <p>
     * The descriptor contains the scope hierarchy and the canonical names of the locally registered types. Full type
     * metadata remains available through the surrounding model descriptor.
     * </p>
     *
     * @return the descriptor representation of this definitions scope.
     */
    public JsonDefinitionsDescriptor describe() {
        JsonDefinitionsDescriptor ret = new JsonDefinitionsDescriptor(name);
        for (JsonType type : types) {
            if (type != null) {
                ret.addType(type.getcName());
            }
        }
        for (JsonDefinitions child : children) {
            if (child != null) {
                ret.addChild(child.describe());
            }
        }
        return ret;
    }
}
