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
 * An annotation is implicitly defined as an array of strings. Every annotation is persistent - its values are
 * written back on save.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonAnnotation {

    /**
     * Name of the editString annotation: a display template for objects of the described type (see the annotation
     * concept). Its value is a string array with exactly one element, the template string.
     */
    public static final String EDIT_STRING = "editString";

    /**
     * Name of the doc annotation (annotation concept): free documentation of the carrier it annotates.
     */
    public static final String DOC = "doc";

    /**
     * Name of the doc wildcard declaration: {@code doc:*} covers every field and the object level of the type.
     */
    public static final String DOC_ALL = "doc:*";

    private final String name;

    /**
     * The declaration value of this annotation (see the annotation concept, decision 13): type knowledge that lives
     * in the model itself, carried on the declaration instead of a modeling. {@code null} for value free
     * declarations - instance values (@ keys of the modelings) are unaffected by this slot.
     */
    private String value;

    /**
     * Constructs an annotation.
     *
     * @param name The name of the annotation without the {@code @} prefix.
     */
    public JsonAnnotation(String name) {
        this.name = name;
    }

    /**
     * Creates an editString declaration with its display template (annotation concept, decision 13): the template
     * tells the display side how instances of the type are shown, e.g. {@code ${profile}}.
     *
     * @param template the display template
     * @return the persistent editString annotation with the template as declaration value
     */
    public static JsonAnnotation editString(String template) {
        return new JsonAnnotation(EDIT_STRING).withValue(template);
    }

    /**
     * Creates a doc declaration with its documentation text - the type knowledge counterpart of an instance level
     * {@code @doc}: the description of the annotated type itself.
     *
     * @param text the documentation text
     * @return the persistent doc annotation with the text as declaration value
     */
    public static JsonAnnotation doc(String text) {
        return new JsonAnnotation(DOC).withValue(text);
    }

    /**
     * Creates the doc wildcard declaration {@code doc:*}: the annotated type is documentable everywhere (object
     * level and every field), without a per-field declaration.
     *
     * @return the persistent doc wildcard annotation
     */
    public static JsonAnnotation doc() {
        return new JsonAnnotation(DOC_ALL);
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
     * Returns the declaration value of this annotation, or {@code null} when the declaration is value free.
     *
     * @return the declaration value, or {@code null}
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the declaration value of this annotation.
     *
     * @param value the declaration value, or {@code null} to clear it
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Sets the declaration value and returns this annotation for chaining.
     *
     * @param value the declaration value, or {@code null} to clear it
     * @return this annotation
     */
    public JsonAnnotation withValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}
