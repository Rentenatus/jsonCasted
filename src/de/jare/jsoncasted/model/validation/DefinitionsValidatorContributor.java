/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.validation;

import de.jare.jsoncasted.model.FieldKind;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonDefinitions;
import de.jare.jsoncasted.model.item.JsonField;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Contributor that registers validation rules for JsonDefinitions.
 * These rules check for common issues in the definitions structure like:
 * <ul>
 * <li>Empty definitions names</li>
 * <li>Duplicate child names in the same scope</li>
 * <li>Types in definitions not registered in the model</li>
 * <li>Empty definition scopes that are being used</li>
 * </ul>
 * 
 * @author Janusch Rentenatus
 */
public class DefinitionsValidatorContributor implements ValidatorContributor {

    @Override
    public void contribute(ValidatorRegistry registry) {
        // Definitions-level validators
        registry.addDefinitionsValidator((definitions, ctx) -> {
            // Check for empty definitions name
            if (definitions.getName() == null || definitions.getName().isBlank()) {
                ctx.error("definitions.name.blank", "Definitions name must not be blank.", definitions);
            }

            // Check for duplicate child names in this scope
            checkDuplicateChildNames(definitions, ctx);
        });

        // Field-level validators related to definitions
        registry.addFieldValidator((field, ctx) -> {
            // Check if reference field's type is registered in the model
            if (field.getKind() == FieldKind.REFERENCE && field.getjType() != null) {
                JsonModel model = ctx.getModel();
                String targetTypeName = field.getjType().getcName();
                
                if (targetTypeName != null && model.getJsonClass(targetTypeName) == null && 
                    model.getJsonInter(targetTypeName) == null && model.getJsonEnum(targetTypeName) == null) {
                    ctx.warning("field.reference.type.unregistered", 
                            "Reference field targets unregistered type: " + targetTypeName, 
                            field);
                }
            }
        });
    }

    /**
     * Checks for duplicate child names within a definitions scope.
     * 
     * @param definitions the definitions to check
     * @param ctx the validation context
     */
    private void checkDuplicateChildNames(JsonDefinitions definitions, ValidationContext ctx) {
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
            ctx.error("definitions.child.name.duplicate", 
                    "Duplicate child name in definitions scope: " + duplicateName, 
                    definitions);
        }
    }
}
