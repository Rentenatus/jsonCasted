/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

/**
 * Constants for special JSON property names used in the jsonCasted and Wood Json Jack systems.
 *
 * <p>These terms are used as reserved property names in JSON structures to convey
 * type information, object identities, and resource references.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonTerms {

    /**
     * Property name for explicit class type declaration.
     * <p>Used in JSON: {@code "_class": "com.example.MyClass"}</p>
     */
    public static final String TERM_CLASS = "_class";

    /**
     * Property name for object identifier within a resource.
     * <p>Used in JSON: {@code "_woodObjectId": "123456"}</p>
     */
    public static final String TERM_WOOD_OBJECT_ID = "_woodObjectId";

    /**
     * Property name for cross-resource reference link.
     * <p>Used in JSON: {@code "_woodLink": "save::123456"}</p>
     */
    public static final String TERM_WOOD_LINK = "_woodLink";

    /**
     * Property name for external resource provider definitions.
     * <p>Used in JSON: {@code "_woodProviders": [...]}</p>
     */
    public static final String TERM_WOOD_PROVIDERS = "_woodProviders";
}
