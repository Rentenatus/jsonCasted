/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.wood.WoodProvider;
import de.jare.jsoncasted.wood.WoodProviderBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a complete JSON system with multiple resources and providers.
 *
 * <p>A JsonSystem manages a collection of {@link JsonResource} instances along with
 * their associated provider configurations. It serves as the top-level container for
 * JSON-based object graphs with cross-resource references.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Manages a main resource and additional imported resources</li>
 *   <li>Tracks root provider and provider box for resource resolution</li>
 *   <li>Provides lookup and management of resources by synonym</li>
 * </ul>
 */
public final class JsonSystem {

    private WoodProvider rootProvider;
    private WoodProviderBox providerBox;
    private JsonResource mainResource;
    private List<JsonResource> resources;
    private JsonModel mainModel;

    private JsonSystem() {
        this.resources = new ArrayList<>();
    }

    /**
     * Creates an empty JsonSystem with no resources.
     *
     * @return a new empty JsonSystem.
     */
    public static JsonSystem empty() {
        return new JsonSystem();
    }

    /**
     * Creates a JsonSystem with the specified main resource.
     *
     * @param mainResource the primary JSON resource.
     * @return a new JsonSystem with the main resource set.
     */
    public static JsonSystem of(JsonResource mainResource) {
        return of(mainResource.getExpectedBox(), mainResource);
    }

    /**
     * Creates a JsonSystem with the specified provider box and main resource.
     *
     * @param providerBox the provider box for resource resolution.
     * @param mainResource the primary JSON resource.
     * @return a new JsonSystem configured with provider box and main resource.
     */
    public static JsonSystem of(WoodProviderBox providerBox, JsonResource mainResource) {
        JsonSystem system = new JsonSystem();
        system.setProviderBox(providerBox != null ? providerBox : new WoodProviderBox(new ArrayList<>()));
        List<JsonResource> resources = new ArrayList<>();
        resources.add(mainResource);
        system.setResources(resources);
        system.setMainResource(mainResource);
        if (mainResource != null && !system.resources.contains(mainResource)) {
            system.resources.add(0, mainResource);
        }
        return system;
    }

    /**
     * Returns the root provider for this system.
     *
     * @return the root provider, or {@code null} if not set.
     */
    public WoodProvider getRootProvider() {
        return rootProvider;
    }

    /**
     * Sets the root provider for this system.
     *
     * @param rootProvider the root provider to set.
     */
    public void setRootProvider(WoodProvider rootProvider) {
        this.rootProvider = rootProvider;
    }

    /**
     * Returns the provider box containing all available providers.
     *
     * @return the provider box, or {@code null} if not set.
     */
    public WoodProviderBox getProviderBox() {
        return providerBox;
    }

    /**
     * Sets the provider box for this system.
     *
     * @param providerBox the provider box to set.
     */
    public void setProviderBox(WoodProviderBox providerBox) {
        this.providerBox = providerBox;
    }

    /**
     * Returns the main resource of this system.
     *
     * @return the main resource, or {@code null} if not set.
     */
    public JsonResource getMainResource() {
        return mainResource;
    }

    /**
     * Sets the main resource for this system.
     *
     * @param mainResource the main resource to set.
     */
    public void setMainResource(JsonResource mainResource) {
        this.mainResource = mainResource;
    }

    /**
     * Returns an unmodifiable list of all resources in this system.
     *
     * @return unmodifiable list of resources.
     */
    public List<JsonResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Sets the list of resources for this system.
     *
     * @param resources the list of resources to set, or {@code null} to clear.
     */
    public void setResources(List<JsonResource> resources) {
        if (resources == null) {
            this.resources = new ArrayList<>();
        } else {
            this.resources = new ArrayList<>(resources);
        }
    }

    /**
     * Finds a resource by its provider synonym.
     *
     * @param synonym the provider synonym to search for.
     * @return the matching resource, or {@code null} if not found.
     */
    public JsonResource findResourcesBySynonym(String synonym) {
        for (JsonResource resource : resources) {
            String provider = resource.getProviderName();
            if (provider.equals(synonym)) {
                return resource;
            }
        }
        return null;
    }

    /**
     * Adds a resource to this system.
     *
     * @param resource the resource to add (must not be null).
     * @throws NullPointerException if resource is null.
     */
    public void addResource(JsonResource resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        resources.add(resource);
    }

    /**
     * Checks if this system has a root provider set.
     *
     * @return {@code true} if root provider is set, {@code false} otherwise.
     */
    public boolean hasRootProvider() {
        return rootProvider != null;
    }

    /**
     * Checks if this system has a main resource set.
     *
     * @return {@code true} if main resource is set, {@code false} otherwise.
     */
    public boolean hasMainResource() {
        return mainResource != null;
    }

    /**
     * Checks if this system has a provider box set.
     *
     * @return {@code true} if provider box is set, {@code false} otherwise.
     */
    public boolean hasProviderBox() {
        return providerBox != null;
    }

    /**
     * Returns the number of resources in this system.
     *
     * @return the resource count.
     */
    public int size() {
        return resources.size();
    }

    /**
     * Returns the main JsonModel for this system.
     *
     * @return the main model, or {@code null} if not set.
     */
    public JsonModel getMainModel() {
        return mainModel;
    }

    /**
     * Sets the main JsonModel for this system.
     *
     * @param mainModel the main model to set.
     */
    public void setMainModel(JsonModel mainModel) {
        this.mainModel = mainModel;
    }

    /**
     * Returns the appropriate model for a given provider synonym.
     * If a repository model is registered for the synonym, it is returned.
     * Otherwise, the main model is returned as fallback.
     *
     * @param synonym the provider synonym to look up.
     * @return the JsonModel for the synonym, or the main model if not found.
     */
    public JsonModel getModelForSynonym(String synonym) {
        if (mainModel == null) {
            return null;
        }
        return mainModel.getModelForSynonym(synonym);
    }

}
