/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.io.writer.WriteStrategy;
import de.jare.jsoncasted.io.writer.record.DefinitionsContext;
import de.jare.jsoncasted.io.writer.record.DefinitionsContextObjectRecord;
import de.jare.jsoncasted.lang.JsonNodeType;
import de.jare.jsoncasted.lang.JsonTerms;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.builder.JsonStringBuilder;
import de.jare.jsoncasted.model.descriptor.JsonDefinitionsDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A walker that writes the root object and handles the _woodDefinitions container. This walker extends
 * ObjectWriteWalker to provide specialized handling for root-level objects, including writing object definitions in the
 * _woodDefinitions section.
 *
 * @author Janusch Rentenatus
 */
public class RootObjectWriteWalker extends ObjectWriteWalker {

    private static final JsonClass JSON_CLASS_STRING = new JsonClass("String", JsonNodeType.STRING, new JsonStringBuilder());

    /**
     * Constructs a RootObjectWriteWalker instance with default indentation.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategy, definitionsContext, jType, null, null, castingLevel, debugLevel);
    }

    /**
     * Constructs a RootObjectWriteWalker instance with a specified indentation string.
     *
     * @param strategy the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param jType The JSON type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public RootObjectWriteWalker(WriteStrategy strategy, DefinitionsContext definitionsContext, JsonType jType, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategy, definitionsContext, jType, null, null, intentPath, castingLevel, debugLevel);
    }

    public void writeRoot(Object ob) throws NullPointerException, ClassCastException {
        strategy.writeStartFile();
        write(ob);
        strategy.writeEndFile();
    }

    @Override
    public void write(Object ob) throws NullPointerException, ClassCastException {
        if (!(ob instanceof List<?>)) {
            super.writeObjectProf(calculateJsonClass(ob), ob);
            return;
        }
        List<?> myList = (List<?>) ob;
        if (myList.isEmpty()) {
            strategy.writePath(intentPath);
            strategy.writeStartArray(null, ob, true, intentPath);
            strategy.writeEndArray(ob, true, false, intentPath);
            return;
        }
        Object ob0 = myList.get(0);
        JsonClass jClass = calculateJsonClass(ob0);
        writeList(jClass, ob0, null, null, intentPath);
    }

    @Override
    public boolean hasFieldKeys(JsonClass jClass, final Object ob) {
        final DefinitionsContext definitionsContext = this.objectGetter.getDefinitionsContext();
        return objectGetter.hasFieldKeys(jClass, ob)
                || definitionsContext.hasDefinitions();
    }

    /**
     * Writes the JsonModelDescriptor as a _woodModel subtree.
     *
     * @param iString the indentation path
     * @return true if the model descriptor was written, false otherwise
     */
    protected boolean writeModelDescription(WriteNodePath iString) {
        JsonModel model = objectGetter.getDefinitionsContext().getModel();
        if (model == null) {
            return false;
        }

        JsonModelDescriptor descriptor = model.getOrCreateDescriptor();
        if (descriptor == null || descriptor.isEmpty()) {
            return false;
        }

        // Write _woodModel start
        strategy.writeAttrName(null, false, JsonTerms.TERM_WOOD_MODEL, iString);
        strategy.writeStartObject(null, descriptor, null, null, false, false, iString);

        WriteNodePath modelIndent = iString.append("  ");

        // Write modelName
        strategy.writeAttrName(null, false, "modelName", modelIndent);
        strategy.writePrimitive(JSON_CLASS_STRING, descriptor.getModelName(), modelIndent);

        // Write describedTypes
        if (!descriptor.getDescribedTypes().isEmpty()) {
            strategy.writeArraySeparator(false, modelIndent);
            strategy.writeAttrName(null, false, "describedTypes", modelIndent);
            strategy.writeStartObject(null, descriptor.getDescribedTypes(), null, null, false, false, modelIndent);

            WriteNodePath typesIndent = modelIndent.append("  ");
            boolean firstType = true;
            for (Map.Entry<String, JsonTypeDescriptor> entry : descriptor.getTypeMap().entrySet()) {
                if (!firstType) {
                    strategy.writeArraySeparator(false, typesIndent);
                }
                firstType = false;

                strategy.writePrimitive(JSON_CLASS_STRING, entry.getKey(), typesIndent);
                writeModelDescriptionType(entry.getValue(), typesIndent);
            }
            strategy.writeEndObject(null, descriptor.getDescribedTypes(), true, false, modelIndent);
        }

        // Write repoDescriptors
        if (!descriptor.getRepoDescriptors().isEmpty()) {
            strategy.writeArraySeparator(false, modelIndent);
            strategy.writeAttrName(null, false, "repoDescriptors", modelIndent);
            strategy.writeStartObject(null, descriptor.getRepoDescriptors(), null, null, false, false, modelIndent);

            WriteNodePath reposIndent = modelIndent.append("  ");
            boolean firstRepo = true;
            for (Map.Entry<String, JsonModelDescriptor> entry : descriptor.getRepoDescriptors().entrySet()) {
                if (!firstRepo) {
                    strategy.writeArraySeparator(false, reposIndent);
                }
                firstRepo = false;

                strategy.writePrimitive(JSON_CLASS_STRING, entry.getKey(), reposIndent);
                writeModelDescriptionEntry(entry.getValue(), reposIndent);
            }
            strategy.writeEndObject(null, descriptor.getRepoDescriptors(), true, false, modelIndent);
        }

        // Write definitionsRoot
        if (descriptor.getDefinitionsRoot() != null) {
            strategy.writeArraySeparator(false, modelIndent);
            strategy.writeAttrName(null, false, "definitionsRoot", modelIndent);
            writeDefinitionsDescriptor(descriptor.getDefinitionsRoot(), modelIndent);
        }

        // Write _woodModel end
        strategy.writeEndObject(null, descriptor, true, false, iString);
        return true;
    }

