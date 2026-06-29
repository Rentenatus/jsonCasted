/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonModelTypeTraverser;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonDefinitions;
import de.jare.jsoncasted.model.item.JsonField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Central validator that traverses the entire JsonModel and executes all registered validators. This class performs a
 * single traversal of the model, applying appropriate validators to each element.
 *
 * <p>
 * The validation process works as follows:
 * </p>
 * <ol>
 * <li>Creates a ValidatorRegistry from all registered ValidatorContributors</li>
 * <li>Creates a ValidationContext with the model and result</li>
 * <li>Executes all ModelValidators on the root model</li>
 * <li>Traverses all types (JsonClass, JsonInter, etc.) and executes TypeValidators</li>
 * <li>For each type, traverses all fields and executes FieldValidators</li>
 * <li>Traverses the definitions tree and executes DefinitionsValidators</li>
 * </ol>
 *
 * @author Janusch Rentenatus
 */
public class ValidationRunner {

    private final List<ValidatorContributor> contributors = new ArrayList<>();

    /**
     * Creates a new JsonModelValidator with no contributors.
     */
    public ValidationRunner() {
        contributors.add(new CoreValidatorContributor());
    }

    /**
     * Adds a validator contributor to this validator.
     *
     * @param contributor the contributor to add
     * @return this validator for method chaining
     */
    public ValidationRunner addContributor(ValidatorContributor contributor) {
        contributors.add(contributor);
        return this;
    }

    /**
     * Returns an unmodifiable list of all registered contributors.
     *
     * @return the list of contributors
     */
    public List<ValidatorContributor> getContributors() {
        return java.util.Collections.unmodifiableList(contributors);
    }

    /**
     * Validates the given JsonModel by traversing all elements and executing registered validators.
     *
     * @param model the model to validate
     * @return the validation result containing all diagnostics
     */
    public ValidationResult validate(JsonModel model) {
        ValidatorRegistry registry = new ValidatorRegistry();

        // Collect all validators from contributors
        for (ValidatorContributor contributor : contributors) {
            contributor.contribute(registry);
        }

        ValidationResult result = new ValidationResult();
        ValidationContext context = new ValidationContext(model, result);

        // Execute model validators
        for (ModelValidator validator : registry.getModelValidators()) {
            validator.validate(model, context);
        }

        // Traverse and validate all types
        validateTypes(model, registry, context);

        // Traverse and validate definitions
        validateDefinitions(model, registry, context);

        return result;
    }

    /**
     * Traverses all types in the model and executes type and field validators. This method processes ALL registered
     * type categories: JsonClass, JsonInter, and JsonEnum. Uses JsonModelTypeTraverser to access all types including
     * those in package-private maps.
     *
     * @param model the model to traverse
     * @param registry the validator registry
     * @param context the validation context
     */
    protected void validateTypes(JsonModel model, ValidatorRegistry registry, ValidationContext context) {
        // Use the type traverser to visit all types (JsonClass, JsonInter, JsonEnum)
        Consumer<JsonType> typeValidator = (type) -> {
            for (TypeValidator validator : registry.getTypeValidators()) {
                validator.validate(type, context);
            }
        };

        Consumer<JsonField> fieldValidator = (field) -> {
            for (FieldValidator validator : registry.getFieldValidators()) {
                validator.validate(field, context);
            }
        };

        JsonModelTypeTraverser.traverseAllTypes(model, typeValidator, fieldValidator);
    }

    /**
     * Validates the definitions tree starting from the root.
     *
     * @param model the model containing the definitions
     * @param registry the validator registry
     * @param context the validation context
     */
    protected void validateDefinitions(JsonModel model, ValidatorRegistry registry, ValidationContext context) {
        JsonDefinitions root = model.getDefinitionsRoot();
        if (root == null) {
            return;
        }
        validateDefinitionsRecursive(root, registry, context);
    }

    /**
     * Recursively traverses the definitions tree and executes definitions validators.
     *
     * @param definitions the current definitions node
     * @param registry the validator registry
     * @param context the validation context
     */
    protected void validateDefinitionsRecursive(JsonDefinitions definitions, ValidatorRegistry registry, ValidationContext context) {
        // Execute definitions validators on the current node
        for (DefinitionsValidator validator : registry.getDefinitionsValidators()) {
            validator.validate(definitions, context);
        }

        // Recursively process children
        Iterator<JsonDefinitions> it = definitions.childrenIterator();
        while (it.hasNext()) {
            validateDefinitionsRecursive(it.next(), registry, context);
        }
    }
}
