package de.jare.jsoncasted.model.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Descriptor for a hierarchical repository of JSON definitions.
 * <p>
 * A {@code JsonDefinitionsDescriptor} describes a definition scope that can contain multiple type names and nested
 * child definition scopes. It is a lightweight structural descriptor used for representing definition trees collected
 * during serialization.
 * </p>
 *
 * <p>
 * Unlike {@link JsonModelDescriptor}, this class does not duplicate full {@link JsonTypeDescriptor} instances. It only
 * stores canonical type names ({@code cName}) because the corresponding type descriptors are already known by the
 * surrounding model descriptor.
 * </p>
 *
 * <p>
 * Typical responsibilities include:
 * </p>
 * <ul>
 * <li>Holding locally defined type names</li>
 * <li>Organizing nested child definition scopes</li>
 * <li>Providing immutable views and traversal helpers</li>
 * <li>Validating descriptor tree consistency</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public class JsonDefinitionsDescriptor {

    private final String name;
    private JsonDefinitionsDescriptor parent;
    private final Map<String, String> describedTypes;
    private final List<JsonDefinitionsDescriptor> children;

    /**
     * Constructs a descriptor with the specified name.
     *
     * @param name the name of this definitions scope, must not be null.
     */
    public JsonDefinitionsDescriptor(String name) {
        this.name = Objects.requireNonNull(name, "name");
        this.describedTypes = new LinkedHashMap<>();
        this.children = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Base data
    // -------------------------------------------------------------------------
    /**
     * Returns the name of this definitions descriptor.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent descriptor.
     *
     * @return the parent, or {@code null} if this is a root descriptor.
     */
    public JsonDefinitionsDescriptor getParent() {
        return parent;
    }

    /**
     * Returns whether this descriptor has a parent.
     *
     * @return {@code true} if a parent exists.
     */
    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns whether this descriptor contains locally registered type names.
     *
     * @return {@code true} if local type names exist.
     */
    public boolean hasTypes() {
        return !describedTypes.isEmpty();
    }

    /**
     * Returns whether this descriptor contains child scopes.
     *
     * @return {@code true} if children exist.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Returns whether this descriptor is empty.
     *
     * @return {@code true} if no local types and no children are present.
     */
    public boolean isEmpty() {
        return describedTypes.isEmpty() && children.isEmpty();
    }

    /**
     * Returns the number of local type names.
     *
     * @return the number of local type names.
     */
    public int size() {
        return describedTypes.size();
    }

    /**
     * Returns the number of direct child descriptors.
     *
     * @return the number of direct children.
     */
    public int childCount() {
        return children.size();
    }

    /**
     * Returns the depth in the descriptor tree.
     *
     * @return {@code 0} for a root descriptor.
     */
    public int getDepth() {
        int ret = 0;
        JsonDefinitionsDescriptor current = parent;
        while (current != null) {
            ret++;
            current = current.parent;
        }
        return ret;
    }

    /**
     * Returns the root descriptor of this tree.
     *
     * @return the root descriptor.
     */
    public JsonDefinitionsDescriptor getRoot() {
        JsonDefinitionsDescriptor current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    // -------------------------------------------------------------------------
    // Query / Lookup
    // -------------------------------------------------------------------------
    /**
     * Checks whether a local type name with the specified canonical name exists.
     *
     * @param cName the canonical type name.
     * @return {@code true} if the type name is contained locally.
     */
    public boolean containsType(String cName) {
        return cName != null && describedTypes.containsKey(cName);
    }

    /**
     * Checks whether a direct child with the specified name exists.
     *
     * @param childName the child name.
     * @return {@code true} if the child exists.
     */
    public boolean containsChild(String childName) {
        return getChild(childName) != null;
    }

    /**
     * Checks whether the specified descriptor is a direct child.
     *
     * @param child the child descriptor.
     * @return {@code true} if the child is contained directly.
     */
    public boolean containsChild(JsonDefinitionsDescriptor child) {
        return children.contains(child);
    }

    /**
     * Returns the locally registered canonical type name.
     *
     * @param cName the canonical type name.
     * @return the canonical type name, or {@code null} if not found.
     */
    public String getType(String cName) {
        if (cName == null) {
            return null;
        }
        return describedTypes.get(cName);
    }

    /**
     * Returns the locally registered canonical type name.
     *
     * @param cName the canonical type name.
     * @return the canonical type name.
     * @throws IllegalStateException if the type name is not found.
     */
    public String requireType(String cName) {
        String ret = describedTypes.get(cName);
        if (ret == null) {
            throw new IllegalStateException("Unknown definitions descriptor type: " + cName);
        }
        return ret;
    }

    /**
     * Returns the direct child descriptor with the specified name.
     *
     * @param childName the child name.
     * @return the child descriptor, or {@code null} if not found.
     */
    public JsonDefinitionsDescriptor getChild(String childName) {
        if (childName == null) {
            return null;
        }
        for (JsonDefinitionsDescriptor next : children) {
            if (childName.equals(next.getName())) {
                return next;
            }
        }
        return null;
    }

    /**
     * Finds a type name recursively in this subtree.
     *
     * @param cName the canonical type name.
     * @return the first matching canonical type name, or {@code null} if not found.
     */
    public String findType(String cName) {
        String local = getType(cName);
        if (local != null) {
            return local;
        }
        for (JsonDefinitionsDescriptor child : children) {
            String nested = child.findType(cName);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Finds a definitions descriptor recursively in this subtree.
     *
     * @param definitionsName the descriptor name.
     * @return the matching descriptor, or {@code null} if not found.
     */
    public JsonDefinitionsDescriptor findDefinitions(String definitionsName) {
        if (name.equals(definitionsName)) {
            return this;
        }
        for (JsonDefinitionsDescriptor child : children) {
            JsonDefinitionsDescriptor nested = child.findDefinitions(definitionsName);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    /**
     * Checks whether this descriptor is an ancestor of the specified candidate.
     *
     * @param candidate the possible descendant.
     * @return {@code true} if this descriptor is an ancestor.
     */
    public boolean isAncestorOf(JsonDefinitionsDescriptor candidate) {
        if (candidate == null) {
            return false;
        }
        JsonDefinitionsDescriptor current = candidate.parent;
        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current.parent;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------
    /**
     * Adds a local canonical type name.
     *
     * @param cName the canonical type name to add.
     * @return this descriptor.
     */
    public JsonDefinitionsDescriptor addType(String cName) {
        Objects.requireNonNull(cName, "cName");
        describedTypes.putIfAbsent(cName, cName);
        return this;
    }

    /**
     * Adds a local canonical type name if absent.
     *
     * @param cName the canonical type name to add.
     * @return {@code true} if the type name was added.
     */
    public boolean addTypeIfAbsent(String cName) {
        Objects.requireNonNull(cName, "cName");
        return describedTypes.putIfAbsent(cName, cName) == null;
    }

    /**
     * Adds multiple local canonical type names.
     *
     * @param cNames the canonical type names to add.
     * @return this descriptor.
     */
    public JsonDefinitionsDescriptor addAllTypes(Collection<String> cNames) {
        Objects.requireNonNull(cNames, "cNames");
        for (String cName : cNames) {
            addType(cName);
        }
        return this;
    }

    /**
     * Adds a child descriptor.
     *
     * @param child the child descriptor to add.
     * @return this descriptor.
     * @throws IllegalArgumentException if a cycle would be created.
     */
    public JsonDefinitionsDescriptor addChild(JsonDefinitionsDescriptor child) {
        Objects.requireNonNull(child, "child");
        if (child == this) {
            throw new IllegalArgumentException("A JsonDefinitionsDescriptor cannot contain itself as child.");
        }
        if (child.isAncestorOf(this)) {
            throw new IllegalArgumentException("Adding this child would create a circular definitions hierarchy.");
        }
        if (child.parent != null && child.parent != this) {
            throw new IllegalArgumentException("The child JsonDefinitionsDescriptor already has another parent.");
        }
        if (!children.contains(child)) {
            child.parent = this;
            children.add(child);
        }
        return this;
    }

    /**
     * Adds a child descriptor only if a direct child with the same name is absent.
     *
     * @param child the child descriptor to add.
     * @return this descriptor.
     */
    public JsonDefinitionsDescriptor addChildIfAbsent(JsonDefinitionsDescriptor child) {
        Objects.requireNonNull(child, "child");
        if (!containsChild(child.getName())) {
            addChild(child);
        }
        return this;
    }

    // -------------------------------------------------------------------------
    // Remove / Clear
    // -------------------------------------------------------------------------
    /**
     * Removes the specified local canonical type name.
     *
     * @param cName the canonical type name to remove.
     * @return the removed canonical type name, or {@code null} if not found.
     */
    public String removeType(String cName) {
        if (cName == null) {
            return null;
        }
        return describedTypes.remove(cName);
    }

    /**
     * Removes the specified direct child descriptor.
     *
     * @param child the child to remove.
     * @return {@code true} if removed.
     */
    public boolean removeChild(JsonDefinitionsDescriptor child) {
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
     * Removes the direct child descriptor with the specified name.
     *
     * @param childName the child name.
     * @return the removed descriptor, or {@code null} if not found.
     */
    public JsonDefinitionsDescriptor removeChild(String childName) {
        if (childName == null) {
            return null;
        }
        for (int i = 0; i < children.size(); i++) {
            JsonDefinitionsDescriptor next = children.get(i);
            if (childName.equals(next.getName())) {
                children.remove(i);
                next.parent = null;
                return next;
            }
        }
        return null;
    }

    /**
     * Removes all local type names.
     */
    public void clearTypes() {
        describedTypes.clear();
    }

    /**
     * Removes all direct child descriptors.
     */
    public void clearChildren() {
        for (JsonDefinitionsDescriptor child : children) {
            child.parent = null;
        }
        children.clear();
    }

    /**
     * Clears local type names and children.
     */
    public void clear() {
        clearTypes();
        clearChildren();
    }

    // -------------------------------------------------------------------------
    // Views
    // -------------------------------------------------------------------------
    /**
     * Returns an unmodifiable map of local canonical type names keyed by canonical name.
     *
     * @return the local type name map.
     */
    public Map<String, String> getTypes() {
        return Collections.unmodifiableMap(describedTypes);
    }

    /**
     * Returns an unmodifiable set of local canonical type names.
     *
     * @return the local canonical type names.
     */
    public Set<String> typeNames() {
        return Collections.unmodifiableSet(describedTypes.keySet());
    }

    /**
     * Returns an unmodifiable list of direct child descriptors.
     *
     * @return the direct child descriptors.
     */
    public List<JsonDefinitionsDescriptor> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns all canonical type names in this subtree as an unmodifiable list.
     *
     * @return all nested canonical type names.
     */
    public List<String> getTypesDeep() {
        List<String> ret = new ArrayList<>();
        collectTypesDeep(ret);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns all child descriptors in this subtree as an unmodifiable list.
     *
     * @return all nested child descriptors.
     */
    public List<JsonDefinitionsDescriptor> getChildrenDeep() {
        List<JsonDefinitionsDescriptor> ret = new ArrayList<>();
        collectChildrenDeep(ret);
        return Collections.unmodifiableList(ret);
    }

    private void collectTypesDeep(List<String> target) {
        target.addAll(describedTypes.keySet());
        for (JsonDefinitionsDescriptor child : children) {
            child.collectTypesDeep(target);
        }
    }

    private void collectChildrenDeep(List<JsonDefinitionsDescriptor> target) {
        for (JsonDefinitionsDescriptor child : children) {
            target.add(child);
            child.collectChildrenDeep(target);
        }
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------
    /**
     * Validates this descriptor and its subtree.
     *
     * @throws IllegalStateException if validation fails.
     */
    public void validate() {
        if (name.isBlank()) {
            throw new IllegalStateException("definitions name is blank");
        }

        for (Map.Entry<String, String> entry : describedTypes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("Blank type key in definitions " + name);
            }
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Blank type value in definitions " + name);
            }
            if (!key.equals(value)) {
                throw new IllegalStateException("Type key/value mismatch in definitions " + name + ": " + key + " != " + value);
            }
        }

        for (JsonDefinitionsDescriptor child : children) {
            if (child == null) {
                throw new IllegalStateException("Child definitions descriptor is null in " + name);
            }
            if (child.parent != this) {
                throw new IllegalStateException("Child parent mismatch for " + child.getName());
            }
            child.validate();
        }
    }

    @Override
    public String toString() {
        return "JsonDefinitionsDescriptor{name=" + name
                + ", types=" + describedTypes.size()
                + ", children=" + children.size() + "}";
    }
}
