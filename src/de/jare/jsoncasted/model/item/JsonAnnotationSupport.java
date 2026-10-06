/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Composition helper that carries declared annotations. Embedded by JsonClass, JsonInterface and JsonField; the
 * JsonType interface exposes the same API through default methods, so carriers without annotations (JsonMap,
 * JsonUnknown) stay empty without any extra code.
 *
 * @author Janusch Rentenatus
 */
public class JsonAnnotationSupport {

    private final List<JsonAnnotation> annotations = new ArrayList<>();

    /**
     * Declares a persistent annotation.
     *
     * @param name The annotation name without the {@code @} prefix.
     */
    public void addAnnotation(String name) {
        addAnnotation(new JsonAnnotation(name));
    }

    /**
     * Declares an annotation.
     *
     * @param name The annotation name without the {@code @} prefix.
     * @param transientFlag {@code true} if the annotation is skipped on save, {@code false} otherwise.
     */
    public void addAnnotation(String name, boolean transientFlag) {
        addAnnotation(new JsonAnnotation(name, transientFlag));
    }

    /**
     * Adds a declared annotation.
     *
     * @param annotation The annotation to add.
     */
    public void addAnnotation(JsonAnnotation annotation) {
        annotations.add(Objects.requireNonNull(annotation, "annotation"));
    }

    /**
     * Returns the declared annotation with the given name.
     *
     * @param name The annotation name without the {@code @} prefix.
     * @return The annotation, or {@code null} if none is declared under that name.
     */
    public JsonAnnotation getAnnotation(String name) {
        for (JsonAnnotation next : annotations) {
            if (next.getName().equals(name)) {
                return next;
            }
        }
        return null;
    }

    /**
     * Returns all declared annotations.
     *
     * @return An unmodifiable list of annotations, empty if none are declared.
     */
    public List<JsonAnnotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Checks whether any annotation is declared.
     *
     * @return {@code true} if at least one annotation is declared, {@code false} otherwise.
     */
    public boolean hasAnnotations() {
        return !annotations.isEmpty();
    }
}
