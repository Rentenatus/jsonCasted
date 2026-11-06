/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.item;

/**
 * One entry of the request's {@code tools} list: {@code type: "function"} + the function
 * schema (name, description, parameters).
 *
 * @author Janusch Rentenatus
 */
public class PostToolDef {

    private String type = "function";
    private PostToolFunction function;

    public PostToolDef() {
    }

    public PostToolDef(PostToolFunction function) {
        this.function = function;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public PostToolFunction getFunction() { return function; }
    public void setFunction(PostToolFunction function) { this.function = function; }
}
