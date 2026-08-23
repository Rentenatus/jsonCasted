/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonWriteException;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Scans Java objects for cycles before serialization. Analog to ItemCircleScannerWalker but operates on Java objects
 * instead of JsonItem structures.
 *
 * @author Janusch Rentenatus
 */
public class ObjectCircleScannerWalker {

    final WriteNodePath intentPath;
    final Set<Object> findings;
    final Set<JsonWriteException> exceptions;
    final DefinitionsContext definitionsContext;
    final JsonCastingLevel castingLevel;

    /**
     * Constructs an ObjectCircleScannerWalker with the necessary context.
     *
     * @param definitionsContext The definitions context for resolving JSON types
     * @param castingLevel The casting level for serialization
     */
    public ObjectCircleScannerWalker(DefinitionsContext definitionsContext, JsonCastingLevel castingLevel) {
        this.intentPath = new WriteNodePath("", new ArrayList<>());
        this.findings = new HashSet<>();
        this.exceptions = new HashSet<>();
        this.definitionsContext = definitionsContext;
        this.castingLevel = castingLevel;
    }

    /**
     * Returns the collection of exceptions encountered during the scanning process.
     *
     * @return An unmodifiable collection of JsonWriteException instances.
     */
    public Collection<JsonWriteException> getExceptions() {
        return Collections.unmodifiableCollection(exceptions);
    }

    /**
     * Returns the collection of objects involved in cycles.
     *
     * @return An unmodifiable collection of objects that are part of detected cycles.
     */
    public Collection<Object> getFindings() {
        return Collections.unmodifiableCollection(findings);
    }

    /**
     * Scans an object for cycles using its JSON class definition.
     *
     * @param ob The object to scan
     * @param jClass The JSON class definition of the object
     */
    public void scan(Object ob, JsonClass jClass) {
        scan(ob, jClass, intentPath);
    }

    /**
     * Scans an object for cycles with a specified path.
     *
     * @param ob The object to scan
     * @param jClass The JSON class definition of the object
     * @param path The current path in the object graph
     */
    protected void scan(Object ob, JsonClass jClass, WriteNodePath path) {
        if (ob == null) {
            return;
        }

        // Use identity hash code to uniquely identify objects regardless of their hashCode() implementation
        int objectId = System.identityHashCode(ob);

        // Cycle detection: object already in path?
        if (path.ids().contains(objectId)) {
            checkCycle(ob, path);
            findings.add(ob);
            return;
        }

        // Add object to path
        WriteNodePath objectPath = path.appendOb(objectId);

        // Handle different object types
        if (ob instanceof Collection<?>) {
            scanCollection((Collection<?>) ob, jClass, objectPath);
        } else if (ob.getClass().isArray()) {
            scanArray(ob, jClass, objectPath);
        } else if (jClass != null) {
            scanObject(ob, jClass, objectPath);
        }
    }

    /**
     * Scans a collection for cycles.Note: This method is called when the object is a Collection but we don't have the
     * JsonField context.For proper item type resolution, we need the JsonField that declared this collection.
     *
     * @param collection
     * @param jClass
     * @param path
     */
    protected void scanCollection(Collection<?> collection, JsonClass jClass, WriteNodePath path) {
        WriteNodePath listPath = path.append("i");
        int collectionId = System.identityHashCode(collection);
        listPath = listPath.appendOb(collectionId);

        for (Object item : collection) {
            if (item != null) {
                // Without JsonField context, we can only use the runtime class
                // This is a fallback for raw collections without type information
                JsonClass itemClass = definitionsContext.getModel().getJsonClass(item.getClass());
                scan(item, itemClass, listPath);
            }
        }
    }

    /**
     * Scans an array for cycles.Note: This method is called when the object is an array but we don't have the JsonField
     * context.
     *
     * @param array
     * @param jClass
     * @param path
     */
    protected void scanArray(Object array, JsonClass jClass, WriteNodePath path) {
        WriteNodePath arrayPath = path.append("i");
        int arrayId = System.identityHashCode(array);
        arrayPath = arrayPath.appendOb(arrayId);

        int length = java.lang.reflect.Array.getLength(array);
        Class<?> componentType = array.getClass().getComponentType();

        for (int i = 0; i < length; i++) {
            Object item = java.lang.reflect.Array.get(array, i);
            if (item != null) {
                // Without JsonField context, use the array component type
                JsonClass itemClass = definitionsContext.getModel().getJsonClass(componentType);
                scan(item, itemClass, arrayPath);
            }
        }
    }

    /**
     * Scans a regular object for cycles by examining its fields.
     *
     * @param ob
     * @param jClass
     * @param path
     */
    protected void scanObject(Object ob, JsonClass jClass, WriteNodePath path) {
        Iterator<String> it = jClass.keysForWriteIterator(ob);
        while (it.hasNext()) {
            String fieldName = it.next();
            JsonField jsonField = jClass.getField(fieldName);
            Object fieldValue = jClass.getAttr(jsonField, ob, null);

            if (fieldValue != null) {
                JsonType fieldType = jsonField.getjType();

                // Skip primitive types and their boxed variants
                if (!fieldType.isBoxOrPrimitive()) {
                    JsonClass fieldClass = definitionsContext.getModel().getJsonClass(fieldType.getcName());
                    boolean isConstructorParam = jsonField.isConstructorParam();

                    WriteNodePath fieldPath = path.append(isConstructorParam ? "c" : "f");
                    scan(fieldValue, fieldClass, fieldPath);
                }
            }
        }
    }

    /**
     * Checks if a detected cycle is forbidden (contains constructor parameters).
     *
     * @param ob The object that forms the cycle
     * @param path The path where the cycle was detected
     */
    protected void checkCycle(Object ob, WriteNodePath path) {
        int objectId = System.identityHashCode(ob);
        int index = path.ids().indexOf(objectId);

        // Extract the cycle path from the index to the end
        String cyclePath = path.path().substring(index);

        // Forbid cycles that go through constructor parameters
        if (cyclePath.contains("c")) {
            exceptions.add(new JsonWriteException(
                    "Forbidden cycle via constructor parameters detected, edge types in cycle: '"
                    + cyclePath
                    + "'. See object: " + ob.getClass().getSimpleName()));
        }
    }
}
