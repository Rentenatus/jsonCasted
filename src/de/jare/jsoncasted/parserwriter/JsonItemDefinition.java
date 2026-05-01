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
 * Interface for JSON item definitions that provide model and casting configuration.
 *
 * <p>Implementations of this interface define how JSON structures should be parsed
 * and cast to Java objects. It provides access to:</p>
 * <ul>
 *   <li>The JSON model containing type definitions</li>
 *   <li>The casting level for type resolution</li>
 *   <li>The model descriptor for introspection</li>
 * </ul>
 *
 * <p>This is typically implemented by model definition classes like
 * {@code ImplTestDefinition} in the test package.</p>
 *
 * @author Janusch Rentenatus
 */
public interface JsonItemDefinition {

    /**
     * Returns the JSON model for this definition.
     *
     * @return the JsonModel containing type definitions.
     */
    public JsonModel getModel();

    /**
     * Returns the model descriptor for introspection.
     *
     * @return the JsonModelDescriptor describing all types in the model.
     */
    default JsonModelDescriptor getDescriptor() {
        return getModel().getOrCreateDescriptor();
    }

    /**
     * Returns the casting level for this definition.
     *
     * @return the JsonCastingLevel determining when casting is performed.
     */
    public JsonCastingLevel getCastingLevel();

}