    /**
     * Writes a JsonTypeDescriptor as part of the model description.
     *
     * @param typeDescriptor the type descriptor to write
     * @param iString the indentation path
     */
    private void writeModelDescriptionType(JsonTypeDescriptor typeDescriptor, WriteNodePath iString) {
        strategy.writeStartObject(null, typeDescriptor, null, null, false, false, iString);
        WriteNodePath typeIndent = iString.append("  ");

        strategy.writeAttrName(null, false, "typeName", typeIndent);
        strategy.writePrimitive(JSON_CLASS_STRING, typeDescriptor.getTypeName(), typeIndent);

        // Add other type descriptor fields as needed
        // This is a simplified version - extend based on JsonTypeDescriptor structure
        if (typeDescriptor.getNodeType() != null) {
            strategy.writeArraySeparator(false, typeIndent);
            strategy.writeAttrName(null, false, "nodeType", typeIndent);
            strategy.writePrimitive(JSON_CLASS_STRING, typeDescriptor.getNodeType().name(), typeIndent);
        }

        strategy.writeEndObject(null, typeDescriptor, true, false, iString);
    }

    /**
     * Writes a JsonModelDescriptor entry (for repoDescriptors).
     *
     * @param descriptor the model descriptor to write
     * @param iString the indentation path
     */
    private void writeModelDescriptionEntry(JsonModelDescriptor descriptor, WriteNodePath iString) {
        strategy.writeStartObject(null, descriptor, null, null, false, false, iString);
        WriteNodePath entryIndent = iString.append("  ");

        strategy.writeAttrName(null, false, "modelName", entryIndent);
        strategy.writePrimitive(JSON_CLASS_STRING, descriptor.getModelName(), entryIndent);

        if (!descriptor.getDescribedTypes().isEmpty()) {
            strategy.writeArraySeparator(false, entryIndent);
            strategy.writeAttrName(null, false, "describedTypes", entryIndent);
            strategy.writeStartObject(null, descriptor.getDescribedTypes(), null, null, false, false, entryIndent);

            WriteNodePath typesIndent = entryIndent.append("  ");
            boolean firstType = true;
            for (Map.Entry<String, JsonTypeDescriptor> typeEntry : descriptor.getTypeMap().entrySet()) {
                if (!firstType) {
                    strategy.writeArraySeparator(false, typesIndent);
                }
                firstType = false;

                strategy.writePrimitive(JSON_CLASS_STRING, typeEntry.getKey(), typesIndent);
                writeModelDescriptionType(typeEntry.getValue(), typesIndent);
            }
            strategy.writeEndObject(null, descriptor.getDescribedTypes(), true, false, entryIndent);
        }

        if (!descriptor.getRepoDescriptors().isEmpty()) {
            strategy.writeArraySeparator(false, entryIndent);
            strategy.writeAttrName(null, false, "repoDescriptors", entryIndent);
            strategy.writeStartObject(null, descriptor.getRepoDescriptors(), null, null, false, false, entryIndent);

            WriteNodePath reposIndent = entryIndent.append("  ");
            boolean firstRepo = true;
            for (Map.Entry<String, JsonModelDescriptor> repoEntry : descriptor.getRepoDescriptors().entrySet()) {
                if (!firstRepo) {
                    strategy.writeArraySeparator(false, reposIndent);
                }
                firstRepo = false;

                strategy.writePrimitive(JSON_CLASS_STRING, repoEntry.getKey(), reposIndent);
                writeModelDescriptionEntry(repoEntry.getValue(), reposIndent);
            }
            strategy.writeEndObject(null, descriptor.getRepoDescriptors(), true, false, entryIndent);
        }

        if (descriptor.getDefinitionsRoot() != null) {
            strategy.writeArraySeparator(false, entryIndent);
            strategy.writeAttrName(null, false, "definitionsRoot", entryIndent);
            writeDefinitionsDescriptor(descriptor.getDefinitionsRoot(), entryIndent);
        }

        strategy.writeEndObject(null, descriptor, true, false, iString);
    }

