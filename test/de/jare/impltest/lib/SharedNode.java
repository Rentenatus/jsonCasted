/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.impltest.lib;

/**
 * Test bean for the wood id level of object identities: a plain node that several fields can reference.
 *
 * @author Janusch Rentenatus
 */
public class SharedNode {

    private String text;

    public SharedNode() {
    }

    public SharedNode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
