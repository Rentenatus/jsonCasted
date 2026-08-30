/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.convertservice;

import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.item.JsonItem;
import static de.jare.jsoncasted.lang.JsonTerms.COLONCOLON;
import static de.jare.jsoncasted.lang.JsonTerms.SELF_SYNONYM;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The WoodResolution class manages the resolution state for converting JSON objects with wood (object reference)
 * support. It tracks resolved objects, unresolved keys, and any exceptions that occurred during the resolution process.
 *
 * @author Janusch Rentenatus
 */
public class WoodResolution {

    private final Map<String, JsonItem> resolvedObjects;
    private final Set<String> unresolvedKeys;
    private final List<JsonParseException> exceptions;
    private final Map<String, JsonItem> answerMap;
    private final AtomicLong atomicLong;

    /**
     * Constructs a new WoodResolution instance with empty collections.
     */
    public WoodResolution() {
        this.resolvedObjects = new LinkedHashMap<>();
        this.unresolvedKeys = new LinkedHashSet<>();
        this.exceptions = new ArrayList<>();
        this.answerMap = new LinkedHashMap<>();
        this.atomicLong = new AtomicLong(100);
    }

    /**
     * Constructs a new WoodResolution instance with empty collections.
     *
     * @param parentWoodResolution
     */
    public WoodResolution(WoodResolution parentWoodResolution) {
        this.resolvedObjects = new LinkedHashMap<>();
        this.unresolvedKeys = new LinkedHashSet<>();
        this.exceptions = new ArrayList<>();
        this.answerMap = new LinkedHashMap<>();
        this.atomicLong = parentWoodResolution.atomicLong;
    }

    /**
     * Checks if all objects have been resolved and no exceptions occurred.
     *
     * @return true if fully resolved (no unresolved keys and no exceptions), false otherwise.
     */
    public boolean isFullyResolved() {
        return unresolvedKeys.isEmpty() && exceptions.isEmpty();
    }

    /**
     * Checks if any objects have been resolved.
     *
     * @return true if at least one object has been resolved, false otherwise.
     */
    public boolean hasResolvedObjects() {
        return !resolvedObjects.isEmpty();
    }

    /**
     * Checks if there are any unresolved keys.
     *
     * @return true if there are unresolved keys, false otherwise.
     */
    public boolean hasUnresolvedKeys() {
        return !unresolvedKeys.isEmpty();
    }

    /**
     * Checks if any exceptions occurred during resolution.
     *
     * @return true if there are exceptions, false otherwise.
     */
    public boolean hasExceptions() {
        return !exceptions.isEmpty();
    }

    /**
     * Adds a resolved object to the resolution with the specified key.
     *
     * @param key The key to associate with the resolved object.
     * @param entry The resolved JsonItem.
     * @throws NullPointerException If key is null.
     */
    public void putResolvedObject(String key, JsonItem entry) {
        Objects.requireNonNull(key, "key must not be null");
        resolvedObjects.put(key, entry);
    }

    /**
     * Adds an unresolved key to the resolution.
     *
     * @param key The key that could not be resolved.
     * @throws NullPointerException If key is null.
     */
    public void addUnresolvedKey(String key) {
        Objects.requireNonNull(key, "key must not be null");
        unresolvedKeys.add(key);
    }

    /**
     * Adds an exception that occurred during resolution.
     *
     * @param exception The exception to add.
     * @throws NullPointerException If exception is null.
     */
    public void addException(JsonParseException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        exceptions.add(exception);
    }

    /**
     * Retrieves a resolved object by its key.
     *
     * @param key The key of the resolved object.
     * @return The resolved JsonItem, or null if not found.
     * @throws NullPointerException If key is null.
     */
    public JsonItem getResolvedObject(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return resolvedObjects.get(key);
    }

    /**
     * Returns an unmodifiable view of all resolved objects.
     *
     * @return An unmodifiable map of resolved objects.
     */
    public Map<String, JsonItem> getUnmodifiableResolvedObjects() {
        return Collections.unmodifiableMap(resolvedObjects);
    }

    /**
     * Checks if a specific key is present in the resolved objects.
     *
     * @param aKey The key to check.
     * @return true if the key exists in resolved objects, false otherwise.
     */
    public boolean containsKey(String aKey) {
        return resolvedObjects.containsKey(aKey);
    }

    /**
     * Returns an unmodifiable view of all unresolved keys.
     *
     * @return An unmodifiable set of unresolved keys.
     */
    public Set<String> getUnmodifiableUnresolvedKeys() {
        return Collections.unmodifiableSet(unresolvedKeys);
    }

    /**
     * Returns an unmodifiable view of all exceptions.
     *
     * @return An unmodifiable list of exceptions.
     */
    public List<JsonParseException> getUnmodifiableExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * Extracts the provider names from unresolved keys. Keys with the format "provider::key" will have the provider
     * part extracted.
     *
     * @return A set of unresolved provider names.
     */
    public Set<String> unresolvedProvider() {
        Set<String> unresolvedProvider = new LinkedHashSet<>();
        for (String un : unresolvedKeys) {
            int index = un.indexOf(COLONCOLON);
            if (index > 0) {
                unresolvedProvider.add(un.substring(0, index));
            }
        }
        return unresolvedProvider;
    }

    public JsonItem getAnswer() {
        return answerMap.get(SELF_SYNONYM);
    }

    public void setAnswer(JsonItem answer) {
        answerMap.put(SELF_SYNONYM, answer);
    }

    /**
     * Adds or updates the root object for a specific provider.
     *
     * @param providerName The name of the provider.
     * @param rootObject The resolved root JsonItem for the provider.
     * @throws NullPointerException If providerName or rootObject is null.
     */
    public void putAnswer(String providerName, JsonItem rootObject) {
        if (rootObject == null) {
            return;
        }
        Objects.requireNonNull(providerName, "providerName must not be null");
        answerMap.put(providerName, rootObject);
    }

    /**
     * Retrieves the root object for a specific provider.
     *
     * @param providerName The name of the provider.
     * @return The resolved root JsonItem for the provider, or null if not found.
     * @throws NullPointerException If providerName is null.
     */
    public JsonItem getAnswer(String providerName) {
        Objects.requireNonNull(providerName, "providerName must not be null");
        return answerMap.get(providerName);
    }

    /**
     * Returns an unmodifiable view of the answer map containing all provider root objects.
     *
     * @return An unmodifiable map of provider names to their root JsonItems.
     */
    public Map<String, JsonItem> getAnswerMap() {
        return Collections.unmodifiableMap(answerMap);
    }

    /**
     * Checks if a root object exists for the specified provider.
     *
     * @param providerName The name of the provider to check.
     * @return true if the provider has a root object, false otherwise.
     * @throws NullPointerException If providerName is null.
     */
    public boolean hasAnswer(String providerName) {
        Objects.requireNonNull(providerName, "providerName must not be null");
        return answerMap.containsKey(providerName);
    }

    /**
     * Returns the set of all provider names that have a root object stored.
     *
     * @return An unmodifiable set of provider names.
     */
    public Set<String> getAnswerProviderNames() {
        return Collections.unmodifiableSet(answerMap.keySet());
    }

    public long getAtomicLong() {
        return atomicLong.get();
    }

    public long incrementAtomicLong() {
        return atomicLong.incrementAndGet();
    }

    @Override
    public String toString() {
        return "WoodResolution{"
                + "answerMap=" + answerMap
                + "}, resolvedObjectsSize=" + resolvedObjects.size()
                + ", unresolvedKeysSize=" + unresolvedKeys.size()
                + ", exceptionsSize=" + exceptions.size()
                + '}';
    }

}
