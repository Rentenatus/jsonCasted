/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.project;

import java.io.File;
import java.io.IOException;

/**
 * A project partition: a named, typed mount of a physical folder on the first level of the project
 * (concept: CONCEPT_ProjectView.md, section 2). Mounts exist only on level 1; the partition is a
 * virtual view (link semantics), never a copy.
 *
 * @author Janusch Rentenatus
 */
public class Partition {

    private String name;
    private PartitionType type;
    private String path;
    private Boolean readonly;

    public Partition() {
        // JSON reflect construction.
    }

    public Partition(String name, PartitionType type, String path, Boolean readonly) {
        this.name = name;
        this.type = type;
        this.path = path;
        this.readonly = readonly;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartitionType getType() {
        return type;
    }

    public void setType(PartitionType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getReadonly() {
        return readonly;
    }

    public void setReadonly(Boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Effective readonly of the mount (concept section 2, the role table): the explicit
     * {@code readonly} flag wins; without one the role decides - {@code gen} is generator
     * output meant to be read (default readonly), {@code target} is build output wiped by
     * builds (readonly) and {@code isolated} sources are view-only by definition (an old
     * version to look at - writable on disk by the user, never saved into from here). The
     * other roles default to editable.
     *
     * @return true when no editor action saves into this mount
     */
    public boolean isReadonly() {
        if (readonly != null) {
            return readonly;
        }
        return type == PartitionType.GEN || type == PartitionType.TARGET
                || type == PartitionType.ISOLATED;
    }

    /**
     * Resolves the physical location of this mount. Relative paths resolve against the project
     * root folder, absolute paths are external mounts and pass through unchanged.
     *
     * @param projectRoot the folder that contains .wood/project.json
     * @return the mounted folder, or null when the path is not set
     */
    public File resolve(File projectRoot) {
        if (path == null || path.isBlank()) {
            return null;
        }
        final File candidate = new File(path);
        if (candidate.isAbsolute()) {
            return candidate;
        }
        String cleaned = path;
        while (cleaned.startsWith("./") || cleaned.startsWith(".\\")) {
            cleaned = cleaned.substring(2);
        }
        return cleaned.isEmpty() ? projectRoot : new File(projectRoot, cleaned);
    }

    /**
     * Mount identity key: the canonical location of the resolved folder. Two partitions mounting
     * the same physical folder collide on this key and are refused at load time.
     *
     * @param projectRoot the folder that contains .wood/project.json
     * @return canonical path when resolvable, otherwise the plain absolute path
     */
    public String mountKey(File projectRoot) {
        final File folder = resolve(projectRoot);
        if (folder == null) {
            return "";
        }
        try {
            return folder.getCanonicalPath();
        } catch (IOException ex) {
            return folder.getAbsolutePath();
        }
    }

}
