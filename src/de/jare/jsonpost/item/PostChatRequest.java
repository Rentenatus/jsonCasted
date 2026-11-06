/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.item;

import java.util.ArrayList;
import java.util.List;

/**
 * A chat-style POST request: model + ordered message list + stream flag + optional JSON
 * format pin (e.g. {@code format: "json"} for ollama, or a response_format object for
 * hosted providers - kept as a raw string here, the def layer maps it).
 *
 * @author Janusch Rentenatus
 */
public class PostChatRequest {

    private String model;
    private List<PostMessage> messages = new ArrayList<>();
    private boolean stream;
    private String format;
    private String responseFormat;   // raw JSON for hosted providers: {"type":"json_object"}

    public PostChatRequest() {
    }

    public PostChatRequest(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<PostMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<PostMessage> messages) {
        this.messages = messages;
    }

    public PostChatRequest addMessage(String role, String content) {
        messages.add(new PostMessage(role, content));
        return this;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }
}
