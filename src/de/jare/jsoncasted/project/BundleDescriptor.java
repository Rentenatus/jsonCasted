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
 * The bundle descriptor (concept: CONCEPT_ProjectView.md, section 4). It lives as
 * {@code .woodbundle.json} inside a bundle folder, carries only relative file names (the
 * descriptor stays valid wherever the folder moves) and declares the main file - the entry
 * point a double-click opens.
 *
 * @author Janusch Rentenatus
 */
public class BundleDescriptor {

    private String name;
    private String main;
    private List<String> files;
    private List<String> protectedFiles;
    private List<BundleImport> imports;

    public BundleDescriptor() {
        this.files = new ArrayList<>();
        this.protectedFiles = new ArrayList<>();
        this.imports = new ArrayList<>();
    }

    public BundleDescriptor(String name, String main) {
        this.name = name;
        this.main = main;
        this.files = new ArrayList<>();
        this.files.add(main);
        this.protectedFiles = new ArrayList<>();
        this.imports = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public List<String> getFiles() {
        if (files == null) {
            files = new ArrayList<>();
        }
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public List<BundleImport> getImports() {
        if (imports == null) {
            imports = new ArrayList<>();
        }
        return imports;
    }

    public void setImports(List<BundleImport> imports) {
        this.imports = imports;
    }

    public boolean hasFile(String fileName) {
        return fileName != null && getFiles().contains(fileName);
    }

    /**
     * The protected files of this bundle (concept CONCEPT_ProjectView.md, phase 20): a
     * temporary read-only fence per file - the user guards a resource against their own quick
     * fingers and against assistants working through the editor. Read-only in the OPENING
     * sense: the file opens as a read-only view until the fence is removed. Traveled inside
     * the descriptor on purpose - the protection is part of the bundle's shared truth, not
     * of a user's local state.
     *
     * @return the protected file names (relative, like files), never null
     */
    public List<String> getProtected() {
        if (protectedFiles == null) {
            protectedFiles = new ArrayList<>();
        }
        return protectedFiles;
    }

    public void setProtected(List<String> protectedFiles) {
        this.protectedFiles = protectedFiles;
    }

    /**
     * @param fileName the relative file name in question
     * @return true when the file opens read-only (the fence is set)
     */
    public boolean isProtected(String fileName) {
        return fileName != null && getProtected().contains(fileName);
    }

    /**
     * Sets the read-only fence, a repeated call is a no-op. The validator demands a protected
     * file to be part of the files list - the fence guards a declared resource.
     *
     * @param fileName the relative file name to protect
     */
    public void protect(String fileName) {
        if (fileName != null && !isProtected(fileName)) {
            getProtected().add(fileName);
        }
    }

    /**
     * Removes the read-only fence.
     *
     * @param fileName the relative file name to release
     */
    public void unprotect(String fileName) {
        getProtected().remove(fileName);
    }

}
