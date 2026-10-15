/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.convertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonValue;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.util.ArrayList;

/**
 * Converter to transform a JsonNode tree into the library's JsonItem model. This class provides the core conversion
 * logic for converting JSON node structures into JsonItem instances that can be processed by the builder services.
 *
 * @author Janusch Rentenatus
 */
public class JsonNodeConverter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException Always thrown as this is a utility class.
     */
    private JsonNodeConverter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Converts a JSON resource into a JsonItem using the specified context class and service.This is the main entry
     * point for converting JSON resources.
     *
     * @param res The JSON resource to convert.
     * @param cNameOrNull The name of the context class for type resolution.
     * @param descriptor The model descriptor containing type definitions.
     * @param resolution The wood resolution for handling object references.
     * @param debugLevel The debug level for controlling debug output.
     * @return The converted JsonItem, or null if input is null or empty.
     * @throws JsonParseException If conversion fails.
     */
    public static JsonItem convert(JsonResource res, String cNameOrNull, JsonModelDescriptor descriptor, WoodResolution resolution, JsonDebugLevel debugLevel) throws JsonParseException {
        if (res == null) {
            return null;
        }
        final JsonNode node = res.getRoot();
        if (node == null) {
            return null;
        }
        JsonTypeDescriptor contextClass = descriptor.getType(cNameOrNull);
        if (contextClass == null) {
            String className = res.getRoot().getCast();
            if (className != null) {
                contextClass = descriptor.getTypePerceptive(className);
            }
            if (contextClass == null) {
                return null;
            }
        }
        // Enrich _class field for root node if missing and type is known
        if (node.isObject() && node.asObjectValues().get(JsonTerms.TERM_CLASS) == null && cNameOrNull != null) {
            node.asObjectValues().put(JsonTerms.TERM_CLASS, JsonNode.stringNode(cNameOrNull));
        }

        ConvertService service = new ConvertService(res, descriptor, resolution, debugLevel);
        return convert(node, contextClass, service);
    }

    /**
     * Converts a JsonNode into a JsonItem using the specified context class and service. This method dispatches to
     * type-specific conversion methods based on the node type.
     *
     * @param node The JSON node to convert.
     * @param contextClass The context class for type resolution.
     * @param service The convert service providing access to resources and descriptors.
     * @return The converted JsonItem, or null if the node is null.
     * @throws JsonParseException If conversion fails or node type is unsupported.
     */
    public static JsonItem convert(JsonNode node, JsonTypeDescriptor contextClass, ConvertService service) throws JsonParseException {
        if (node == null) {
            return null;
        }
        switch (node.getType()) {
            case OBJECT:
                return JsonObjectConverter.convertObject(node, contextClass, service);
            case ARRAY:
                return convertArray(node, contextClass, false, service);
            case STRING:
                return convertString(node, contextClass, service);
            case NUMBER:
                return convertNumber(node, contextClass, service);
            case LONG:
                return convertLongNumber(node, contextClass, service);
            case BOOLEAN:
                return convertBoolean(node, contextClass, service);
            case NULL:
                return convertNull(node, contextClass, service);
            default:
                throw new JsonParseException("Unsupported JsonNode type: " + node.getType());
        }
    }

    /**
     * Converts a JsonNode array into a JsonList.
     *
     * @param node The JSON node containing the array.
     * @param contextClass The context class for type resolution.
     * @param asList If true, the result will be treated as a list.
     * @param service The convert service for accessing resources.
     * @return A JsonList containing the converted items.
     * @throws JsonParseException If conversion fails.
     */
    protected static JsonItem convertArray(JsonNode node, JsonTypeDescriptor contextClass, boolean asList, ConvertService service) throws JsonParseException {
        ArrayList<JsonItem> list = new ArrayList<>();
        // A list-typed field leads every value here, no matter its node
        // type: a null value or a malformed scalar is treated like a
        // missing entry (empty list) instead of crashing with an NPE.
        final java.util.List<JsonNode> children = node.asArray();
        if (children != null) {
            for (JsonNode child : children) {
                list.add(convert(child, contextClass, service));
            }
        }
        return new JsonList(list, asList, contextClass, service.incrementAtomicLong());
    }

    /**
     * Converts the value of a list-typed field, keeping identities alive: an array definition registers its list
     * under the wood id, a stub resolves to the shared instance - and pulls the definition node from the linking
     * set when the target is not converted yet, so the order of stub and definition does not matter. Two fields
     * that carry the same identity end up with the same JsonList instance.
     *
     * @param node The field value node: an array (definition or plain) or a stub object.
     * @param elementClass The element type of the field.
     * @param asList Whether the field is declared as list (or array).
     * @param service The convert service for accessing the resolution and the linking set.
     * @return The converted JsonList, shared between all fields with the same identity.
     * @throws JsonParseException If conversion fails or the identity is malformed.
     */
    static JsonItem convertListField(JsonNode node, JsonTypeDescriptor elementClass, boolean asList,
            ConvertService service) throws JsonParseException {
        final String key = listIdentityKey(node, service);
        if (key != null) {
            JsonItem shared = service.getResolvedObject(key);
            if (shared != null) {
                return shared;
            }
            if (!node.isArray()) {
                return convertStubbedList(key, elementClass, asList, service);
            }
        }
        final JsonItem list = convertArray(node, elementClass, asList, service);
        if (key != null && node.isArray()) {
            service.getResolution().putResolvedObject(key, list);
        }
        return list;
    }

    /**
     * Resolves a stub at a list field: when the linking set holds an array definition for the target key, that
     * definition is converted here - typed by this field's element type - and registered, so stub and definition
     * share one instance regardless of their order in the file. A stub without an array target degrades to an empty
     * list, mirroring the behavior of unresolved object stubs.
     */
    private static JsonItem convertStubbedList(String key, JsonTypeDescriptor elementClass, boolean asList,
            ConvertService service) throws JsonParseException {
        final LinkNodeEntry target = service.getLinkingSet().getObjectIdMap().get(key);
        if (target != null && target.getNode() != null && target.getNode().isArray()) {
            final JsonItem list = convertArray(target.getNode(), elementClass, asList, service);
            service.getResolution().putResolvedObject(key, list);
            return list;
        }
        return new JsonList(new ArrayList<>(), asList, elementClass, service.incrementAtomicLong());
    }

    /**
     * Finds the identity key of a list field value: the wood id of an array definition, or the id or link key of a
     * stub object. Plain arrays and scalars carry no identity.
     */
    private static String listIdentityKey(JsonNode node, ConvertService service) throws JsonParseException {
        if (node == null) {
            return null;
        }
        final String providerName = service.getLinkingSet().getProviderName();
        if (node.isArray()) {
            return node.getObjectId(providerName);
        }
        if (node.isObject()) {
            final String providerKey = node.getObjectId(providerName);
            if (providerKey != null) {
                return providerKey;
            }
            return node.getLink(providerName);
        }
        return null;
    }

    /**
     * Converts a JsonNode with string type into a JsonValue.
     *
     * @param node The JSON node containing the string value.
     * @param contextClass The context class for type resolution.
     * @param service The convert service for accessing resources.
     * @return A JsonValue containing the string.
     * @throws JsonParseException If conversion fails.
     */
    private static JsonItem convertString(JsonNode node, JsonTypeDescriptor contextClass, ConvertService service) throws JsonParseException {
        return new JsonValue(node.toText(), contextClass);
    }

    /**
     * Converts a JsonNode with number type into a JsonValue. If the context class expects a string, converts to string
     * representation.
     *
     * @param node The JSON node containing the number value.
     * @param contextClass The context class for type resolution.
     * @param service The convert service for accessing resources.
     * @return A JsonValue containing the number.
     * @throws JsonParseException If type check fails.
     */
    private static JsonItem convertNumber(JsonNode node, JsonTypeDescriptor contextClass, ConvertService service) throws JsonParseException {
        if (contextClass.getNodeType() == JsonNodeType.STRING) {
            return new JsonValue(node.toText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asNumber(), contextClass);
    }

    /**
     * Converts a JsonNode with long number type into a JsonValue. Handles type coercion between LONG and NUMBER types.
     *
     * @param node The JSON node containing the long value.
     * @param contextClass The context class for type resolution.
     * @param service The convert service for accessing resources.
     * @return A JsonValue containing the long or number.
     * @throws JsonParseException If type check fails.
     */
    private static JsonItem convertLongNumber(JsonNode node, JsonTypeDescriptor contextClass, ConvertService service) throws JsonParseException {
        if (contextClass.getNodeType() == JsonNodeType.STRING) {
            return new JsonValue(node.toText(), contextClass);
        }
        if (contextClass.getNodeType() == JsonNodeType.NUMBER) {
            return new JsonValue(node.toNumber(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asLong(), contextClass);
    }

    /**
     * Converts a JsonNode with boolean type into a JsonValue.
     *
     * @param node The JSON node containing the boolean value.
     * @param contextClass The context class for type resolution.
     * @param service The convert service for accessing resources.
     * @return A JsonValue containing the boolean.
     * @throws JsonParseException If type check fails.
     */
    private static JsonItem convertBoolean(JsonNode node, JsonTypeDescriptor contextClass, ConvertService service) throws JsonParseException {
        if (contextClass.getNodeType() == JsonNodeType.STRING) {
            return new JsonValue(node.toText(), contextClass);
        }
        checkType(node, contextClass);
        return new JsonValue(node.asBoolean(), contextClass);
    }

    /**
     * Converts a JsonNode with null type into a JsonValue.
     *
     * @param node The JSON node containing null.
     * @param contextClass The context class for type resolution.
     * @param service The convert service for accessing resources.
     * @return A JsonValue with null value.
     * @throws JsonParseException If conversion fails.
     */
    private static JsonItem convertNull(JsonNode node, JsonTypeDescriptor contextClass, ConvertService service) throws JsonParseException {
        return new JsonValue(contextClass);
    }

    /**
     * Checks if the node type matches the expected context class type. Allows LONG where NUMBER is expected for
     * compatibility.
     *
     * @param node The JSON node to check.
     * @param contextClass The context class defining the expected type.
     * @throws JsonParseException If types do not match.
     */
    private static void checkType(JsonNode node, JsonTypeDescriptor contextClass) throws JsonParseException {
        if (node.getType() != contextClass.getNodeType()) {
            // allow LONG where NUMBER is expected
            if (!(node.getType() == JsonNodeType.LONG && contextClass.getNodeType() == JsonNodeType.NUMBER)) {
                throw new JsonParseException("JsonNode type: " + node.getType() + " to class type missmatch.");
            }
        }
    }
}
