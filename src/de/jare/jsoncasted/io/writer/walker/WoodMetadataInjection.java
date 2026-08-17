/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.io.writer.WriteStrategy;

/**
 *
 * @author Janusch Renteantus
 */
public class WoodMetadataInjection {

    boolean carried = false;
    private final JsonResource woodResource;

    public WoodMetadataInjection(final JsonResource woodResource) {
        this.woodResource = woodResource;
    }

    public boolean isCarried() {
        return carried;
    }

    public boolean hasToDo() {
        return !carried;
    }

    public void carriedOut() {
        this.carried = true;
    }

    public static boolean hasInjection(WoodMetadataInjection woodMetadata) {
        return woodMetadata != null && woodMetadata.hasToDo();
    }

    public void popWood(WriteStrategy strategie, WriteNodePath iString, JsonDebugLevel debugLevel) {
        NodeWriteWalker reWriter = new NodeWriteWalker(strategie, iString, debugLevel);
        reWriter.writeNode(woodResource.getRoot());
        carried = true;
    }

}
