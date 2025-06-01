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
import de.jare.jsoncasted.parserwriter.JsonCastingLevel;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Janusch Rentenatus
 */
public interface JsonType {

    /**
     * Wenn keine geschweifte Klammern fuer ein Objekt gefunden wurden, dann
     * wird das Feld direkt vorgegeben. Eigentlich kann es sich dann nur um ein
     * Primitiv handeln.
     *
     * @return die JsonClass oder null.
     */
    public JsonClass getDirectClass();

    /**
     * Prueft, ob diese Klasse in diesem Typ zugelassen ist.
     *
     * @param check Vorschlag.
     * @return true, wenns passt.
     */
    public boolean contains(JsonClass check);

    public Object build(Iterator<JsonItem> listIterator, boolean asList, int size) throws JsonBuildException;

    public boolean isPrimitive();

    public String toString(Object attr);

    public String getcName();

    public Collection<?> asList(Object ob);

    public boolean needCast(JsonCastingLevel level);

    public default String setterPre(JsonType jType) {
        return jType.ownSetterPre();
    }

    public default String getterPre(JsonType jType) {
        return jType.ownGetterPre();
    }

    public default String ownSetterPre() {
        return "set";
    }

    public default String ownGetterPre() {
        return "get";
    }
}
