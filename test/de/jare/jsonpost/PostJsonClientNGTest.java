/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the raw variants of {@link PostJsonClient} (postRaw / getRaw) against a local
 * JDK HttpServer - no external network needed. This is the seam the WJJ harness uses for
 * its local ollama calls ({@code /api/generate} POST, {@code /api/tags} GET probe).
 *
 * @author Janusch Rentenatus
 */
public class PostJsonClientNGTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeClass
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            byte[] body = exchange.getRequestBody().readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/tags", exchange -> {
            byte[] body = "{\"models\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterClass
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void testPostRawEchoesBody() throws Exception {
        PostJsonClient client = new PostJsonClient();
        String answer = client.postRaw("{\"model\":\"qwen2.5:3b\",\"prompt\":\"hi\"}",
                baseUrl + "/echo", 5000);
        Assert.assertEquals(answer, "{\"model\":\"qwen2.5:3b\",\"prompt\":\"hi\"}");
    }

    @Test
    public void testGetRaw() throws Exception {
        PostJsonClient client = new PostJsonClient();
        String answer = client.getRaw(baseUrl + "/tags", 5000);
        Assert.assertEquals(answer, "{\"models\":[]}");
    }

    @Test
    public void testPostRawToClosedPortFails() {
        PostJsonClient client = new PostJsonClient();
        try {
            client.postRaw("{}", "http://127.0.0.1:1/echo", 1000);
            Assert.fail("expected IOException for a closed port");
        } catch (IOException expected) {
            // ok - the caller (e.g. the harness provider) marks the outage
        }
    }
}
