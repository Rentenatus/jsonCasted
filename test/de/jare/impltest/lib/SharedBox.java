/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-20.html
 */
package de.jare.impltest.lib;

import java.util.List;

/**
 * Test bean for the wood list sharing: two list fields that may carry the same shared list identity.
 *
 * @author Janusch Rentenatus
 */
public class SharedBox {

    private List<String> left;
    private List<String> right;

    public List<String> getLeft() {
        return left;
    }

    public void setLeft(List<String> left) {
        this.left = left;
    }

    public List<String> getRight() {
        return right;
    }

    public void setRight(List<String> right) {
        this.right = right;
    }

}
