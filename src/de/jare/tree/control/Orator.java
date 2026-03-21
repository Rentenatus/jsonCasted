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
import java.util.function.Consumer;

/**
 *
 * @author Janusch Rentenatus
 * @param <T>
 */
public class Orator<T> {

    private final SortedMap<Integer, List<WeakReference<T>>> listenerRefMap;

    public Orator() {
        listenerRefMap = new TreeMap<>();
    }

    public void addListener(final T listener) {
        addListener(5, listener);
    }

    public void addListener(int level, final T listener) {
        synchronized (listenerRefMap) {
            List<WeakReference<T>> listenerRefList
                    = listenerRefMap.get(level);
            if (listenerRefList == null) {
                listenerRefList = new ArrayList<>();
                listenerRefMap.put(level, listenerRefList);
            }
            if (!contains(listener)) {
                listenerRefList.add(new WeakReference<>(listener));
            }
        }
        removeListener(null);
    }

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

    public void removeListener(final T listener) {
        List<WeakReference<T>> hits = new ArrayList<>();
        synchronized (listenerRefMap) {
            for (List<WeakReference<T>> listenerRefList : listenerRefMap.values()) {
                for (WeakReference<T> ref : listenerRefList) {
                    T candidate = ref.get();
                    if (candidate == listener || candidate == null) {
                        hits.add(ref);
                    }
                }
                listenerRefList.removeAll(hits);
            }
        }
    }

    public void clear() {
        synchronized (listenerRefMap) {
            listenerRefMap.clear();
        }
    }

    public void say(Consumer<T> consumer) {
        List<T> hits = new ArrayList<>();
        synchronized (listenerRefMap) {
            for (List<WeakReference<T>> listenerRefList : listenerRefMap.values()) {
                for (WeakReference<T> ref : listenerRefList) {
                    T candidate = ref.get();
                    if (candidate != null) {
                        hits.add(candidate);
                    }
                }
            }
            for (T hit : hits) {
                consumer.accept(hit);
            }
        }
    }
}
