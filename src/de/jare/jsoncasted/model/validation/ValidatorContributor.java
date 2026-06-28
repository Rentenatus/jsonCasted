/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

/**
 * Interface for contributors that register validators with a ValidatorRegistry.
 * Contributors should only provide validators and not perform traversal themselves.
 * 
 * @author Janusch Rentenatus
 */
public interface ValidatorContributor {

    /**
     * Contributes validators to the registry.
     * This method is called by the JsonModelValidator to collect all validators.
     * 
     * @param registry the registry to add validators to
     */
    default void contribute(ValidatorRegistry registry) {
        // Default implementation does nothing
        // Contributors should override this method to register their validators
    }
}
