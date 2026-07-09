/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation.defdefault;

import de.jare.jsoncasted.model.item.JsonDefinitions;
import de.jare.jsoncasted.model.validation.DefinitionsValidator;
import de.jare.jsoncasted.model.validation.ValidationContext;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Validator that checks for duplicate child names within a definitions scope. Each definitions node should have unique
 * child names to avoid ambiguity.
 *
 * @author Janusch Rentenatus
 */
public class DuplicateChildNameValidator implements DefinitionsValidator {

    /**
     * Diagnostic code for duplicate child name errors.
     */
    public static final String DUPLICATE_CHILD_NAME_CODE = "definitions.child.name.duplicate";

    @Override
    public void validate(JsonDefinitions definitions, ValidationContext context) {
        Set<String> childNames = new HashSet<>();
        Set<String> duplicateNames = new HashSet<>();

        Iterator<JsonDefinitions> children = definitions.childrenIterator();
        while (children.hasNext()) {
            JsonDefinitions child = children.next();
            String name = child.getName();
            if (name != null && !name.isBlank()) {
                if (childNames.contains(name)) {
                    duplicateNames.add(name);
                } else {
                    childNames.add(name);
                }
            }
        }

        for (String duplicateName : duplicateNames) {
            context.error(DUPLICATE_CHILD_NAME_CODE,
                    "Duplicate child name in definitions scope: " + duplicateName,
                    definitions);
        }
    }
}
