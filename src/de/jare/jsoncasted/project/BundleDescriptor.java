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
    private List<BundleImport> imports;

    public BundleDescriptor() {
        this.files = new ArrayList<>();
        this.imports = new ArrayList<>();
    }

    public BundleDescriptor(String name, String main) {
        this.name = name;
        this.main = main;
        this.files = new ArrayList<>();
        this.files.add(main);
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

}
