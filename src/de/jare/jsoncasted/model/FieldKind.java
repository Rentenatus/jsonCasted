/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration representing kinds of fields in the model.
 *
 * <p>
 * Each {@code FieldKind} constant defines how a field behaves in the model:
 * </p>
 * <ul>
 * <li>{@link #ATTRIBUTE} – simple value field, not referencing objects</li>
 * <li>{@link #CONTAINMENT} – ownership/containment field, child is part of
 * parent tree</li>
 * <li>{@link #REFERENCE} – reference field, pointing to another object via
 * ID/link</li>
 * </ul>
 *
 * <p>
 * Utility methods are provided to retrieve an enumerator by literal string, by
 * name, or by integer value. A public unmodifiable list of all values is also
 * available via {@link #VALUES}.
 * </p>
 */
public enum FieldKind {

    /**
     * Simple value field, not referencing objects.
     */
    ATTRIBUTE(0, "Attribute", "ATTRIBUTE", false, false),
    /**
     * Ownership/containment field, child is part of parent tree.
     */
    CONTAINMENT(1, "Containment", "CONTAINMENT", true, false),
    /**
     * Reference field, pointing to another object via ID/link.
     */
    REFERENCE(2, "Reference", "REFERENCE", false, true);

    // --- Integer values for each literal ---
    public static final int ATTRIBUTE_VALUE = 0;
    public static final int CONTAINMENT_VALUE = 1;
    public static final int REFERENCE_VALUE = 2;

    /**
     * Internal array of all enumerators.
     */
    private static final FieldKind[] VALUES_ARRAY = new FieldKind[]{
        ATTRIBUTE, CONTAINMENT, REFERENCE
    };

    /**
     * Public unmodifiable list of all enumerators.
     */
    public static final List<FieldKind> VALUES
            = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

    /**
     * Returns the enumerator with the specified literal string.
     *
     * @param literal the literal string
     * @return matching enumerator, or {@code null} if none found
     */
    public static FieldKind get(String literal) {
        for (FieldKind result : VALUES_ARRAY) {
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the enumerator with the specified name.
     *
     * Used by JsonEnumByNameBuilder.
     *
     * @param name the name
     * @return matching enumerator, or {@code null} if none found
     */
    public static FieldKind getByName(String name) {
        for (FieldKind result : VALUES_ARRAY) {
            if (result.getName().equals(name)) {
                return result;
            }
            if (result.getLiteral().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the enumerator with the specified integer value.
     *
     * @param value the integer value
     * @return matching enumerator, or {@code null} if none found
     */
    public static FieldKind get(int value) {
        switch (value) {
            case ATTRIBUTE_VALUE:
                return ATTRIBUTE;
            case CONTAINMENT_VALUE:
                return CONTAINMENT;
            case REFERENCE_VALUE:
                return REFERENCE;
            default:
                return null;
        }
    }

    // --- Internal fields ---
    private final int value;
    private final String name;
    private final String literal;
    private final boolean owned;
    private final boolean externalReference;

    /**
     * Private constructor for enum constants.
     */
    private FieldKind(
            int value,
            String name,
            String literal,
            boolean owned,
            boolean externalReference
    ) {
        this.value = value;
        this.name = name;
        this.literal = literal;
        this.owned = owned;
        this.externalReference = externalReference;
    }

    /**
     * @return integer value of the enumerator
     */
    public int getValue() {
        return value;
    }

    /**
     * @return name of the enumerator
     */
    public String getName() {
        return name;
    }

    /**
     * @return literal string of the enumerator
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * @return true if this kind represents an owned/containment relationship.
     */
    public boolean isOwned() {
        return owned;
    }

    /**
     * @return true if this kind represents an external reference (not owned).
     */
    public boolean isExternalReference() {
        return externalReference;
    }

    /**
     * Returns the literal string representation of the enumerator.
     *
     * @return literal string
     */
    @Override
    public String toString() {
        return literal;
    }

} // FieldKind
