/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Janusch Renteantus
 */
public record WriteNodePath(String path, List<Object> ids) {

    public WriteNodePath append(String... text) {
        StringBuilder sb = new StringBuilder(path);
        for (String item : text) {
            sb.append(item);
        }
        return new WriteNodePath(sb.toString(), ids);
    }

    public WriteNodePath appendId(long... id) {
        ArrayList<Object> idList = new ArrayList<>(ids);
        for (long item : id) {
            idList.add(item);
        }
        return new WriteNodePath(path, idList);
    }

    public WriteNodePath appendOb(Object... id) {
        ArrayList<Object> idList = new ArrayList<>(ids);
        for (Object item : id) {
            idList.add(item);
        }
        return new WriteNodePath(path, idList);
    }

    @Override
    public String toString() {
        return path;
    }

}
