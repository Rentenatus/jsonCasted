/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.diff;

/**
 * Semantic node kind used by the diff engine. Coarser than the representation-specific node types: it abstracts
 * {@code JsonNode} (parse layer) and {@code EditNodeAbstract} (edit layer) onto one common shape.
 *
 * <p>
 * PROPERTY exists for the edit layer, where a member and its value are separate nodes. The parse layer has no
 * PROPERTY nodes; object members are the keyed children of OBJECT nodes.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public enum NodeKind {

    /**
     * An object node: children are keyed members (member key on each child).
     */
    OBJECT,
    /**
     * A property node (edit layer only): the member itself, holding the value as child.
     */
    PROPERTY,
    /**
     * An array node: children are positional, their member key is {@code null}.
     */
    ARRAY,
    /**
     * A scalar leaf: string, number, boolean or null.
     */
    VALUE
}
