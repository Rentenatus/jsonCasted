/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.model.item.JsonClass;

/**
 * Repository model that extends JsonModel and implements JsonRepoEntity.
 * Only classes that represent JsonRepoEntity implementations can be added to this model.
 *
 * @author Janusch Rentenatus
 */
public class JsonRepoModel extends JsonModel implements JsonRepoEntity {

    public JsonRepoModel(String mName) {
        super(mName);
    }

    /**
     * Masks the addClass method from JsonModel.
     * Only JsonClass instances whose underlying class implements JsonRepoEntity are allowed.
     *
     * @param jClass The JsonClass to add to the repository model.
     * @throws IllegalArgumentException If the class does not implement JsonRepoEntity.
     */
    @Override
    public void addClass(JsonClass jClass) {
        if (jClass != null) {
            String className = jClass.getcName();
            try {
                Class<?> clazz = Class.forName(className);
                if (!JsonRepoEntity.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException(
                        "Only classes implementing JsonRepoEntity are allowed in repository model. " +
                        "Class '" + className + "' does not implement JsonRepoEntity.");
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(
                    "Cannot verify class for repository model: " + className, e);
            }
        }
        super.addClass(jClass);
    }

}
