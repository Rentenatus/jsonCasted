/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import de.jare.jsoncasted.model.item.JsonInter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Helper class to traverse all types in a JsonModel.
 * This class is in the same package as JsonModel to access package-private fields.
 * 
 * @author Janusch Rentenatus
 */
public class JsonModelTypeTraverser {

    /**
     * Visits all types in the model with a consumer.
     * This includes JsonClass, JsonInter, and JsonEnum (stored as JsonClass).
     * 
     * @param model the model to traverse
     * @param typeConsumer consumer that receives each JsonType
     * @param fieldConsumer consumer that receives each JsonField of each type
     */
    public static void traverseAllTypes(JsonModel model, Consumer<JsonType> typeConsumer, Consumer<JsonField> fieldConsumer) {
        // Traverse all JsonClass instances (regular classes)
        Iterator<JsonClass> classIt = model.classesIterator();
        while (classIt.hasNext()) {
            JsonClass jsonClass = classIt.next();
            if (typeConsumer != null) {
                typeConsumer.accept(jsonClass);
            }
            traverseFields(jsonClass, fieldConsumer);
        }

        // Traverse all JsonInter instances (interfaces)
        for (JsonInter inter : model.interfaces.values()) {
            if (typeConsumer != null) {
                typeConsumer.accept(inter);
            }
            // Validate fields in all implementation classes of the interface
            for (JsonClass implClass : inter) {
                traverseFields(implClass, fieldConsumer);
            }
        }

        // Traverse all JsonEnum instances (enums, stored as JsonClass)
        for (JsonClass enumClass : model.enums.values()) {
            if (typeConsumer != null) {
                typeConsumer.accept(enumClass);
            }
            traverseFields(enumClass, fieldConsumer);
        }
    }

    /**
     * Traverses all fields of a JsonClass and applies the field consumer.
     * 
     * @param jsonClass the class whose fields to traverse
     * @param fieldConsumer consumer that receives each field
     */
    private static void traverseFields(JsonClass jsonClass, Consumer<JsonField> fieldConsumer) {
        if (fieldConsumer == null) {
            return;
        }
        Iterator<JsonField> fieldIt = jsonClass.fieldsIterator();
        while (fieldIt.hasNext()) {
            JsonField field = fieldIt.next();
            fieldConsumer.accept(field);
        }
    }

    /**
     * Returns a list of all JsonType instances in the model.
     * 
     * @param model the model
     * @return list of all types
     */
    public static List<JsonType> getAllTypes(JsonModel model) {
        List<JsonType> allTypes = new ArrayList<>();
        
        // Add all classes
        Iterator<JsonClass> classIt = model.classesIterator();
        while (classIt.hasNext()) {
            allTypes.add(classIt.next());
        }
        
        // Add all interfaces
        allTypes.addAll(model.interfaces.values());
        
        // Add all enums (stored as JsonClass)
        allTypes.addAll(model.enums.values());
        
        return allTypes;
    }

    /**
     * Returns a list of all JsonField instances across all types in the model.
     * 
     * @param model the model
     * @return list of all fields
     */
    public static List<JsonField> getAllFields(JsonModel model) {
        List<JsonField> allFields = new ArrayList<>();
        
        // Add fields from all classes
        Iterator<JsonClass> classIt = model.classesIterator();
        while (classIt.hasNext()) {
            JsonClass jsonClass = classIt.next();
            addFieldsFromClass(jsonClass, allFields);
        }
        
        // Add fields from all interface implementations
        for (JsonInter inter : model.interfaces.values()) {
            for (JsonClass implClass : inter) {
                addFieldsFromClass(implClass, allFields);
            }
        }
        
        // Add fields from all enums
        for (JsonClass enumClass : model.enums.values()) {
            addFieldsFromClass(enumClass, allFields);
        }
        
        return allFields;
    }

    private static void addFieldsFromClass(JsonClass jsonClass, List<JsonField> targetList) {
        Iterator<JsonField> fieldIt = jsonClass.fieldsIterator();
        while (fieldIt.hasNext()) {
            targetList.add(fieldIt.next());
        }
    }
}
