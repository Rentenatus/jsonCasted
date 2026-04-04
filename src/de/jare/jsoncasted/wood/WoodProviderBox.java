package de.jare.jsoncasted.wood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class WoodProviderBox {

    private final List<WoodProvider> providers;

    public WoodProviderBox(List<WoodProvider> providers) {
        Objects.requireNonNull(providers, "providers must not be null");
        this.providers = Collections.unmodifiableList(new ArrayList<>(providers));
    }

    public List<WoodProvider> getProviders() {
        return providers;
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

    @Override
    public String toString() {
        return "WoodProviderBox{"
                + "providers=" + providers
                + '}';
    }
}
