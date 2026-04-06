/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserwriter;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;

/**
 *
 * @author Janusch Rentenatus
 */
public interface JsonItemDefinition {

    public JsonModel getModel();

    default JsonModelDescriptor getDescriptor() {
        return getModel().getOrCreateDescriptor();
    }

    public JsonCastingLevel getCastingLevel();

}
