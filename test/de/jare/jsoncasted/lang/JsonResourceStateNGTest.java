/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.lang;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * State model of doc/JSON_BUNDLE.md step 1 on the resource itself: fingerprint capture, consumption flag and
 * the FRESH/STALE detection - all computable for a standalone resource without any bundle around it.
 *
 * @author Janusch Rentenatus
 */
public class JsonResourceStateNGTest {

    private static Path tempDir;

    @BeforeClass
    public static void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("jsonbundle-state");
    }

    @AfterClass
    public static void removeTempDir() throws IOException {
        if (tempDir != null) {
            for (Path child : Files.list(tempDir).toList()) {
                Files.deleteIfExists(child);
            }
            Files.deleteIfExists(tempDir);
        }
    }

    private static JsonResource writeResource(String name, String content) throws IOException {
        final Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return JsonResource.forFile(file.toString());
    }

    @Test
    public void capturedFingerprintIsFresh() throws IOException {
        final JsonResource res = writeResource("fresh.json", "{\"a\":1}");
        res.captureFingerprint();

        assertTrue(res.getFingerprint() != null, "captured fingerprint expected");
        assertFalse(res.isStale(), "unchanged file must be FRESH");
        assertTrue(res.isFresh());
    }

    @Test
    public void changedFileIsStale() throws IOException {
        final JsonResource res = writeResource("stale.json", "{\"a\":1}");
        res.captureFingerprint();

        // size-change is the deterministic part of the stage 1 fingerprint
        Files.write(tempDir.resolve("stale.json"), "{\"a\":12345}".getBytes(StandardCharsets.UTF_8));

        assertTrue(res.isStale(), "changed file must be detected as STALE");
        assertFalse(res.isFresh());
    }

    @Test
    public void vanishedFileIsStale() throws IOException {
        final JsonResource res = writeResource("vanish.json", "{\"a\":1}");
        res.captureFingerprint();
        Files.delete(tempDir.resolve("vanish.json"));

        assertTrue(res.isStale(), "vanished origin file means the cache detached");
    }

    @Test
    public void inMemoryResourceStaysFresh() {
        final JsonResource res = JsonResource.forRoot(null);
        res.captureFingerprint();

        assertNull(res.getFingerprint(), "no file - no fingerprint");
        assertFalse(res.isStale(), "in-memory resources have no file state to detach from");
        assertTrue(res.isFresh());
    }

    @Test
    public void consumptionFlagStartsRegistered() {
        final JsonResource res = JsonResource.empty();

        assertFalse(res.isConsumed(), "a fresh parse is REGISTERED, not CONSUMED");
        res.markConsumed();
        assertTrue(res.isConsumed(), "first resolution use flips to CONSUMED");
    }

    @Test
    public void fingerprintEqualityIsAttributeEquality() throws IOException {
        final Path file = tempDir.resolve("equal.json");
        Files.write(file, "{\"a\":1}".getBytes(StandardCharsets.UTF_8));

        final ResourceFingerprint first = ResourceFingerprint.of(file.toFile());
        final ResourceFingerprint second = ResourceFingerprint.of(file.toFile());

        assertEquals(first, second, "same file state must produce equal fingerprints");
        assertFalse(first.differsFrom(second));
        assertEquals(first.hashCode(), second.hashCode());

        Files.write(file, "{\"a\":12}".getBytes(StandardCharsets.UTF_8));
        final ResourceFingerprint third = ResourceFingerprint.of(file.toFile());
        assertTrue(first.differsFrom(third), "size change must deviate");
        assertNotSame(first, third);
    }

    @Test
    public void fingerprintOfMissingFileIsNull() {
        assertNull(ResourceFingerprint.of(new File(tempDir.toFile(), "does-not-exist.json")));
    }

    @Test
    public void fingerprintAgainstNullCannotVerify() throws IOException {
        final JsonResource res = writeResource("nullcheck.json", "{\"a\":1}");
        res.captureFingerprint();

        assertTrue(res.getFingerprint().differsFrom(null), "one side unknown counts as deviating");
    }

    @Test
    public void setFingerprintRefreshesTheAxis() throws IOException {
        final JsonResource res = writeResource("refresh.json", "{\"a\":1}");
        res.captureFingerprint();
        Files.write(tempDir.resolve("refresh.json"), "{\"a\":12}".getBytes(StandardCharsets.UTF_8));
        assertTrue(res.isStale());

        res.captureFingerprint();
        assertFalse(res.isStale(), "re-capture declares the new file state as the load-time state");
        assertSame(res, res, "sanity: identity");
    }
}
