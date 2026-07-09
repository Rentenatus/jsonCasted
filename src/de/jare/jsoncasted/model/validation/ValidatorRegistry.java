/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry that collects and organizes validators by their target element type. Validators are registered by
 * ValidatorContributors and used by JsonModelValidator.
 *
 * @author Janusch Rentenatus
 */
public class ValidatorRegistry {

    private final List<ModelValidator> modelValidators = new ArrayList<>();
    private final List<TypeValidator> typeValidators = new ArrayList<>();
    private final List<FieldValidator> fieldValidators = new ArrayList<>();
    private final List<DefinitionsValidator> definitionsValidators = new ArrayList<>();

    /**
     * Adds a model validator to the registry.
     *
     * @param validator the model validator to add
     * @return this registry for method chaining
     */
    public ValidatorRegistry addModelValidator(ModelValidator validator) {
        modelValidators.add(validator);
        return this;
    }

    /**
     * Adds a type validator to the registry.
     *
     * @param validator the type validator to add
     * @return this registry for method chaining
     */
    public ValidatorRegistry addTypeValidator(TypeValidator validator) {
        typeValidators.add(validator);
        return this;
    }

    /**
     * Adds a field validator to the registry.
     *
     * @param validator the field validator to add
     * @return this registry for method chaining
     */
    public ValidatorRegistry addFieldValidator(FieldValidator validator) {
        fieldValidators.add(validator);
        return this;
    }

    /**
     * Adds a definitions validator to the registry.
     *
     * @param validator the definitions validator to add
     * @return this registry for method chaining
     */
    public ValidatorRegistry addDefinitionsValidator(DefinitionsValidator validator) {
        definitionsValidators.add(validator);
        return this;
    }

    /**
     * Returns an unmodifiable list of all registered model validators.
     *
     * @return the list of model validators
     */
    public List<ModelValidator> getModelValidators() {
        return Collections.unmodifiableList(modelValidators);
    }

    /**
     * Returns an unmodifiable list of all registered type validators.
     *
     * @return the list of type validators
     */
    public List<TypeValidator> getTypeValidators() {
        return Collections.unmodifiableList(typeValidators);
    }

    /**
     * Returns an unmodifiable list of all registered field validators.
     *
     * @return the list of field validators
     */
    public List<FieldValidator> getFieldValidators() {
        return Collections.unmodifiableList(fieldValidators);
    }

    /**
     * Returns an unmodifiable list of all registered definitions validators.
     *
     * @return the list of definitions validators
     */
    public List<DefinitionsValidator> getDefinitionsValidators() {
        return Collections.unmodifiableList(definitionsValidators);
    }

    /**
     * Returns the total count of all registered validators.
     *
     * @return the total count
     */
    public int getTotalValidatorCount() {
        return modelValidators.size() + typeValidators.size()
                + fieldValidators.size() + definitionsValidators.size();
    }

    /**
     * Clears all registered validators.
     */
    public void clear() {
        modelValidators.clear();
        typeValidators.clear();
        fieldValidators.clear();
        definitionsValidators.clear();
    }
}
