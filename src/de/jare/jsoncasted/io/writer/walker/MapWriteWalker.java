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
import de.jare.jsoncasted.lang.JsonInstance;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.model.item.JsonMap;
import java.util.Iterator;

/**
 *
 * @author Janusch Renteantus
 */
public class MapWriteWalker extends ObjectWriteWalker {

    private final JsonMap jMap;

    /**
     * Constructs a MapWriteWalker instance.
     *
     * @param strategie the write strategy to use
     * @param definitionsContext the context for definitions
     * @param castingLevel the casting level for serialization
     * @param parent the parent object
     * @param jMap The JSON map type used for serialization.
     * @param intentPath The indentation string for formatted output.
     * @param debugLevel The debug level for controlling debug output.
     */
    public MapWriteWalker(WriteStrategy strategie, DefinitionsContext definitionsContext, JsonMap jMap, Object parent, WriteNodePath intentPath, JsonCastingLevel castingLevel, JsonDebugLevel debugLevel) {
        super(strategie, definitionsContext, jMap, null, parent, intentPath, castingLevel, debugLevel);
        this.jMap = jMap;
    }

    /**
     * Writes a JSON object representation.
     *
     * @param jClass The JSON class defining the object's structure.
     * @param ob The object to serialize.
     */
    @Override
    public void writeObject(final JsonClass jClass, final Object ob) {
        if (!(ob instanceof JsonInstance<?> inst)) {
            objectGetter.warning(new ClassCastException(), () -> "Expected instance of JsonInstance<?>");
            return;
        }
        WriteNodePath iString = intentPath.append("  ");
        writeStart(jClass, ob, iString);

        boolean isFollowing = false;
        boolean hasFieldKeys = false;
        try {
            Iterator<String> it = inst.keySet().iterator();
            hasFieldKeys = it.hasNext();
            if (hasFieldKeys) {
                strategy.writeHasFieldKeys(jClass, ob, iString);
            }

            while (it.hasNext()) {
                final String nextName = it.next();
                Object attr = inst.get(nextName);

                if (attr == null && jClass.isSkippingNulls()) {
                    continue;
                }
                // Skip if already processed
                if (strategy.skipProcess(jMap.getItemClass(), attr)) {
                    continue;
                }

                strategy.writeAttrName(jMap.getItemClass(), isFollowing, nextName, iString);
                isFollowing = true;

                if (jMap.isAsListOrArray()) {
                    writeList(jMap.getItemClass(), attr, null, null, iString);
                } else {
                    writeSingle(jMap.getItemClass(), attr, null, null, iString);
                }
            }
        } finally {
            strategy.writeEndObject(jClass, ob, isFollowing, hasFieldKeys, intentPath);
        }
    }

}
