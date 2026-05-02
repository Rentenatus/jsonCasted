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
 *
 * @author Janusch Rentenatus
 */
public class JsonRepo implements JsonRepoEntity {

    private List<JsonRepoEntity> contents;
    private String repoName;

    public static JsonRepo emptyRepo() {
        JsonRepo ret = new JsonRepo();
        ret.setContents(new ArrayList<>());
        return ret;
    }

    public static JsonRepo copyRepo(List<JsonRepoEntity> contents) {
        JsonRepo ret = new JsonRepo();
        ret.setContents(new ArrayList<>(contents));
        return ret;
    }

    public JsonRepo() {
        this.contents = new ArrayList<>();
        this.repoName = "";
    }

    public JsonRepo(String repoName) {
        this.contents = new ArrayList<>();
        this.repoName = repoName;
    }

    public List<JsonRepoEntity> getContents() {
        return contents;
    }

    public void setContents(List<JsonRepoEntity> contents) {
        this.contents = contents;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

}
