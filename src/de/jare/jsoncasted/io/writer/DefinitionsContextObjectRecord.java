/*
 * Copyright (c) 2026 Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io.writer;

import java.util.Objects;

/**
 *
 * @author Janusch Renteantus
 */
public class DefinitionsContextObjectRecord {

    public enum Disposition {
        UNKNOWN,
        FINDING,
        CANDIDATE,
        ASSIGNED
    }
    private final Object object;
    private long localId = -1;
    private Object container = null;
    private boolean isContainer = false;

    // In welches Repository soll die Definition?
    private String repositoryKey = null;

    private Disposition disposition = Disposition.UNKNOWN;

    public DefinitionsContextObjectRecord(Object object) {
        this.object = object;
    }

    public DefinitionsContextObjectRecord(Object object, long localId) {
        this.object = object;
        this.localId = localId;
    }

    public Object getObject() {
        return object;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public Object geContainer() {
        return container;
    }

    public void setContainer(Object container) {
        this.container = Objects.requireNonNull(container);
        this.disposition = Disposition.ASSIGNED;
    }

    public String getRepositoryKey() {
        return repositoryKey;
    }

    public void setRepositoryKey(String repositoryKey) {
        this.repositoryKey = repositoryKey;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public boolean isCandidate() {
        return disposition == Disposition.CANDIDATE;
    }

    public boolean isFinding() {
        return disposition == Disposition.FINDING;
    }

    public boolean isAssigned() {
        return disposition == Disposition.ASSIGNED;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public void setDisposition(Disposition disposition) {
        if (this.disposition == Disposition.ASSIGNED && disposition != Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Objects.requireNonNull(disposition);
    }

    public void asFinding() {
        if (this.disposition == Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Disposition.FINDING;
    }

    public void asCandidate() {
        if (this.disposition == Disposition.ASSIGNED) {
            throw new IllegalArgumentException("Once assigned, objects cannot be unlinked.");
        }
        this.disposition = Disposition.CANDIDATE;
    }

    public void asContainer() {
        this.isContainer = true;
    }
}
