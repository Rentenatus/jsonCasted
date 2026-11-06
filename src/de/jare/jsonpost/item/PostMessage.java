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
 * One chat-style message of a POST payload: role + content. The role is a free string -
 * the usual set is {@code system} (system prompt), {@code user} (user prompt),
 * {@code assistant} (assistant answer, may carry {@code tool_calls}) and {@code tool}
 * (tool result, carries {@code tool_call_id}). This is the shape shared by ollama's
 * {@code /api/chat} and hosted chat-completions APIs.
 *
 * @author Janusch Rentenatus
 */
public class PostMessage {

    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_TOOL = "tool";

    private String role;
    private String content;
    private List<PostToolCall> toolCalls = new ArrayList<>();  // assistant messages
    private String toolCallId;                                  // tool-role messages
    private String name;                                        // tool name for tool-role messages

    public PostMessage() {
    }

    public PostMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<PostToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<PostToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
