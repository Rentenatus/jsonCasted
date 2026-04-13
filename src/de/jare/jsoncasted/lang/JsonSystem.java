/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.jsoncasted.wood.WoodProvider;
import de.jare.jsoncasted.wood.WoodProviderBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class JsonSystem {

    private WoodProvider rootProvider;
    private WoodProviderBox providerBox;
    private JsonResource mainResource;
    private List<JsonResource> resources;

    private JsonSystem() {
        this.resources = new ArrayList<>();
    }

    public static JsonSystem empty() {
        return new JsonSystem();
    }

    public static JsonSystem of(JsonResource mainResource) {
        JsonSystem system = new JsonSystem();
        system.setMainResource(mainResource);
        system.resources.add(mainResource);
        return system;
    }

    public static JsonSystem of(WoodProviderBox providerBox, JsonResource mainResource, List<JsonResource> resources) {
        JsonSystem system = new JsonSystem();
        system.setProviderBox(providerBox);
        system.setResources(resources);
        system.setMainResource(mainResource);
        if (mainResource != null && !system.resources.contains(mainResource)) {
            system.resources.add(0, mainResource);
        }
        return system;
    }

    public WoodProvider getRootProvider() {
        return rootProvider;
    }

    public void setRootProvider(WoodProvider rootProvider) {
        this.rootProvider = rootProvider;
    }

    public WoodProviderBox getProviderBox() {
        return providerBox;
    }

    public void setProviderBox(WoodProviderBox providerBox) {
        this.providerBox = providerBox;
    }

    public JsonResource getMainResource() {
        return mainResource;
    }

    public void setMainResource(JsonResource mainResource) {
        this.mainResource = mainResource;
    }

    public List<JsonResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    public void setResources(List<JsonResource> resources) {
        if (resources == null) {
            this.resources = new ArrayList<>();
        } else {
            this.resources = new ArrayList<>(resources);
        }
    }

    public void addResource(JsonResource resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        resources.add(resource);
    }

    public boolean hasRootProvider() {
        return rootProvider != null;
    }

    public boolean hasMainResource() {
        return mainResource != null;
    }

    public boolean hasProviderBox() {
        return providerBox != null;
    }

    public int size() {
        return resources.size();
    }
}
