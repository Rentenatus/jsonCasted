/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.pconvertservice;

import de.jare.jsoncasted.parserwriter.JsonParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class WoodResolution {

    private final Map<String, Object> resolvedObjects;
    private final Set<String> unresolvedKeys;
    private final List<JsonParseException> exceptions;

    public WoodResolution() {
        this.resolvedObjects = new LinkedHashMap<>();
        this.unresolvedKeys = new LinkedHashSet<>();
        this.exceptions = new ArrayList<>();
    }

    public boolean isFullyResolved() {
        return unresolvedKeys.isEmpty() && exceptions.isEmpty();
    }

    public boolean hasResolvedObjects() {
        return !resolvedObjects.isEmpty();
    }

    public boolean hasUnresolvedKeys() {
        return !unresolvedKeys.isEmpty();
    }

    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    public void putResolvedObject(String key, Object entry) {
        Objects.requireNonNull(key, "key must not be null");
        resolvedObjects.put(key, entry);
    }

    public void addUnresolvedKey(String key) {
        Objects.requireNonNull(key, "key must not be null");
        unresolvedKeys.add(key);
    }

    public void addException(JsonParseException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        exceptions.add(exception);
    }

    public Object getResolvedObject(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return resolvedObjects.get(key);
    }

    public Map<String, Object> getUnmodifiableResolvedObjects() {
        return Collections.unmodifiableMap(resolvedObjects);
    }

    public Set<String> getUnmodifiableUnresolvedKeys() {
        return Collections.unmodifiableSet(unresolvedKeys);
    }

    public List<JsonParseException> getUnmodifiableExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    @Override
    public String toString() {
        return "WoodResolution{"
                + "resolvedObjectsSize=" + resolvedObjects.size()
                + ", unresolvedKeysSize=" + unresolvedKeys.size()
                + ", exceptionsSize=" + exceptions.size()
                + '}';
    }
}
