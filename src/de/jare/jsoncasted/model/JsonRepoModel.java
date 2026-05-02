/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.model.item.JsonClass;

/**
 *
 *
 * @author Janusch Rentenatus
 */
public class JsonRepoModel extends JsonModel implements JsonRepoEntity {

    public JsonRepoModel(String mName) {
        super(mName);
    }

    /**
     * Maskiert die addClass-Methode von JsonModel.
     * Fügt eine JsonClass zum Repository-Modell hinzu.
     *
     * @param jClass Die hinzuzufügende JsonClass
     */
    @Override
    public void addClass(JsonClass jClass) {
        super.addClass(jClass);
    }

}
