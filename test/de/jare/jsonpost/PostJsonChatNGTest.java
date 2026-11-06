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
import de.jare.jsonpost.item.PostMessage;
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
    private volatile String lastAuthHeader;

    @BeforeClass
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/chat", exchange -> {
            lastRequestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastAuthHeader = exchange.getRequestHeaders().getFirst("Authorization");
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

    @Test
    public void testPostChatWithHeadersAndResponseFormat() throws Exception {
        PostChatRequest req = new PostChatRequest("mistral-small").addMessage("user", "hi");
        req.setResponseFormat("{\"type\":\"json_object\"}");
        java.util.Map<String, String> headers = java.util.Map.of("Authorization", "Bearer test-key");
        PostChatResponse response = new PostJsonClient().postChat(req, baseUrl + "/api/chat", 5000, headers);

        Assert.assertTrue(lastRequestBody.contains("response_format") && lastRequestBody.contains("json_object"),
                "response_format serialized as raw JSON: " + lastRequestBody);
        Assert.assertEquals(lastAuthHeader, "Bearer test-key", "Authorization header sent");
        Assert.assertEquals(response.getMessage().getRole(), "assistant");
    }

    @Test
    public void testToolMessagesAndToolCallsSerialize() throws Exception {
        // the full tool-loop message list: system prompt, user prompt, assistant with
        // tool_calls, tool result message - plus the tools catalog on the request
        PostChatRequest req = new PostChatRequest("qwen2.5:3b")
                .addMessage(PostMessage.ROLE_SYSTEM, "You answer strict JSON.")
                .addMessage(PostMessage.ROLE_USER, "Read a.txt");
        PostMessage assistant = new PostMessage(PostMessage.ROLE_ASSISTANT, null);
        assistant.getToolCalls().add(new de.jare.jsonpost.item.PostToolCall("call_1", "fs.read", "{\"path\":\"a.txt\"}"));
        req.addMessage(assistant);
        PostMessage tool = new PostMessage(PostMessage.ROLE_TOOL, "hello");
        tool.setToolCallId("call_1");
        tool.setName("fs.read");
        req.addMessage(tool);
        req.addTool(new de.jare.jsonpost.item.PostToolDef(
                new de.jare.jsonpost.item.PostToolFunction("fs.read", "read a file", "{\"type\":\"object\"}")));
        req.setToolChoice("auto");

        new PostJsonClient().postChat(req, baseUrl + "/api/chat", 5000);

        Assert.assertTrue(lastRequestBody.contains("\"role\": \"system\""), "system prompt message");
        Assert.assertTrue(lastRequestBody.contains("\"role\": \"user\""), "user prompt message");
        Assert.assertTrue(lastRequestBody.contains("\"role\": \"assistant\""), "assistant message");
        Assert.assertTrue(lastRequestBody.contains("\"role\": \"tool\""), "tool result message");
        Assert.assertTrue(lastRequestBody.contains("tool_calls"), "tool_calls serialized");
        Assert.assertTrue(lastRequestBody.contains("call_1"), "tool call id + tool_call_id serialized");
        Assert.assertTrue(lastRequestBody.contains("\"tools\": ["), "tools catalog serialized");
        Assert.assertTrue(lastRequestBody.contains("fs.read"), "tool function name serialized");
        Assert.assertTrue(lastRequestBody.contains("tool_choice"), "tool_choice serialized");
    }
}
