/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.model;

import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.item.JsonItem;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Janusch Rentenatus
 */
public interface JsonModellClassBuilder {

    // public static final String NOT_LIST = "This Class is not a list.";
    public Class<?> getSingularClass();

    public Object build(JsonClass jClass, JsonItem jsonItem) throws JsonBuildException;

    public Object buildList(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException;

    public Object buildArray(JsonType jType, Iterator<JsonItem> listIterator, int size) throws JsonBuildException;

    public default boolean isPrimitive() {
        return true;
    }

    public String toString(Object attr);

    public List<?> asList(Object ob);

    public default String setterPre() {
        return "set";
    }

    public default String getterPre() {
        return "get";
    }
}
