/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model.item;

import de.jare.jsoncasted.model.JsonType;
import de.jare.jsoncasted.model.descriptor.JsonModelDescriptor;
import de.jare.jsoncasted.model.descriptor.JsonTypeDescriptor;

/**
 *
 *
 * @author Janusch Rentenatus
 */
public interface JsonInter extends JsonType {

    public Iterable<JsonClass> iterable();

    public JsonTypeDescriptor describeHeadInterface(JsonModelDescriptor context);

}