    /**
     * Writes a JsonDefinitionsDescriptor as part of the model description.
     *
     * @param definitionsDescriptor the definitions descriptor to write
     * @param iString the indentation path
     */
    private void writeDefinitionsDescriptor(JsonDefinitionsDescriptor definitionsDescriptor, WriteNodePath iString) {
        strategy.writeStartObject(null, definitionsDescriptor, null, null, false, false, iString);
        WriteNodePath defIndent = iString.append("  ");

        strategy.writeAttrName(null, false, "name", defIndent);
        strategy.writePrimitive(JSON_CLASS_STRING, definitionsDescriptor.getName(), defIndent);

        // Add types if present
        if (!definitionsDescriptor.getTypes().isEmpty()) {
            strategy.writeArraySeparator(false, defIndent);
            strategy.writeAttrName(null, false, "types", defIndent);
            strategy.writeStartArray(null, definitionsDescriptor.getTypes(), false, defIndent);

            WriteNodePath typesIndent = defIndent.append("  ");
            boolean first = true;
            for (String typeName : definitionsDescriptor.typeNames()) {
                if (!first) {
                    strategy.writeArraySeparator(false, typesIndent);
                }
                first = false;
                strategy.writePrimitive(JSON_CLASS_STRING, typeName, typesIndent);
            }
            strategy.writeEndArray(definitionsDescriptor.getTypes(), true, false, defIndent);
        }

        // Add children if present
        if (!definitionsDescriptor.getChildren().isEmpty()) {
            strategy.writeArraySeparator(false, defIndent);
            strategy.writeAttrName(null, false, "children", defIndent);
            strategy.writeStartObject(null, definitionsDescriptor.getChildren(), null, null, false, false, defIndent);

            WriteNodePath childrenIndent = defIndent.append("  ");
            boolean firstChild = true;
            for (JsonDefinitionsDescriptor child : definitionsDescriptor.getChildren()) {
                if (!firstChild) {
                    strategy.writeArraySeparator(false, childrenIndent);
                }
                firstChild = false;

                strategy.writePrimitive(JSON_CLASS_STRING, child.getName(), childrenIndent);
                writeDefinitionsDescriptor(child, childrenIndent);
            }
            strategy.writeEndObject(null, definitionsDescriptor.getChildren(), true, false, defIndent);
        }

        strategy.writeEndObject(null, definitionsDescriptor, true, false, iString);
    }

