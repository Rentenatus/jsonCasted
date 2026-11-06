/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.item;

/**
 * A chat-style POST response: the assistant {@link PostMessage} plus done/model bookkeeping.
 *
 * @author Janusch Rentenatus
 */
public class PostChatResponse {

    private PostMessage message;
    private boolean done;
    private String model;

    public PostMessage getMessage() {
        return message;
    }

    public void setMessage(PostMessage message) {
        this.message = message;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
