/*
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package de.jare.jsoncasted.io;

/**
 * Wood id level for serialization, the wood id counterpart of the {@link JsonCastingLevel}: it controls when the
 * writer emits wood id notations and in which form.
 * <p>
 * The levels govern:</p>
 * <ul>
 * <li>Whether the collection sharing policies ({@code withSameCollection}, {@code withEqualsCollection}) are honored
 * at all, or whether every list field writes its content inline</li>
 * <li>Whether object identities use the angle bracket notation ({@code <"id">}) or the {@code _woodObjectId} member
 * form</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 */
public enum JsonWoodIdLevel {

    /**
     * No wood id notation: the collection sharing policies are ignored, every list field writes its content inline -
     * a list that hangs at several fields appears duplicated. Object identities keep their
     * {@code _woodObjectId} member and {@code _woodLink} form: the member form is the strict JSON fallback the
     * references rely on, so it cannot be turned off without breaking the links.
     */
    NEVER(0),
    /**
     * Wood ids only where necessary: shared lists become {@code <"id">[...]} definitions with bare
     * {@code <"id">} references, referenced objects carry the {@code _woodObjectId} member. This is the default.
     */
    NECESSARY(5),
    /**
     * Like {@link #NECESSARY}, but object identities use the angle bracket notation too: definitions carry the
     * {@code <"id">} prefix in front of the object, references become the bare {@code <"id">} instead of the
     * {@code _woodObjectId} member. Lists behave exactly as with {@link #NECESSARY}.
     */
    PREFIXED(7);

    private final int key;

    /**
     * Constructor for JsonWoodIdLevel.
     *
     * @param key numeric identifier for the level (used for comparison).
     */
    JsonWoodIdLevel(int key) {
        this.key = key;
    }

    /**
     * Returns the numeric key of the level.
     *
     * @return the numeric identifier.
     */
    public int getKey() {
        return key;
    }
}
