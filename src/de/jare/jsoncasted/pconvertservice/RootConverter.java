/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.parserwriter.JsonParseException;

public final class RootConverter {

    public RootConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static JsonItem convert(JsonResource res, String cName, JsonModelDescriptor descriptor, JsonDebugLevel debugLevel) throws JsonParseException {
        if (res == null) {
            return null;
        }
        if (res.getRoot() == null) {
            return null;
        }

        WoodResolution resolution = WoodConverter.convert(res, descriptor, debugLevel);
        return JsonNodeConverter.convert(res, cName, descriptor, resolution, debugLevel);
    }

}
