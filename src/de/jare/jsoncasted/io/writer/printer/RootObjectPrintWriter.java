/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer.printer;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.writer.DefinitionsContext;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The RootObjectWriter class extends ObjectWriter and handles the serialization of JSON root-level structures,
 * including objects and arrays. It ensures proper formatting when serializing lists or single objects.
 *
 * @author Janusch Rentenatus
 */
public class RootObjectPrintWriter extends ObjectPrintWriter {

    private JsonResource resource;
    private boolean writeWoodMetadata;

    /**
     * Constructs a RootObjectWriter instance with default indentation.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     */
    public RootObjectPrintWriter(JsonItemDefinition definition, JsonType jType) {
        super(new DefinitionsContext(definition.getModel()), jType, definition.getCastingLevel(), JsonDebugLevel.SIMPLE);
    }

    /**
     * Constructs a RootObjectWriter instance with default indentation and debug level.
     *
     * @param definition The JSON item definition.
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectPrintWriter(JsonItemDefinition definition, JsonType jType, JsonDebugLevel debugLevel) {
        super(new DefinitionsContext(definition.getModel()), jType, definition.getCastingLevel(), debugLevel);
    }

    /**
     * Constructs a RootObjectWriter instance with default indentation.
     *
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     */
    public RootObjectPrintWriter(DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel) {
        super(definitionsContext, jType, castingLevel, JsonDebugLevel.SIMPLE);
    }

    /**
     * Constructs a RootObjectWriter instance with a specified indentation string.
     *
     * @param definitionsContext
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param intentString The indentation string for formatted output.
     */
    public RootObjectPrintWriter(DefinitionsContext definitionsContext, JsonType jType, String intentString, JsonCastingLevel castingLevel) {
        super(definitionsContext, jType, intentString, castingLevel, JsonDebugLevel.SIMPLE);
    }

    /**
     * Writes an object or list as a JSON structure. If the provided object is a list, it serializes it as a JSON array.
     * <p>
     * For single objects, Wood metadata (_woodProviders, _woodDefinitions) is written first if enabled.
     * </p>
     *
     * @param out The PrintWriter to write the JSON output.
     * @param ob The object or list to serialize.
     */
    @Override
    public void write(PrintWriter out, Object ob) {
        writeWoodMetadata = true;

        if (!(ob instanceof List<?>)) {
            super.write(out, ob);
            return;
        }

        List<?> myList = (List<?>) ob;
        if (myList.isEmpty()) {
            out.print(intentString);
            out.print("[]");
            out.flush();
            return;
        }

        Object ob0 = myList.get(0);
        JsonClass jClass = calculateJsonClass(ob0);
        out.print("[\n");
        Iterator<?> it = myList.iterator();
        out.print(intentString);
        super.write(out, jClass, it.next());

        while (it.hasNext()) {
            out.println(",");
            out.print(intentString);
            super.write(out, jClass, it.next());
        }

        out.print("\n]");
        out.flush();
    }

    @Override
    void writeCast(final JsonClass jClass, final PrintWriter out, final Object ob, String iString) {
        super.writeCast(jClass, out, ob, iString);
        if (writeWoodMetadata) {
            writeWoodMetadata = false;
            writeWood(out);
        }
    }

    void writeWood(PrintWriter out) {
        // For single objects: write Wood metadata first
        if (resource != null) {
            writeWoodProviders(out);

            // If we wrote Wood metadata, we need to handle the comma and object writing
            boolean hasWrittenMetadata = hasWoodMetadata();
            if (hasWrittenMetadata) {
                // Write the actual object with proper separation
                out.println(",");
                out.print(intentString);
            }
        }
        writeWoodDefinitions(out);
    }

    /**
     * Checks if there is any Wood metadata to write.
     */
    private boolean hasWoodMetadata() {
        if (resource == null) {
            return false;
        }
        return hasWoodProviders();
    }

    /**
     * Checks if the resource has wood providers.
     */
    private boolean hasWoodProviders() {
        if (resource == null || resource.getRoot() == null) {
            return false;
        }
        JsonNode rootNode = resource.getRoot();
        if (!rootNode.isObject()) {
            return false;
        }
        Map<String, JsonNode> rootValues = rootNode.asObjectValues();
        return rootValues.containsKey(JsonTerms.TERM_WOOD_PROVIDERS);
    }

    /**
     * Writes the _woodProviders field from the resource's root node. This assumes the root node is an object containing
     * the _woodProviders field.
     */
    private void writeWoodProviders(PrintWriter out) {
        if (resource == null || resource.getRoot() == null) {
            return;
        }

        JsonNode rootNode = resource.getRoot();
        if (!rootNode.isObject()) {
            return;
        }

        Map<String, JsonNode> rootValues = rootNode.asObjectValues();
        JsonNode providersNode = rootValues.get(JsonTerms.TERM_WOOD_PROVIDERS);

        if (providersNode == null) {
            return;
        }

        // Write the _woodProviders field
        out.print(intentString);
        out.print("\"");
        out.print(JsonTerms.TERM_WOOD_PROVIDERS);
        out.print("\": ");

        // Write the providers array/object
        writeNode(out, providersNode, intentString);
    }

    /**
     * Writes the _woodDefinitions field from the resource's definition nodes. Uses the DefinitionalWriter hierarchy to
     * process and categorize nodes.
     */
    private void writeWoodDefinitions(PrintWriter out) {
        if (resource == null || !resource.hasDefinitionNodes()) {
            return;
        }

        // Create _woodDefinitions object from definition nodes
        JsonNode definitionsObject = JsonNode.objectNode();

        for (JsonNode defNode : resource.getDefinitionNodes()) {
            String defId = getOrGenerateDefinitionId(defNode);
            definitionsObject.put(defId, defNode);
        }

        if (definitionsObject.asObjectValues().isEmpty()) {
            return;
        }

        // Write _woodDefinitions
        out.print(",\n" + intentString);
        out.print("\"");
        out.print(JsonTerms.TERM_WOOD_DEFINITIONS);
        out.print("\": ");
        writeNode(out, definitionsObject, intentString + "  ");
    }

    /**
     * Gets or generates a definition ID for a node.
     */
    private String getOrGenerateDefinitionId(JsonNode node) {
        if (node == null || !node.isObject()) {
            return "def_" + System.currentTimeMillis();
        }

        JsonNode idNode = node.asObjectValues().get(JsonTerms.TERM_WOOD_OBJECT_ID);
        if (idNode != null) {
            return idNode.asText();
        }

        // Generate from class name
        JsonNode classNode = node.asObjectValues().get(JsonTerms.TERM_CLASS);
        if (classNode != null) {
            String className = classNode.asText();
            if (className != null && !className.isEmpty()) {
                int lastDot = className.lastIndexOf('.');
                String simpleName = lastDot > 0 ? className.substring(lastDot + 1) : className;
                return simpleName.toLowerCase() + "_def";
            }
        }

        return "def_" + System.currentTimeMillis();
    }

}