    /**
     * Writes the _woodDefinitions container with all definition objects.
     * Also writes the model description as _woodModel if present.
     *
     * @param iString the indentation path
     * @return true, if done
     */
    @Override
    protected boolean writeDefinitions(WriteNodePath iString) {
        final DefinitionsContext definitionsContext = objectGetter.getDefinitionsContext();
        List<DefinitionsContextObjectRecord> records = definitionsContext.getDefinitionRecords();
        boolean hasWrittenContent = false;

        // Write model description first if available
        if (writeModelDescription(iString)) {
            hasWrittenContent = true;
        }

        if (records.isEmpty()) {
            return hasWrittenContent;
        }

        if (hasWrittenContent) {
            strategy.writeArraySeparator(false, iString);
        }

        List<Object> objects = records.stream()
                .map(DefinitionsContextObjectRecord::getObject)
                .toList();

        // Write _woodDefinitions start
        strategy.writeAttrName(null, false, JsonTerms.TERM_WOOD_DEFINITIONS, iString);
        strategy.writeStartArray(null, objects, false, iString);

        WriteNodePath entryIndent = iString.append("  ");

        Iterator<DefinitionsContextObjectRecord> it = records.iterator();
        while (it.hasNext()) {
            DefinitionsContextObjectRecord next = it.next();
            Object ob = next.getObject();

            writeDefinitionEntry(ob, next.getJsonType(), entryIndent);
            if (it.hasNext()) {
                strategy.writeArraySeparator(false, entryIndent);
            }
            next.asAssigned();
        }
        // Write _woodDefinitions end
        strategy.writeEndArray(objects, true, true, iString);
        return true;
    }

    /**
     * Writes an individual JSON entry, handling primitive and object types. For definitions, always writes class
     * information unless casting level is NEVER.
     *
     * @param entry The object to serialize.
     * @param jsonType The JSON type of the entry.
     * @param iString The indentation string for formatted output.
     */
    protected void writeDefinitionEntry(Object entry, JsonType jsonType, WriteNodePath iString) {
        if (entry == null) {
            strategy.writeAttrNull(iString);
        } else {
            // For definitions, ensure class information is always written unless casting is NEVER
            JsonCastingLevel effectiveCastingLevel = objectGetter.getCastingLevel();
            if (effectiveCastingLevel != JsonCastingLevel.NEVER) {
                effectiveCastingLevel = JsonCastingLevel.ALWAYS_CLASS_DEF;
            }
            if (jsonType instanceof JsonMap jMap) {
                MapWriteWalker mapWriter = new MapWriteWalker(
                        strategy,
                        objectGetter.getDefinitionsContext(),
                        jMap, null,
                        iString,
                        effectiveCastingLevel,
                        objectGetter.getDebugLevel()
                );
                mapWriter.writeObject(mapWriter.calculateJsonClass(entry), entry);
            } else if (jsonType instanceof JsonClass jClass) {
                ObjectWriteWalker reWriter = new ObjectWriteWalker(strategy, objectGetter.getDefinitionsContext(), jsonType, null, null, iString, effectiveCastingLevel, objectGetter.getDebugLevel());
                reWriter.writeObjectProf(jClass, entry);
            }
        }
    }
}
