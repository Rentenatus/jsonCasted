/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository container for storing collections of {@link JsonRepoEntity} instances.
 * <p>
 * This class represents a named repository that can hold multiple entities
 * and is used for organizing and managing external JSON resources.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public class JsonRepo implements JsonRepoEntity {

    private List<JsonRepoEntity> contents;
    private String repoName;

    /**
     * Creates an empty JsonRepo instance with empty contents and empty name.
     *
     * @return A new empty JsonRepo instance.
     */
    public static JsonRepo emptyRepo() {
        JsonRepo ret = new JsonRepo();
        ret.setContents(new ArrayList<>());
        return ret;
    }

    /**
     * Creates a JsonRepo instance with the specified contents.
     *
     * @param contents The list of entities to store in the repository.
     * @return A new JsonRepo instance with the specified contents.
     */
    public static JsonRepo copyRepo(List<JsonRepoEntity> contents) {
        JsonRepo ret = new JsonRepo();
        ret.setContents(new ArrayList<>(contents));
        return ret;
    }

    /**
     * Constructs a JsonRepo with empty contents and empty name.
     */
    public JsonRepo() {
        this.contents = new ArrayList<>();
        this.repoName = "";
    }

    /**
     * Constructs a JsonRepo with the specified name and empty contents.
     *
     * @param repoName The name of the repository.
     */
    public JsonRepo(String repoName) {
        this.contents = new ArrayList<>();
        this.repoName = repoName;
    }

    /**
     * Returns the list of entities stored in this repository.
     *
     * @return The list of JsonRepoEntity instances.
     */
    public List<JsonRepoEntity> getContents() {
        return contents;
    }

    /**
     * Sets the list of entities for this repository.
     *
     * @param contents The list of JsonRepoEntity instances to store.
     */
    public void setContents(List<JsonRepoEntity> contents) {
        this.contents = contents;
    }

    /**
     * Returns the name of this repository.
     *
     * @return The repository name.
     */
    public String getRepoName() {
        return repoName;
    }

    /**
     * Sets the name of this repository.
     *
     * @param repoName The name to assign to the repository.
     */
    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

}
