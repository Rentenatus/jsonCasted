/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.model.item;

import java.util.List;

/**
 * Marks objects that carry annotation declarations whose values the writer emits as {@code @} keys (annotation
 * concept, decision 13): the description file form of a declaration value is the @ key on the carrier - the type
 * entry and the field entry of a description - never a structured attribute of the annotations list. The parse side
 * mirrors it: the raw adoption reads the @ keys back onto these declarations.
 *
 * @author Janusch Rentenatus
 */
public interface JsonAnnotationCarrier {

    /**
     * Returns the annotation declarations carried by this object, valued or value free.
     *
     * @return the annotation declarations, never null
     */
    List<JsonAnnotation> getAnnotations();

    /**
     * Adds an annotation declaration carried by this object.
     *
     * @param annotation the annotation declaration to add
     */
    void addAnnotation(JsonAnnotation annotation);

    /**
     * Returns the declaration with the given annotation name.
     *
     * @param name the annotation name without the {@code @} prefix
     * @return the declaration, or {@code null} when none is carried under that name
     */
    JsonAnnotation getAnnotation(String name);
}
