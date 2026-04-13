/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.DebugTuple;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.lang.JsonNode;
import static de.jare.jsoncasted.lang.JsonTerms.TERM_CLASS;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonObjectConverter {

    public JsonObjectConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static JsonItem convertObject(JsonNode node, JsonTypeDescriptor contextClass,
            ConvertService service) throws JsonParseException {

        JsonItem cached = findCachedObject(node, service);
        if (cached != null) {
            return cached;
        }

        JsonObjectConverter converter = new JsonObjectConverter(node, contextClass, service);
        final JsonTypeDescriptor castedChildType
                = converter.castOrGet(contextClass, node.asObjectValues(), "this");
        if (castedChildType != null && castedChildType != contextClass) {
            converter.setCastedContext(castedChildType);
        }

        return converter.convertObject();
    }

    private static JsonItem findCachedObject(JsonNode node, ConvertService service) throws JsonParseException {
        String key = findResolvedKey(node, service);
        if (key == null) {
            return null;
        }
        return service.getResolvedObject(key);
    }

    private static String findResolvedKey(JsonNode node, ConvertService service) throws JsonParseException {
        if (node == null || !node.isObject()) {
            return null;
        }
        final String providerName = service.getLinkingSet().getProviderName();

        String providerKey = node.getObjectId(providerName);
        if (providerKey != null && service.containsResolutionKey(providerKey)) {
            return providerKey;
        }

        String linkKey = node.getLink(providerName);
        if (service.containsResolutionKey(linkKey)) {
            return linkKey;
        }
        return null;
    }

    final Map<String, JsonNode> values;
    private JsonObject myObject;
    private JsonTypeDescriptor contextClass;
    final ConvertService service;

    JsonObjectConverter(JsonNode node, JsonTypeDescriptor contextClass,
            ConvertService service) throws JsonParseException {

        this.values = node.asObjectValues();
        if (contextClass == null && values != null) {
            final JsonNode cast = values.get(TERM_CLASS);
            if (cast != null) {
                contextClass = service.getType(cast.asText());
            }
        }
        node.setJsonDescriptor(contextClass);
        if (contextClass == null) {
            throw new JsonParseException("No Class.");
        }
        this.contextClass = contextClass;
        this.myObject = new JsonObject(contextClass);
        this.service = service;
    }

    private void setCastedContext(JsonTypeDescriptor castedChildType) {
        this.contextClass = castedChildType;
        this.myObject = new JsonObject(castedChildType);
    }

    public JsonItem convertObject() throws JsonParseException {
        ArrayList<JsonParseException> exList = new ArrayList<>();
        values.forEach((paramName, childNode) -> {
            try {
                calculateParam(paramName, childNode);
            } catch (JsonParseException ex) {
                service.warning(() -> new DebugTuple(
                        "[WARNING] " + contextClass.getTypeName() + "." + paramName
                        + ": Convert failed.",
                        ex
                ));

                exList.add(ex);
            }
        });
        if (exList.isEmpty()) {
            return myObject;
        }
        if (exList.size() == 1) {
            throw exList.get(0);
        }
        StringJoiner joiner = new StringJoiner("; ");
        for (JsonParseException ex : exList) {
            joiner.add(ex.getMessage());
        }
        throw new JsonParseException(joiner.toString(), exList.get(0));
    }

    protected void calculateParam(String paramName, JsonNode childNode) throws JsonParseException {
        // Map object:
        if (contextClass.getMappingAllFields() != null) {
            calculateMapEntry(paramName, childNode);
            return;
        }

        JsonFieldDescriptor field = contextClass.getField(paramName);
        if (field == null) {
            if (TERM_CLASS.equals(paramName)) {
                return;
            }
            service.info(() -> new DebugTuple("{0}: Field {1} not found.",
                    contextClass.getTypeName(), paramName)
            );
            return;
        }

        final JsonTypeDescriptor childType = service.getType(field.getTypeName());
        final JsonTypeDescriptor castedChildType = castOrGet(childType, childNode.asObjectValues(), paramName);
        if (castedChildType == null) {
            return;
        }

        JsonItem paramObject = field.isAsListOrArray()
                ? JsonNodeConverter.convertArray(childNode, castedChildType, field.isAsList(), service)
                : JsonNodeConverter.convert(childNode, castedChildType, service);
        myObject.putParam(paramName, paramObject);

    }

    public JsonTypeDescriptor castOrGet(JsonTypeDescriptor suspectedType, Map<String, JsonNode> childValues, String paramName) {
        JsonTypeDescriptor castedChildType = suspectedType;
        final JsonNode cast = childValues == null ? null : childValues.get(TERM_CLASS);
        if (suspectedType == null) {
            if (cast == null) {
                Logger.getGlobal().log(Level.SEVERE, "{0}.{1}: Missing cast.",
                        new Object[]{contextClass.getTypeName(), paramName});
                return null;
            }
            castedChildType = service.getTypePerceptive(cast.asText());
            if (castedChildType == null) {
                Logger.getGlobal().log(Level.SEVERE, "{0}.{1}: Unknown class: {2}",
                        new Object[]{contextClass.getTypeName(), paramName, cast.asText()});
                return null;
            }
            service.info(() -> new DebugTuple("{0}.{1}: Cast used: {2}",
                    contextClass.getTypeName(), paramName, cast.asText())
            );
        } else if (cast != null) {
            final List<JsonTypeDescriptor> implementors = suspectedType.getImplementors();
            final JsonTypeDescriptor candidate = service.getTypePerceptive(cast.asText());
            if (implementors.isEmpty()) {
                if (candidate.containsSuper(suspectedType)) {

                    service.info(() -> new DebugTuple("{0}.{1}: Cast used: {2} extends {3}",
                            contextClass.getTypeName(),
                            paramName,
                            candidate.getTypeName(),
                            suspectedType.getTypeName()));
                    castedChildType = candidate;
                }
            } else {
                if (candidate == null) {
                    Logger.getGlobal().log(Level.SEVERE, "Unknown class: {0}", cast.asText());
                    return null;
                }
                castedChildType = null;
                for (JsonTypeDescriptor im : implementors) {
                    if (im.getTypeName().equals(candidate.getTypeName())) {
                        castedChildType = candidate;
                        service.info(() -> new DebugTuple("{0}.{1}: Cast used: {2} implements {3}",
                                contextClass.getTypeName(),
                                paramName,
                                candidate.getTypeName(),
                                suspectedType.getTypeName())
                        );
                        break;
                    }
                }
                if (castedChildType == null) {
                    Logger.getGlobal().log(Level.SEVERE, "{0}.{1}: Wrong cast: {2} inmplemts {3}",
                            new Object[]{contextClass.getTypeName(), paramName, cast.asText(), suspectedType.getTypeName()});
                    return null;
                }
            }

        }
        return castedChildType;
    }

    protected void calculateMapEntry(String paramName, JsonNode childNode) throws JsonParseException {
        JsonFieldDescriptor field = contextClass.getMappingAllFields();
        JsonTypeDescriptor childType = service.getType(field.getTypeName());
        JsonItem paramObject = field.isAsListOrArray()
                ? JsonNodeConverter.convertArray(childNode, childType, field.isAsList(), service)
                : JsonNodeConverter.convert(childNode, childType, service);
        myObject.putParam(paramName, paramObject);

    }

}
