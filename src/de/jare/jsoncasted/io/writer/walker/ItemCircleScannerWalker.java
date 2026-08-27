/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer.walker;

import de.jare.jsoncasted.io.JsonWriteException;
import de.jare.jsoncasted.io.writer.WriteNodePath;
import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.JsonList;
import de.jare.jsoncasted.item.JsonObject;
import de.jare.jsoncasted.model.descriptor.JsonFieldDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Janusch Renteantus
 */
public class ItemCircleScannerWalker {

    final WriteNodePath intentPath;
    final Set<Object> findings;
    final Set<JsonWriteException> exceptions;

    /**
     * Constructs an ItemCircleScannerWalker instance.
     */
    public ItemCircleScannerWalker() {
        this.intentPath = new WriteNodePath("", new ArrayList<>());
        exceptions = new HashSet<>();
        findings = new HashSet<>();
    }

    /**
     * Returns the collection of exceptions encountered during the writing process.
     *
     * @return An unmodifiable collection of JsonWriteException instances.
     */
    public Collection<JsonWriteException> getExceptions() {
        return Collections.unmodifiableCollection(exceptions);
    }

    /**
     * Returns the collection of findings encountered during the writing process.
     *
     * @return An unmodifiable collection of findings.
     */
    public Collection<Object> getFindings() {
        return Collections.unmodifiableCollection(findings);
    }

    /**
     * Writes a JsonItem structure as JSON.
     *
     * @param item The JsonItem to write.
     */
    public void writeType(JsonItem item) {
        writeType(item, intentPath);
    }

    /**
     * Writes a JsonItem structure as JSON with a specified indentation.
     *
     * @param item The JsonItem to write.
     * @param iString The indentation string for formatted output.
     */
    protected void writeType(JsonItem item, WriteNodePath iString) {
        if (item == null) {
            return;
        }
        if (item instanceof JsonObject object) {
            writeObject(object, iString);
        } else if (item instanceof JsonList list) {
            writeList(list, iString);
        }
    }

    protected void writeObject(JsonObject object, WriteNodePath iString) {

        final long resolverId = object.getResolverId();
        if (resolverId >= 0 && iString.ids().contains(resolverId)) {
            checkCycle(object, iString);
            findings.add(object);
            return;
        }

        java.util.Iterator<String> it = object.getParamSet().iterator();
        WriteNodePath preChildIndent = iString.appendId(object.getResolverId());
        while (it.hasNext()) {
            final String nextName = it.next();

            JsonItem attr = object.getParam(nextName);
            final JsonFieldDescriptor field = object.getField(nextName);
            writeType(attr, preChildIndent.append(
                    field == null ? "n" : (field.isConstructorParam() ? "c" : "f")));
        }

    }

    protected void writeList(JsonList list, WriteNodePath iString) {
        java.util.Iterator<JsonItem> it = list.listIterator();
        WriteNodePath childIndent = iString.append("i").appendId(list.getResolverId());
        while (it.hasNext()) {
            JsonItem next = it.next();
            writeType(next, childIndent);
        }
    }

    protected void checkCycle(JsonObject object, WriteNodePath iString) {
        final long resolverId = object.getResolverId();
        int index = iString.ids().indexOf(resolverId);
        // check path from index to end for cycles
        String cycle = iString.path().substring(index);
        if (cycle.contains("c")) {
            exceptions.add(new JsonWriteException(
                    "Forbidden cycle via constructor parameters detected, edge types in cycle: '"
                    + cycle
                    + "'. See resolver ID: "
                    + resolverId));
        }
    }

}
