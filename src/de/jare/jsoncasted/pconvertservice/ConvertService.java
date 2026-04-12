/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.DebugTuple;
import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.LinkingSet;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;
import java.util.Objects;
import java.util.function.Supplier;

public class ConvertService {

    private JsonResource res;
    private JsonModelDescriptor descriptor;
    private WoodResolution resolution;
    private JsonDebugLevel debugLevel;

    public ConvertService(JsonResource res,
            JsonModelDescriptor descriptor,
            WoodResolution resolution,
            JsonDebugLevel debugLevel) {
        this.res = Objects.requireNonNull(res, "res must not be null");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
        this.resolution = Objects.requireNonNull(resolution, "resolution must not be null");
        this.debugLevel = Objects.requireNonNull(debugLevel, "debugLevel must not be null");
    }

    public JsonResource getRes() {
        return res;
    }

    public void setRes(JsonResource res) {
        this.res = Objects.requireNonNull(res, "res must not be null");
    }

    public JsonModelDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(JsonModelDescriptor descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor must not be null");
    }

    public WoodResolution getResolution() {
        return resolution;
    }

    public void setResolution(WoodResolution resolution) {
        this.resolution = Objects.requireNonNull(resolution, "resolution must not be null");
    }

    public JsonDebugLevel getDebugLevel() {
        return debugLevel;
    }

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

    public void info(Supplier<DebugTuple> block) {
        debugLevel.info(block);
    }

    public void warning(Supplier<DebugTuple> block) {
        debugLevel.warning(block);
    }

    public JsonTypeDescriptor getType(String typeName) {
        return descriptor.getType(typeName);
    }

    public JsonTypeDescriptor getTypePerceptive(String typeName) {
        return descriptor.getTypePerceptive(typeName);
    }

    public JsonNode getRoot() {
        return res.getRoot();
    }

    public LinkingSet getLinkingSet() {
        return res.getLinkingSet();
    }

    public Object getResolvedObject(String key) {
        return resolution.getResolvedObject(key);
    }
}
