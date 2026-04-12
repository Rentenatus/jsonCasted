/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class WoodConverter {

    public WoodConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static WoodResolution convert(
            JsonResource container,
            JsonModelDescriptor descriptor,
            JsonDebugLevel debugLevel) {

        Objects.requireNonNull(container, "container must not be null");
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        Objects.requireNonNull(debugLevel, "debugLevel must not be null");

        LinkingSet linkingSet = Objects.requireNonNull(
                container.getLinkingSet(),
                "container.linkingSet must not be null");

        WoodResolution resolution = new WoodResolution();

        Set<String> remainingKeys = new LinkedHashSet<>(linkingSet.getObjectIdMap().keySet());
        boolean progress = true;

        ConvertService service = new ConvertService(container, descriptor, resolution, debugLevel);
        while (!remainingKeys.isEmpty() && progress) {
            progress = convertLoop(remainingKeys, linkingSet,  service);
        }

        for (String unresolved : remainingKeys) {
            resolution.addUnresolvedKey(unresolved);
        }

        return resolution;
    }

    private static boolean convertLoop(Set<String> remainingKeys, LinkingSet linkingSet, ConvertService service) {
        boolean progress;
        progress = false;
        Set<String> resolvedThisRound = new LinkedHashSet<>();
        for (String key : remainingKeys) {
            LinkNodeEntry entry = linkingSet.getObjectIdMap().get(key);
            if (entry == null) {
                service.getResolution().addUnresolvedKey(key);
                resolvedThisRound.add(key);
                continue;
            }

            final JsonNode node = entry.getNode();
            if (!isConvertibleNow(node, linkingSet, service.getResolution())) {
                continue;
            }

            try {
                JsonTypeDescriptor typeDescriptor = resolveContextClass(node, service.getDescriptor());
                Object builtObject = JsonObjectConverter.convertObject(node, typeDescriptor, service);
                service.getResolution().putResolvedObject(key, builtObject);
                resolvedThisRound.add(key);
                progress = true;
            } catch (JsonParseException ex) {
                service.getResolution().addException(ex);
                resolvedThisRound.add(key);
                progress = true;
            }
        }
        remainingKeys.removeAll(resolvedThisRound);
        return progress;
    }

    private static boolean isConvertibleNow(
            JsonNode node,
            LinkingSet linkingSet,
            WoodResolution resolution) {

        if (node == null) {
            return false;
        }

        if (node.isObject()) {
            JsonNode linkNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_LINK);
            if (linkNode != null) {
                try {
                    String linkKey = linkNode.toText();
                    if (!linkingSet.getObjectIdMap().containsKey(linkKey)
                            && !resolution.getUnmodifiableResolvedObjects().containsKey(linkKey)) {
                        return false;
                    }
                    if (linkingSet.getObjectIdMap().containsKey(linkKey)
                            && !resolution.getUnmodifiableResolvedObjects().containsKey(linkKey)) {
                        return false;
                    }
                } catch (JsonParseException ex) {
                    resolution.addException(ex);
                    return false;
                }
            }

            for (Map.Entry<String, JsonNode> entry : node.asObjectValues().entrySet()) {
                String key = entry.getKey();
                if (JsonTerms.TERM_WOOD_OBJECT_ID.equals(key) || JsonTerms.TERM_WOOD_LINK.equals(key)) {
                    continue;
                }
                if (!isConvertibleNow(entry.getValue(), linkingSet, resolution)) {
                    return false;
                }
            }
            return true;
        }

        if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                if (!isConvertibleNow(child, linkingSet, resolution)) {
                    return false;
                }
            }
            return true;
        }

        return true;
    }

    private static JsonTypeDescriptor resolveContextClass(JsonNode node, JsonModelDescriptor descriptor)
            throws JsonParseException {

        if (node == null || !node.isObject()) {
            throw new JsonParseException("Context node must be an object.");
        }

        JsonNode classNode = node.asObjectValues().get(JsonTerms.TERM_CLASS);
        if (classNode == null) {
            throw new JsonParseException("Missing _class on containment root node.");
        }

        String className = classNode.toText();
        JsonTypeDescriptor typeDescriptor = descriptor.getOrDefault(className, null);

        if (typeDescriptor == null) {
            throw new JsonParseException("No JsonClass found in descriptor for _class=" + className);
        }

        return typeDescriptor;
    }
}
