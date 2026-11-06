/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.item;

/**
 * The function part of a tool definition or tool call: name + description + the JSON
 * schema of the arguments (parameters) as raw JSON string.
 *
 * @author Janusch Rentenatus
 */
public class PostToolFunction {

    private String name;
    private String description;
    private String parameters;   // raw JSON schema string, e.g. {"type":"object","properties":{...}}

    public PostToolFunction() {
    }

    public PostToolFunction(String name, String description, String parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
}
