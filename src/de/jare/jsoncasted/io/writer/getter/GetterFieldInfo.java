/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.io.writer.getter;

import de.jare.jsoncasted.model.item.JsonField;

/**
 *
 * @author Jansuch Rentenatus
 */
public class GetterFieldInfo {

    private final String fieldName;
    private final JsonField jsonField;
    private final Object attribute;

    public GetterFieldInfo(String fieldName, JsonField jsonField, Object attribute) {
        this.fieldName = fieldName;
        this.jsonField = jsonField;
        this.attribute = attribute;
    }

    public String getFieldName() {
        return fieldName;
    }

    public JsonField getJsonField() {
        return jsonField;
    }

    public Object getAttribute() {
        return attribute;
    }

}
