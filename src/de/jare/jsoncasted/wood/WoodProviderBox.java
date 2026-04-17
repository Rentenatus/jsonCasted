/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WoodProviderBox {

    private final List<WoodProvider> providers;

    public WoodProviderBox(List<WoodProvider> _woodProviders) {
        Objects.requireNonNull(_woodProviders, "providers must not be null");
        this.providers = new ArrayList<>(_woodProviders);
    }

    public List<WoodProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    public WoodProvider findBySynonym(String synonym) {
        for (WoodProvider provider : providers) {
            if (provider.matchesSynonym(synonym)) {
                return provider;
            }
        }
        return null;
    }

    public boolean containsSynonym(String synonym) {
        return findBySynonym(synonym) != null;
    }

    public int size() {
        return providers.size();
    }

    public boolean isEmpty() {
        return providers.isEmpty();
    }

    public void mergeBox(WoodProviderBox box) {
        if (box == null || box.isEmpty()) {
            return;
        }
        for (WoodProvider provider : box.providers) {
            if (!containsSynonym(provider.getSynonym())) {
                providers.add(provider);
            }
        }
    }

    @Override
    public String toString() {
        return "WoodProviderBox{"
                + "providers=" + providers
                + '}';
    }

}
