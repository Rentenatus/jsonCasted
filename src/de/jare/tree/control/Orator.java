/* <copyright>
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.tree.control;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 * A level-based event notifier that manages listeners at different priority levels.
 *
 * <p>
 * This class allows registering listeners with or without a specific level. Without a level, only one instance of a
 * listener can exist globally across all levels. With a level, uniqueness is only checked per level, meaning the same
 * listener can be registered at multiple different levels. This enables scenarios like having the same listener execute
 * both pre-action and post-action by placing it at different levels, effectively wrapping other listeners in
 * between.</p>
 *
 * <p>
 * Listeners are stored using weak references to allow garbage collection when they are no longer referenced elsewhere.
 * The {@code say} method invokes all active listeners with their respective levels.</p>
 *
 * <p>
 * Example usage:
 * <pre>
 * Orator&lt;Runnable&gt; orator = new Orator&lt;&gt;();
 *
 * // Register listener without level - only one instance can exist globally
 * orator.addListener(myListener);
 *
 * // Register same listener at different levels for pre/post wrapping
 * orator.addListener(1, myListener);  // pre-action
 * orator.addListener(10, myListener); // post-action
 *
 * // Notify all listeners
 * orator.say((level, listener) -> listener.run());
 * </pre>
 * </p>
 *
 * @param <T> the type of listeners this orator manages
 * @author Janusch Rentenatus
 */
public class Orator<T> {

    private final SortedMap<Integer, List<WeakReference<T>>> listenerRefMap;

    /**
     * Creates a new Orator with an empty listener map.
     */
    public Orator() {
        listenerRefMap = new TreeMap<>();
    }

    /**
     * Adds a listener without a specific level. The listener will be added at default level 5.
     *
     * <p>
     * Without a level specification, only one instance of this listener can exist across all levels. If the listener is
     * already registered at any level, it will not be added again.</p>
     *
     * @param listener the listener to add
     */
    public void addListener(final T listener) {
        if (!contains(listener)) {
            addListener(5, listener);
        }
    }

    /**
     * Adds a listener at a specific level.
     *
     * <p>
     * With a level specification, uniqueness is only checked within that level. This means the same listener can be
     * registered at multiple different levels. This is useful for implementing pre-action and post-action patterns
     * where the same listener needs to execute at different stages, effectively wrapping other listeners.</p>
     *
     * <p>
     * For example, a listener registered at level 1 and level 10 will be invoked twice during notification - once at
     * level 1 (pre) and once at level 10 (post), with all other listeners at levels 2-9 executing in between.</p>
     *
     * @param level the priority level at which to add the listener
     * @param listener the listener to add
     */
    public void addListener(int level, final T listener) {
        synchronized (listenerRefMap) {
            List<WeakReference<T>> listenerRefList = listenerRefMap.get(level);
            if (listenerRefList == null) {
                listenerRefList = new ArrayList<>();
                listenerRefMap.put(level, listenerRefList);
            }
            if (!contains(level, listener)) {
                listenerRefList.add(new WeakReference<>(listener));
            }
        }
        removeListener(null);
    }

