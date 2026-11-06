/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost;

import com.sun.net.httpserver.HttpServer;
import de.jare.jsonpost.def.JsonPostDefinition;
import de.jare.jsonpost.item.PostChatRequest;
import de.jare.jsonpost.item.PostChatResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the definition-driven chat path (jsonpost.item + jsonpost.def): a
 * {@link PostChatRequest} bean is serialized by the {@link JsonPostDefinition} model and
 * posted, the stub server answers a chat-style JSON which is parsed back into a
 * {@link PostChatResponse} bean.
 *
 * @author Janusch Rentenatus
 */
public class PostJsonChatNGTest {

    private HttpServer server;
    private String baseUrl;
    private volatile String lastRequestBody;

    @BeforeClass
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String resp = "{\"model\":\"qwen2.5:3b\",\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"decision\\\":\\\"worked\\\"}\"},\"done\":true}";
            byte[] out = resp.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, out.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(out);
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
    public void testDefinitionValidates() {
        Assert.assertFalse(JsonPostDefinition.INSTANCE.validateDefinition().hasErrors());
    }

    @Test
    public void testPostChatRoundTrip() throws Exception {
        PostChatRequest request = new PostChatRequest("qwen2.5:3b")
                .addMessage("system", "You answer strict JSON.")
                .addMessage("user", "Say worked.");
        request.setFormat("json");
        request.setStream(false);

        PostChatResponse response = new PostJsonClient().postChat(request, baseUrl + "/api/chat", 5000);

        Assert.assertNotNull(lastRequestBody);
        Assert.assertTrue(lastRequestBody.contains("\"model\": \"qwen2.5:3b\""), "request model serialized: " + lastRequestBody);
        Assert.assertTrue(lastRequestBody.contains("\"role\": \"user\""), "messages serialized");
        Assert.assertTrue(lastRequestBody.contains("\"format\": \"json\""), "format pin serialized");
        Assert.assertEquals(response.getMessage().getRole(), "assistant");
        Assert.assertEquals(response.getMessage().getContent(), "{\"decision\":\"worked\"}");
        Assert.assertTrue(response.isDone());
        Assert.assertEquals(response.getModel(), "qwen2.5:3b");
    }
}
