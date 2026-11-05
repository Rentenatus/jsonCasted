/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

/**
 * One import entry of a bundle descriptor (concept: CONCEPT_ProjectView.md, section 6). The two
 * channels of the Maven analogy: a {@code path} import is a reactor reference (live, project
 * local), a {@code version} import is a dependency (frozen materialized copy from the local
 * bundle repository). Exactly one of both must be set.
 *
 * @author Janusch Rentenatus
 */
public class BundleImport {

    private String bundle;
    private String path;
    private String version;

    public BundleImport() {
        // JSON reflect construction.
    }

    public BundleImport(String bundle, String path, String version) {
        this.bundle = bundle;
        this.path = path;
        this.version = version;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return true when this entry is a reactor import (path set, resolves live inside the
     * project)
     */
    public boolean isReactor() {
        return path != null && !path.isBlank() && (version == null || version.isBlank());
    }

    /**
     * @return true when this entry is a dependency import (version pinned, materializes as a
     * deep copy from the local repository)
     */
    public boolean isDependency() {
        return version != null && !version.isBlank() && (path == null || path.isBlank());
    }

    /**
     * @return true when the channel is unambiguous (exactly one of path/version set, or the
     * plain naming import with none of both)
     */
    public boolean hasChannel() {
        return isReactor() || isDependency();
    }

}
