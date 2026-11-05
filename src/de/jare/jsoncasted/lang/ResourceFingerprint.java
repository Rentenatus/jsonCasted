/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Fingerprint of a resource's origin file, capture at load time (concept doc/JSON_BUNDLE.md, state model):
 * every reuse of a registered resource verifies the fingerprint against the file to detect a change under the
 * cache. Stage 1 compares {@code {FileTime mtime, size}} - cheap and exact enough for the registry use; the
 * interface is the upgrade path for a content digest stage (CRC32) against touch/re-save false alarms.
 *
 * <p>
 * Semantics: both fingerprints null (in-memory, never captured) means "nothing known, nothing detected". A
 * null on exactly one side means "cannot verify" and counts as differing. Fingerprint equality of stage 1 is
 * attribute equality, never content identity.
 * </p>
 *
 * @author Janusch Rentenatus
 */
public interface ResourceFingerprint {

    /**
     * Captures the stage 1 fingerprint of the file: last modification time and size.
     *
     * @param file the origin file, not null.
     * @return the fingerprint, or null when the file does not exist or its attributes are unreadable.
     */
    static ResourceFingerprint of(File file) {
        try {
            final BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            return new FileFingerprint(attrs.lastModifiedTime().toMillis(), attrs.size());
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Tells whether the two fingerprints deviate - the cached parse instance no longer maps its origin file.
     *
     * @param other the fingerprint to compare against, may be null.
     * @return true when the fingerprints deviate (also when one side is null and the other is not).
     */
    boolean differsFrom(ResourceFingerprint other);

    /**
     * Stage 1 fingerprint: last modification time and size of the file.
     */
    final class FileFingerprint implements ResourceFingerprint {

        private final long lastModified;
        private final long size;

        private FileFingerprint(long lastModified, long size) {
            this.lastModified = lastModified;
            this.size = size;
        }

        @Override
        public boolean differsFrom(ResourceFingerprint other) {
            if (!(other instanceof FileFingerprint that)) {
                return true;
            }
            return this.lastModified != that.lastModified || this.size != that.size;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FileFingerprint that)) {
                return false;
            }
            return this.lastModified == that.lastModified && this.size == that.size;
        }

        @Override
        public int hashCode() {
            return Objects.hash(lastModified, size);
        }

        @Override
        public String toString() {
            return "FileFingerprint{mtime=" + lastModified + ", size=" + size + '}';
        }
    }
}
