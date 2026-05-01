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

/**
 * Container for a collection of WoodProvider instances.
 *
 * <p>This class manages multiple {@link WoodProvider} instances and provides
 * lookup and merging capabilities for resource provider management.</p>
 *
 * @see WoodProvider
 */
public final class WoodProviderBox {

    private final List<WoodProvider> providers;

    /**
     * Constructs a WoodProviderBox with the specified list of providers.
     *
     * @param _woodProviders the list of providers (must not be null).
     * @throws NullPointerException if the provider list is null.
     */
    public WoodProviderBox(List<WoodProvider> _woodProviders) {
        Objects.requireNonNull(_woodProviders, "providers must not be null");
        this.providers = new ArrayList<>(_woodProviders);
    }

    /**
     * Returns an unmodifiable list of all providers.
     *
     * @return unmodifiable list of WoodProvider instances.
     */
    public List<WoodProvider> getProviders() {
        return Collections.unmodifiableList(providers);
    }

    /**
     * Finds a provider by its synonym.
     *
     * @param synonym the synonym to search for.
     * @return the matching WoodProvider, or {@code null} if not found.
     */
    public WoodProvider findBySynonym(String synonym) {
        for (WoodProvider provider : providers) {
            if (provider.matchesSynonym(synonym)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Checks if this box contains a provider with the specified synonym.
     *
     * @param synonym the synonym to check.
     * @return {@code true} if a matching provider exists, {@code false} otherwise.
     */
    public boolean containsSynonym(String synonym) {
        return findBySynonym(synonym) != null;
    }

    /**
     * Returns the number of providers in this box.
     *
     * @return the provider count.
     */
    public int size() {
        return providers.size();
    }

    /**
     * Checks if this box is empty.
     *
     * @return {@code true} if there are no providers, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * Merges another provider box into this one.
     *
     * <p>Providers from the other box are added only if their synonym
     * is not already present in this box.</p>
     *
     * @param box the other provider box to merge (ignored if null or empty).
     */
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
