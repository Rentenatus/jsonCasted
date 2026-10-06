/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

/**
 * A declared annotation of a model type or field. Annotations are features without a Java counterpart: the model
 * declares them by name (without the {@code @} prefix), the wood JSON carries the values under a key with the
 * {@code @} prefix.
 *
 * <p>
 * An annotation is implicitly defined as an array of strings. Transient annotations are session-local in the
 * editor and are skipped when the tree is saved back to JSON.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonAnnotation {

    private final String name;
    private boolean transientFlag;

    /**
     * Constructs a persistent annotation.
     *
     * @param name The name of the annotation without the {@code @} prefix.
     */
    public JsonAnnotation(String name) {
        this(name, false);
    }

    /**
     * Constructs an annotation.
     *
     * @param name The name of the annotation without the {@code @} prefix.
     * @param transientFlag {@code true} if the annotation is skipped on save, {@code false} otherwise.
     */
    public JsonAnnotation(String name, boolean transientFlag) {
        this.name = name;
        this.transientFlag = transientFlag;
    }

    /**
     * Returns the name of the annotation without the {@code @} prefix.
     *
     * @return The annotation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether the annotation is transient. Transient annotations are not written back on save.
     *
     * @return {@code true} if the annotation is transient, {@code false} otherwise.
     */
    public boolean isTransient() {
        return transientFlag;
    }

    /**
     * Sets the transient flag of the annotation.
     *
     * @param transientFlag {@code true} to skip the annotation on save, {@code false} otherwise.
     */
    public void setTransient(boolean transientFlag) {
        this.transientFlag = transientFlag;
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}
