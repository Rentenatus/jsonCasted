/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.DebugTuple;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The ConvertService class provides a central service for JSON to Java object conversion.
 * It manages the resource, model descriptor, wood resolution, and debug level required
 * for the conversion process, and provides utility methods for accessing these components.
 *
 * @author Janusch Rentenatus
 */
public class ConvertService {

    private JsonResource res;
    private JsonModelDescriptor descriptor;
    private WoodResolution resolution;
    private JsonDebugLevel debugLevel;

    /**
     * Constructs a ConvertService instance with the specified components.
     *
     * @param res The JSON resource containing the JSON data to convert.
     * @param descriptor The model descriptor containing type definitions.
     * @param resolution The wood resolution for handling object references.
     * @param debugLevel The debug level for controlling debug output.
     */
    public ConvertService(JsonResource res,
            JsonModelDescriptor descriptor,
            WoodResolution resolution,
            JsonDebugLevel debugLevel) {
        this.res = Objects.requireNonNull(res, "res must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
        this.resolution = Objects.requireNonNull(resolution, "resolution must not be null");
        this.debugLevel = Objects.requireNonNull(debugLevel, "debugLevel must not be null");
    }

    /**
     * Returns the JSON resource associated with this service.
     *
     * @return The JSON resource.
     */
    public JsonResource getRes() {
        return res;
    }

    /**
     * Sets the JSON resource for this service.
     *
     * @param res The JSON resource to set.
     */
    public void setRes(JsonResource res) {
        this.res = Objects.requireNonNull(res, "res must not be null");
    }

    /**
     * Returns the model descriptor associated with this service.
     *
     * @return The model descriptor.
     */
    public JsonModelDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the model descriptor for this service.
     *
     * @param descriptor The model descriptor to set.
     */
    public void setDescriptor(JsonModelDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
    }

    /**
     * Returns the wood resolution associated with this service.
     *
     * @return The wood resolution.
     */
    public WoodResolution getResolution() {
        return resolution;
    }

    /**
     * Sets the wood resolution for this service.
     *
     * @param resolution The wood resolution to set.
     */
    public void setResolution(WoodResolution resolution) {
        this.resolution = Objects.requireNonNull(resolution, "resolution must not be null");
    }

    /**
     * Returns the debug level associated with this service.
     *
     * @return The debug level.
     */
    public JsonDebugLevel getDebugLevel() {
        return debugLevel;
    }

    /**
     * Sets the debug level for this service.
     *
     * @param debugLevel The debug level to set.
     */
    public void setDebugLevel(JsonDebugLevel debugLevel) {
        this.debugLevel = Objects.requireNonNull(debugLevel, "debugLevel must not be null");
    }

    @Override
    public String toString() {
        return "ConvertService{"
                + "hasResource=" + (res != null)
                + ", hasDescriptor=" + (descriptor != null)
                + ", hasResolution=" + (resolution != null)
                + ", debugLevel=" + debugLevel
                + '}';
    }

    /**
     * Logs an informational message using the configured debug level.
     *
     * @param block The supplier providing the debug tuple containing the message and optional exception.
     */
    public void info(Supplier<DebugTuple> block) {
        debugLevel.info(block);
    }

    /**
     * Logs a warning message using the configured debug level.
     *
     * @param block The supplier providing the debug tuple containing the message and optional exception.
     */
    public void warning(Supplier<DebugTuple> block) {
        debugLevel.warning(block);
    }

    /**
     * Retrieves a type descriptor by its type name.
     *
     * @param typeName The name of the type to retrieve.
     * @return The type descriptor, or null if not found.
     */
    public JsonTypeDescriptor getType(String typeName) {
        return descriptor.getType(typeName);
    }

    /**
     * Retrieves a type descriptor by its type name with perceptive matching.
     *
     * @param typeName The name of the type to retrieve.
     * @return The type descriptor, or null if not found.
     */
    public JsonTypeDescriptor getTypePerceptive(String typeName) {
        return descriptor.getTypePerceptive(typeName);
    }

    /**
     * Returns the root JSON node from the associated resource.
     *
     * @return The root JSON node.
     */
    public JsonNode getRoot() {
        return res.getRoot();
    }

    /**
     * Returns the linking set from the associated resource.
     *
     * @return The linking set.
     */
    public LinkingSet getLinkingSet() {
        return res.getLinkingSet();
    }

    /**
     * Retrieves a resolved JSON item by its key from the wood resolution.
     *
     * @param key The key of the resolved object.
     * @return The resolved JSON item, or null if not found.
     */
    public JsonItem getResolvedObject(String key) {
        return resolution.getResolvedObject(key);
    }

    /**
     * Checks if the resolution contains the specified key.
     *
     * @param aKey The key to check.
     * @return true if the key is present in the resolution, false otherwise.
     */
    boolean containsResolutionKey(String aKey) {
        return getResolution().containsKey(aKey);
    }
}
