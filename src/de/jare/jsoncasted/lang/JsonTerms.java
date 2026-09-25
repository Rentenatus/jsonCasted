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
 * <p>
 * These terms are used as reserved property names in JSON structures to convey type information, object identities, and
 * resource references.</p>
 *
 * @author Janusch Rentenatus
 */
public class JsonTerms {

    /**
     * Property name for explicit class type declaration.
     * <p>
     * Used in JSON: {@code "_class": "com.example.MyClass"}</p>
     */
    public static final String TERM_CLASS = "_class";

    /**
     * Property name for object identifier within a resource.
     * <p>
     * Used in JSON: {@code "_woodObjectId": "123456"}</p>
     */
    public static final String TERM_WOOD_OBJECT_ID = "_woodObjectId";

    /**
     * Property name for cross-resource reference link.
     * <p>
     * Used in JSON: {@code "_woodLink": "save::123456"}</p>
     */
    public static final String TERM_WOOD_LINK = "_woodLink";

    /**
     * Property name for resolver identifier used e.g. in cycle resolution.
     */
    public static final String TERM_RESOLVER_ID = "_resolverId";

    /**
     * Property name for cycle resolver identifier.
     */
    public static final String TERM_CYCLE_RESOLVER_ID = "_cycle_resolverId";

    /**
     * Property name for external resource provider definitions.
     * <p>
     * Used in JSON: {@code "_woodProviders": [...]}</p>
     */
    public static final String TERM_WOOD_PROVIDERS = "_woodProviders";

    /**
     * JsonModelDescription is itself model data. Because Wood Json Jack always deserializes a file into generic tree
     * nodes first, a save file may embed a special subtree such as _model that contains the model description.
     */
    public static final String TERM_WOOD_MODEL = "_woodModel";

    /**
     * Property name for definitions container.
     * <p>
     * Used in JSON: {@code "_woodDefinitions": {...}</p>
     */
    public static final String TERM_WOOD_DEFINITIONS = "_woodDefinitions";

    public static String DEFINITIONS_SUFFIX = "_def";

    public static final String SELF_SYNONYM = "self";
    public static final String THIS_SYNONYM = "this";
    public static final String COLONCOLON = "::";

    public static final String PREFIX_THIS = THIS_SYNONYM + COLONCOLON;
    public static final String PREFIX_SELF = SELF_SYNONYM + COLONCOLON;

    /**
     * Property name for model file reference.
     * <p>
     * Used in JSON: {@code "fileName": "path/to/model.json"}</p>
     */
    public static final String TERM_FILE_NAME = "fileName";
}
