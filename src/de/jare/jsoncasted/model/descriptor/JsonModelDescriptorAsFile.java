/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor;

import java.util.Objects;

/**
 * Description as file link.
 *
 *
 * @author Janusch Rentenatus
 */
public class JsonModelDescriptorAsFile {

    private final String modelName;
    private final String filelName;

    /**
     * Constructs a model descriptor with the specified model name.
     *
     * @param modelName the name of the model (must not be null).
     * @param filelName
     */
    public JsonModelDescriptorAsFile(String modelName, String filelName) {
        this.modelName = Objects.requireNonNull(modelName, "modelName");
        this.filelName = Objects.requireNonNull(filelName, "filelName");
    }

    public String getModelName() {
        return modelName;
    }

    public String getFilelName() {
        return filelName;
    }

    @Override
    public String toString() {
        return "JsonModelDescriptor[modelName=" + modelName
                + ", filelName=" + filelName + "]";
    }
}
