/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.model.JsonCollectionType;
import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.parserwriter.JsonValidationMethod;

/**
 *
 * @author Janusch Rentenatus
 */
public class JsonCParam extends JsonField {

    public JsonCParam(String fName, JsonType jType, JsonCollectionType colType, String getter, String setter) {
        super(fName, jType, colType, getter, setter, JsonValidationMethod.NONE);
    }

    public JsonCParam(String fName, JsonType jType, String getter, String setter) {
        super(fName, jType, JsonCollectionType.NONE, getter, setter, JsonValidationMethod.NONE);
    }

    public JsonCParam(JsonType parent, String fName, JsonType jType, JsonCollectionType type, String getterSetterNorm) {
        super(parent, fName, jType, type, getterSetterNorm, JsonValidationMethod.NONE);
    }

    public JsonCParam(JsonType parent, String fName, JsonType jType, String getterSetterNorm) {
        super(parent, fName, jType, JsonCollectionType.NONE, getterSetterNorm, JsonValidationMethod.NONE);
    }

    public JsonCParam(JsonType parent, String fName, JsonType jType, JsonCollectionType type) {
        super(parent, fName, jType, type, JsonValidationMethod.NONE);

    }

    public JsonCParam(JsonType parent, String fName, JsonType jType) {
        super(parent, fName, jType, JsonCollectionType.NONE, JsonValidationMethod.NONE);
    }

    @Override
    public boolean isConstructorParam() {
        return true;
    }
}
