/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.impltest;

/**
 * Interface for value objects that can provide text representation.
 * All test value classes implement this interface.
 *
 * @author Janusch Rentenatus
 */
public interface ValueInterface {

    /**
     * Returns the text representation of this value.
     *
     * @return The string representation of the value.
     */
    public String getText();

}