    /**
     * Checks if a listener is registered at a specific level.
     *
     * @param level the level to check
     * @param listener the listener to look for
     * @return true if the listener is registered at the specified level, false otherwise
     */
    public boolean contains(int level, final T listener) {
        synchronized (listenerRefMap) {
            List<WeakReference<T>> listenerRefList = listenerRefMap.get(level);
            if (listenerRefList == null) {
                return false;
            }
            for (WeakReference<T> ref : listenerRefList) {
                if (ref.get() == listener) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a listener is registered at any level.
     *
     * <p>
     * This method searches across all levels to determine if the listener exists. Without a level specification, only
     * one instance of a listener can exist globally.</p>
     *
     * @param listener the listener to look for
     * @return true if the listener is registered at any level, false otherwise
     */
    public boolean contains(final T listener) {
        synchronized (listenerRefMap) {
            for (List<WeakReference<T>> listenerRefList : listenerRefMap.values()) {
                for (WeakReference<T> ref : listenerRefList) {
                    if (ref.get() == listener) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Removes a listener from all levels.
     *
     * <p>
     * This method removes all occurrences of the listener, regardless of the level at which it was registered. It also
     * cleans up any null references (garbage collected listeners) found during the process.</p>
     *
     * @param listener the listener to remove
     * @return true if the listener was found and removed, false otherwise
     */
    public boolean removeListener(final T listener) {
        List<WeakReference<T>> hits = new ArrayList<>();
        boolean removed = false;
        synchronized (listenerRefMap) {
            for (List<WeakReference<T>> listenerRefList : listenerRefMap.values()) {
                for (WeakReference<T> ref : listenerRefList) {
                    T candidate = ref.get();
                    if (candidate == null) {
                        hits.add(ref);
                    } else if (candidate == listener) {
                        hits.add(ref);
                        removed = true;
                    }
                }
                listenerRefList.removeAll(hits);
            }
        }
        return removed;
    }

    /**
     * Removes a listener from a specific level only.
     *
     * <p>
     * This method removes the listener only from the specified level, leaving it registered at any other levels. This
     * allows for fine-grained control when the same listener is registered at multiple levels for different purposes
     * (e.g., pre-action and post-action).</p>
     *
     * <p>
     * If the listener is not registered at the specified level, this method returns false and does not affect the
     * listener's registration at other levels.</p>
     *
     * @param level the level from which to remove the listener
     * @param listener the listener to remove
     * @return true if the listener was found and removed from the specified level, false otherwise
     */
    public boolean removeListener(int level, final T listener) {
        boolean removed = false;
        synchronized (listenerRefMap) {
            List<WeakReference<T>> listenerRefList = listenerRefMap.get(level);
            if (listenerRefList != null) {
                List<WeakReference<T>> hits = new ArrayList<>();
                for (WeakReference<T> ref : listenerRefList) {
                    T candidate = ref.get();
                    if (candidate == null) {
                        hits.add(ref);
                    } else if (candidate == listener) {
                        hits.add(ref);
                        removed = true;
                    }
                }
                listenerRefList.removeAll(hits);
            } 
        }
        return removed;
    }

    /**
     * Removes all listeners from all levels.
     *
     * <p>
     * This method clears the entire listener map, removing all registered listeners regardless of their level. After
     * this call, the orator will have no listeners.</p>
     */
    public void clear() {
        synchronized (listenerRefMap) {
            listenerRefMap.clear();
        }
    }

    /**
     * Notifies all registered listeners by invoking the consumer with each listener and its level.
     *
     * <p>
     * This method iterates through all levels in ascending order and invokes the consumer for each active (non-garbage
     * collected) listener. The consumer receives the level and the listener instance, allowing for level-specific
     * handling.</p>
     *
     * <p>
     * Listeners are invoked in level order, from lowest to highest. If the same listener is registered at multiple
     * levels, it will be invoked once for each level.</p>
     *
     * @param consumer the consumer to invoke for each listener, receiving the level and listener
     */
    public void say(BiConsumer<Integer, T> consumer) {
        List<T> hits = new ArrayList<>();
        synchronized (listenerRefMap) {
            for (Integer level : listenerRefMap.keySet()) {
                List<WeakReference<T>> listenerRefList = listenerRefMap.get(level);
                for (WeakReference<T> ref : listenerRefList) {
                    T candidate = ref.get();
                    if (candidate != null) {
                        hits.add(candidate);
                    }
                }
                for (T hit : hits) {
                    consumer.accept(level, hit);
                }
            }

        }
    }

    /**
     * Returns the number of active (non-garbage collected) listeners.
     *
     * <p>
     * This method counts all listeners that are still reachable (not garbage collected) across all levels. It also
     * performs cleanup of null references during the count.</p>
     *
     * <p>
     * Note: If the same listener is registered at multiple levels, it will be counted once for each level it appears
     * at.</p>
     *
     * @return the number of active listeners
     */
    public int getListenerCount() {
        List<WeakReference<T>> hits = new ArrayList<>();
        int found = 0;
        synchronized (listenerRefMap) {
            for (List<WeakReference<T>> listenerRefList : listenerRefMap.values()) {
                for (WeakReference<T> ref : listenerRefList) {
                    T candidate = ref.get();
                    if (candidate == null) {
                        hits.add(ref);
                    } else {
                        found++;
                    }
                }
                listenerRefList.removeAll(hits);
            }
        }
        return found;
    }

}
