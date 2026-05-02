/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.model.builder.JsonBooleanBuilder;
import de.jare.jsoncasted.model.builder.JsonBooleanObjBuilder;
import de.jare.jsoncasted.model.builder.JsonDoubleBuilder;
import de.jare.jsoncasted.model.builder.JsonDoubleObjBuilder;
import de.jare.jsoncasted.model.builder.JsonFloatBuilder;
import de.jare.jsoncasted.model.builder.JsonFloatObjBuilder;
import de.jare.jsoncasted.model.builder.JsonIntBuilder;
import de.jare.jsoncasted.model.builder.JsonIntegerObjBuilder;
import de.jare.jsoncasted.model.builder.JsonLongBuilder;
import de.jare.jsoncasted.model.builder.JsonLongObjBuilder;
import de.jare.jsoncasted.model.builder.JsonStringBuilder;
import de.jare.jsoncasted.model.item.JsonClass;

/**
 * Repository model that extends JsonModel and implements JsonRepoEntity. Only
 * classes that represent JsonRepoEntity implementations can be added to this
 * model.
 *
 * <p>This model is used for managing types from external JSON resources, ensuring
 * that all registered classes implement the JsonRepoEntity interface for cross-resource
 * referencing support.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonRepoModel extends JsonModel implements JsonRepoEntity {

    /**
     * Constructs a JsonRepoModel with the specified model name.
     * Creates a repository model that can only contain JsonClass instances
     * whose underlying classes implement JsonRepoEntity.
     *
     * @param mName The name of the repository model.
     */
    public JsonRepoModel(String mName) {
        super(mName);
    }

    /**
     * Masks the addClass method from JsonModel. Only JsonClass instances whose
     * underlying class implements JsonRepoEntity are allowed.
     *
     * @param jClass The JsonClass to add to the repository model.
     * @throws IllegalArgumentException If the class does not implement
     * JsonRepoEntity.
     */
    @Override
    public void addClass(JsonClass jClass) {
        if (jClass != null) {
            String className = jClass.getcName();
            try {
                Class<?> clazz = Class.forName(className);
                if (!JsonRepoEntity.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException(
                            "Only classes implementing JsonRepoEntity are allowed in repository model. "
                            + "Class '" + className + "' does not implement JsonRepoEntity.");
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(
                        "Cannot verify class for repository model: " + className, e);
            }
        }
        super.addClass(jClass);
    }

    /**
     * Populates the model with basic data types used in JSON processing.
     * <p>This override adds the same basic types as {@link JsonModel#addBasicModel()}
     * but uses super.addClass() to bypass the JsonRepoEntity validation.</p>
     * <p>Registers primitive wrapper types (String, Integer, Long, Float, Double, Boolean)
     * and their primitive counterparts (int, long, float, double, boolean).</p>
     */
    @Override
    public void addBasicModel() {
        super.addClass(new JsonClass("String", JsonNodeType.STRING, new JsonStringBuilder()));
        super.addClass(new JsonClass("Integer", JsonNodeType.LONG, new JsonIntegerObjBuilder()));
        super.addClass(new JsonClass("Long", JsonNodeType.LONG, new JsonLongObjBuilder()));
        super.addClass(new JsonClass("Float", JsonNodeType.NUMBER, new JsonFloatObjBuilder()));
        super.addClass(new JsonClass("Double", JsonNodeType.NUMBER, new JsonDoubleObjBuilder()));
        super.addClass(new JsonClass("Boolean", JsonNodeType.BOOLEAN, new JsonBooleanObjBuilder()));
        super.addClass(new JsonClass("int", JsonNodeType.LONG, new JsonIntBuilder()));
        super.addClass(new JsonClass("long", JsonNodeType.LONG, new JsonLongBuilder()));
        super.addClass(new JsonClass("float", JsonNodeType.NUMBER, new JsonFloatBuilder()));
        super.addClass(new JsonClass("double", JsonNodeType.NUMBER, new JsonDoubleBuilder()));
        super.addClass(new JsonClass("boolean", JsonNodeType.BOOLEAN, new JsonBooleanBuilder()));
    }
}
