/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.item;

/**
 * One tool call of an assistant message ({@code tool_calls}: [{id, type, function:{name, arguments}}]).
 * The arguments stay a raw JSON string - the model decides the shape.
 *
 * @author Janusch Rentenatus
 */
public class PostToolCall {

    private String id;
    private String type = "function";
    private String name;         // flattened for simple APIs; function.name in full shape
    private String arguments;    // raw JSON string

    public PostToolCall() {
    }

    public PostToolCall(String id, String name, String arguments) {
        this.id = id;
        this.name = name;
        this.arguments = arguments;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getArguments() { return arguments; }
    public void setArguments(String arguments) { this.arguments = arguments; }
}
