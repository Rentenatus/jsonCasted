/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.item.JsonClass;

/**
 *
 * @author Janusch Renteantus
 */
public interface WriteStrategie {

    public void writePath(WriteNodePath intentPath);

    public void writeStart(JsonClass jClassOtNull, Object ob, boolean needsCast, boolean needsClassDef, WriteNodePath iString);

    public void writeStartArray(Object ob, boolean isPrimitive, WriteNodePath iString);

    public void writeEnd(final JsonClass jClassOrNull, final Object ob, boolean isFollowing, boolean hasFieldKeys, WriteNodePath iString);

    public void writeEndArray(final Object ob, boolean isPrimitive, boolean isFollowing, WriteNodePath iString);

    public void writeHasFieldKeys(JsonClass jClassOrNull, Object ob, WriteNodePath iString);

    public void writeAttrName(JsonClass jClassOrNull, boolean isFollowing, String fName, WriteNodePath iString);

    public void writeAttrNull(WriteNodePath iString);

    public void writePrimitive(JsonType jTypePrim, Object attr, WriteNodePath iString);

    public void writeArraySeparator(boolean primitive, WriteNodePath iString);

    default boolean skippProzess(JsonType jTypeOrNull, Object ob) {
        return false;
    }

    public void writeNodeValue(Object object, WriteNodePath iString);

}
