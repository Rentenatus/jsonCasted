/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonField;

/**
 * Strategy interface for writing JSON structures during serialization.
 * Implementations of this interface define how JSON nodes, objects, arrays, and primitives
 * are written to the output, including formatting and path tracking.
 *
 * <p>This interface provides methods for writing different JSON elements:
 * <ul>
 *   <li>Start and end markers for JSON objects and arrays</li>
 *   <li>Attribute names and values</li>
 *   <li>Null values and primitive types</li>
 *   <li>Array separators</li>
 *   <li>Path information for tracking the current position in the JSON structure</li>
 * </ul>
 * </p>
 *
 * @author Janusch Rentenatus
 */
public interface WriteStrategy {

    /**
     * Writes the current path information for tracking the position in the JSON structure.
     *
     * @param intentPath the current path in the JSON structure
     */
    public void writePath(WriteNodePath intentPath);

    /**
     * Writes the start of a JSON object.
     *
     * @param jClassOrNull the JSON class being written, or null if not applicable
     * @param ob the object being serialized
     * @param parentField the parent field containing this object, or null if this is a root object
     * @param parent the parent object, or null if this is a root object
     * @param needsCast whether the object requires type casting
     * @param needsClassDef whether the class definition needs to be written
     * @param iString the indentation path for formatted output
     */
    public void writeStart(JsonClass jClassOrNull, Object ob, JsonField parentField, Object parent, boolean needsCast, boolean needsClassDef, WriteNodePath iString);

    /**
     * Writes the start of a JSON array.
     *
     * @param jTypeOrNull the JSON type of array elements, or null if not applicable
     * @param ob the array object being serialized
     * @param isPrimitive whether the array contains primitive types
     * @param iString the indentation path for formatted output
     */
    public void writeStartArray(JsonType jTypeOrNull, Object ob, boolean isPrimitive, WriteNodePath iString);

    /**
     * Writes the end of a JSON object.
     *
     * @param jClassOrNull the JSON class being closed, or null if not applicable
     * @param ob the object being serialized
     * @param isFollowing whether this is followed by another element
     * @param hasFieldKeys whether the object has field keys
     * @param iString the indentation path for formatted output
     */
    public void writeEnd(final JsonClass jClassOrNull, final Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString);

    /**
     * Writes the end of a JSON array.
     *
     * @param ob the array object being closed
     * @param isPrimitive whether the array contains primitive types
     * @param isFollowing whether this is followed by another element
     * @param iString the indentation path for formatted output
     */
    public void writeEndArray(final Object ob, boolean isPrimitive, boolean isFollowing, WriteNodePath iString);

    /**
     * Writes the indicator that a JSON object has field keys.
     *
     * @param jClassOrNull the JSON class, or null if not applicable
     * @param ob the object being serialized
     * @param iString the indentation path for formatted output
     */
    public void writeHasFieldKeys(JsonClass jClassOrNull, Object ob, WriteNodePath iString);

    /**
     * Writes a JSON attribute name.
     *
     * @param jClassOrNull the JSON class of the parent object, or null if not applicable
     * @param isFollowing whether this attribute is followed by another attribute
     * @param fName the field name to write
     * @param iString the indentation path for formatted output
     */
    public void writeAttrName(JsonClass jClassOrNull, boolean isFollowing, String fName, WriteNodePath iString);

    /**
     * Writes a null JSON value.
     *
     * @param iString the indentation path for formatted output
     */
    public void writeAttrNull(WriteNodePath iString);

    /**
     * Writes a primitive JSON value.
     *
     * @param jTypePrim the JSON type of the primitive value
     * @param attr the primitive value to write
     * @param iString the indentation path for formatted output
     */
    public void writePrimitive(JsonType jTypePrim, Object attr, WriteNodePath iString);

    /**
     * Writes a separator between array elements.
     *
     * @param primitive whether the array contains primitive types
     * @param iString the indentation path for formatted output
     */
    public void writeArraySeparator(boolean primitive, WriteNodePath iString);

    /**
     * Determines whether an object should be skipped during processing.
     * This can be used to avoid processing objects that have already been written
     * or should not be included in the output.
     *
     * @param jTypeOrNull the JSON type of the object, or null if not applicable
     * @param ob the object to check
     * @return true if the object should be skipped, false otherwise
     */
    default boolean skipProcess(JsonType jTypeOrNull, Object ob) {
        return false;
    }

    /**
     * Writes a JSON node value. This method handles the actual value output
     * for various JSON node types.
     *
     * @param object the value to write
     * @param iString the indentation path for formatted output
     */
    public void writeNodeValue(Object object, WriteNodePath iString);

}
