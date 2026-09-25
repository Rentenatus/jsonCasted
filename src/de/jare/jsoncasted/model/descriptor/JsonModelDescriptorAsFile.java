/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.descriptor;

import de.jare.jsoncasted.tools.SimpleStringSplitter;
import java.util.Arrays;
import java.util.Objects;

/**
 * Description as file link.
 *
 *
 * @author Janusch Rentenatus
 */
public class JsonModelDescriptorAsFile implements SimpleStringSplitter {

    private final String modelName;
    private final String fileName;

    /**
     * Constructs a model descriptor with the specified model name.
     *
     * @param modelName the name of the model (must not be null).
     * @param filelName
     */
    public JsonModelDescriptorAsFile(String modelName, String filelName) {
        this.modelName = Objects.requireNonNull(modelName, "modelName");
        this.fileName = simpleReplace(
                Objects.requireNonNull(filelName, "filelName"),
                Arrays.asList("\\", "\\\\"),
                "/");
    }

    /**
     * Returns the model name associated with this descriptor.
     *
     * @return The model name.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Returns the file name associated with this descriptor.
     *
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "JsonModelDescriptor[modelName=" + modelName
                + ", fileName=" + fileName + "]";
    }
}
