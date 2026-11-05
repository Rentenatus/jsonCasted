/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The project document (concept: CONCEPT_ProjectView.md, section 1). It lives as
 * {@code .wood/project.json} in the project root folder, is itself plain JSON and therefore a
 * first-class editing candidate of the Wood editor.
 *
 * @author Janusch Rentenatus
 */
public class ProjectDocument {

    private String name;
    private List<Partition> roots;

    public ProjectDocument() {
        this.roots = new ArrayList<>();
    }

    public ProjectDocument(String name) {
        this.name = name;
        this.roots = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Partition> getRoots() {
        if (roots == null) {
            roots = new ArrayList<>();
        }
        return roots;
    }

    public void setRoots(List<Partition> roots) {
        this.roots = roots;
    }

    public Partition findPartition(String partitionName) {
        if (partitionName == null) {
            return null;
        }
        for (Partition partition : getRoots()) {
            if (partitionName.equals(partition.getName())) {
                return partition;
            }
        }
        return null;
    }

}
