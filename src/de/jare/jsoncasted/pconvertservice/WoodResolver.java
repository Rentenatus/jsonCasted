/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonSystem;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.lang.LinkNodeEntry;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.parserservice.ParseStreamReader;
import de.jare.jsoncasted.parserservice.RootParser;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.wood.WoodProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class WoodResolver {

    public WoodResolver() {
        throw new IllegalStateException("Utility class");
    }

    public static WoodResolution resolve(
            JsonSystem sys,
            JsonModelDescriptor descriptor,
            JsonDebugLevel debugLevel) {

        Objects.requireNonNull(sys, "container must not be null");
        Objects.requireNonNull(descriptor, "descriptor must not be null");
        Objects.requireNonNull(debugLevel, "debugLevel must not be null");

        WoodResolution resolution = attempt(sys, descriptor, debugLevel);
        if (resolution.hasExceptions()) {
            return resolution;
        }
        if (resolution.isFullyResolved()) {
            return resolution;
        }

        boolean wasSomething = false;

        for (String synonym : resolution.unresolvedProvider()) {
            WoodProvider provider = sys.getProviderBox().findBySynonym(synonym);
            if (provider == null) {
                resolution.addException(new JsonParseException("The provider with the synonym " + synonym + " could not be found."));
                continue;
            }
            JsonResource imp = sys.findResourcesBySynonym(synonym);
            if (imp == null) {
                try {
                    JsonResource loaded = load(provider, debugLevel);
                    sys.addResource(loaded);
                    sys.getProviderBox().mergeBox(loaded.getExpectedBox());
                    wasSomething = true;
                } catch (FileNotFoundException ex) {
                    resolution.addException(new JsonParseException("The resource with the synonym " + synonym + " cannot be found.", ex));
                } catch (IOException | JsonParseException ex) {
                    resolution.addException(new JsonParseException("The resource with the synonym " + synonym + " cannot be loaded.", ex));
                }
            }
        }
        if (wasSomething) {
            return resolve(sys, descriptor, debugLevel);
        }

        return resolution;
    }

    public static JsonResource load(WoodProvider provider, JsonDebugLevel debugLevel) throws FileNotFoundException, IOException, JsonParseException {
        File file = new File(provider.getFilename());
        ParseStreamReader psr = new ParseStreamReader(new FileReader(file), debugLevel);
        JsonResource subContainer = JsonResource.forFile(provider.getFilename());
        subContainer.setProviderName(provider.getSynonym());
        return RootParser.parse(psr, subContainer, debugLevel);
    }

    public static WoodResolution attempt(JsonSystem sys, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) {
        JsonResource container = sys.getMainResource();
        LinkingSet linkingSet = Objects.requireNonNull(container.getLinkingSet(),
                "container.linkingSet must not be null");
        WoodResolution resolution = new WoodResolution();
        Set<String> remainingKeys = new LinkedHashSet<>(linkingSet.getObjectIdMap().keySet());
        boolean progress = true;
        ConvertService service = new ConvertService(container, descriptor, resolution, debugLevel);
        while (!remainingKeys.isEmpty() && progress) {
            progress = resolveLoop(remainingKeys, linkingSet, service);
        }
        for (String unresolved : remainingKeys) {
            resolution.addUnresolvedKey(unresolved);
        }
        return resolution;
    }

    private static boolean resolveLoop(Set<String> remainingKeys, LinkingSet linkingSet, ConvertService service) {
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
                JsonItem convertedObject = JsonObjectConverter.convertObject(node, typeDescriptor, service);
                convertedObject.setWoodKey(key);
                service.getResolution().putResolvedObject(key, convertedObject);
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

    private static boolean isConvertibleNow(JsonNode node,
            LinkingSet linkingSet,
            WoodResolution resolution) {

        if (node == null) {
            return false;
        }
        final String providerName = linkingSet.getProviderName();

        if (node.isArray()) {
            for (JsonNode child : node.asArray()) {
                if (!isConvertibleNow(child, linkingSet, resolution)) {
                    return false;
                }
            }
            return true;
        }

        if (!node.isObject()) {
            return true;
        }

        try {
            String linkKey = node.getLink(providerName);
            if (linkKey != null) {
                if (!resolution.getUnmodifiableResolvedObjects().containsKey(linkKey)) {
                    return false;
                }
            }
            String idKey = node.getObjectId(providerName);
            if (idKey != null) {
                if (!resolution.getUnmodifiableResolvedObjects().containsKey(idKey)) {
                    return isConvertibleBelow(node, linkingSet, resolution);
                }
            }
        } catch (JsonParseException ex) {
            resolution.addException(ex);
            return false;
        }

        return true;

    }

    public static boolean isConvertibleBelow(JsonNode node,
            LinkingSet linkingSet,
            WoodResolution resolution) {
        for (Map.Entry<String, JsonNode> entry : node.asObjectValues().entrySet()) {
            String key = entry.getKey();
            if (JsonTerms.TERM_WOOD_OBJECT_ID.equals(key) || JsonTerms.TERM_WOOD_LINK.equals(key)
                    || JsonTerms.TERM_CLASS.equals(key) || JsonTerms.TERM_WOOD_PROVIDERS.equals(key)) {
                continue;
            }
            if (!isConvertibleNow(entry.getValue(), linkingSet, resolution)) {
                return false;
            }
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
        JsonTypeDescriptor typeDescriptor = descriptor.getTypePerceptive(className);

        if (typeDescriptor == null) {
            throw new JsonParseException("No JsonClass found in descriptor for _class=" + className);
        }

        return typeDescriptor;
    }
}
