/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.impltest.lib;

/**
 * Test bean for the wood id level of object identities: two object fields that may carry the same shared node.
 *
 * @author Janusch Rentenatus
 */
public class SharedRefBox {

    private SharedNode left;
    private SharedNode right;

    public SharedNode getLeft() {
        return left;
    }

    public void setLeft(SharedNode left) {
        this.left = left;
    }

    public SharedNode getRight() {
        return right;
    }

    public void setRight(SharedNode right) {
        this.right = right;
    }

}
