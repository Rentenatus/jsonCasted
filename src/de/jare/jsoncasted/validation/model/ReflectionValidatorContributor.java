/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.validation.model;

import de.jare.jsoncasted.validation.model.reflectdefault.CollectionTypeMatchValidator;
import de.jare.jsoncasted.validation.model.reflectdefault.ConstructorArityValidator;
import de.jare.jsoncasted.validation.model.reflectdefault.GetterSetterValidator;

/**
 * Contributor that registers reflection-based validation rules. These rules check that the Java side of a model
 * matches its declaration: getter and setter methods exist, constructors match the declared constructor parameters,
 * and declared collection types match the actual property types. All rules are based on the shared ReflectionService
 * and silently skip types whose Java class cannot be resolved.
 *
 * @author Janusch Rentenatus
 */
public class ReflectionValidatorContributor implements ValidatorContributor {

    @Override
    public void contribute(ValidatorRegistry registry) {
        registry.addTypeValidator(new GetterSetterValidator());
        registry.addTypeValidator(new ConstructorArityValidator());
        registry.addTypeValidator(new CollectionTypeMatchValidator());
    }
}
