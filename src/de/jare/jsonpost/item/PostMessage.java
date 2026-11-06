/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.item;

/**
 * One chat-style message of a POST payload: role + content (e.g. "system"/"user"/"assistant").
 * This is the shape shared by ollama's {@code /api/chat} and hosted chat-completions APIs.
 *
 * @author Janusch Rentenatus
 */
public class PostMessage {

    private String role;
    private String content;

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
}
