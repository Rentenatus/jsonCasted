/* <copyright>
 * Copyright (c) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost.def;

import static de.jare.jsoncasted.model.JsonCollectionType.LIST;

import de.jare.jsoncasted.io.JsonCastingLevel;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.model.JsonModel;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsonpost.item.PostChatRequest;
import de.jare.jsonpost.item.PostChatResponse;
import de.jare.jsonpost.item.PostMessage;

/**
 * The model definition of the POST interface (package {@code jsonpost.def}): describes the
 * chat-style payload items ({@link PostMessage}, {@link PostChatRequest},
 * {@link PostChatResponse}) as a JsonModel, so {@link de.jare.jsonpost.PostJsonClient} can
 * serialize requests and parse responses definition-driven - the same way WJJ parses its
 * settings. Usable for ollama {@code /api/chat} and hosted chat-completions alike.
 *
 * @author Janusch Rentenatus
 */
public class JsonPostDefinition implements JsonItemDefinition {

    public static final JsonPostDefinition INSTANCE = new JsonPostDefinition();

    private final JsonModel model;
    private final JsonClass messageClass;
    private final JsonClass chatRequestRoot;
    private final JsonClass chatResponseRoot;

    public JsonPostDefinition() {
        model = new JsonModel("Post");
        model.addBasicModel();

        final JsonClass asString = model.getJsonClass("String");
        final JsonClass asBoolean = model.getJsonClass("Boolean");

        messageClass = model.newJsonReflect(PostMessage.class);
        messageClass.addField("role", asString);
        messageClass.addField("content", asString);

        chatRequestRoot = model.newJsonReflect(PostChatRequest.class);
        chatRequestRoot.addField("model", asString);
        chatRequestRoot.addField("messages", messageClass, LIST);
        chatRequestRoot.addField("stream", asBoolean);
        chatRequestRoot.addField("format", asString);

        chatResponseRoot = model.newJsonReflect(PostChatResponse.class);
        chatResponseRoot.addField("message", messageClass);
        chatResponseRoot.addField("done", asBoolean);
        chatResponseRoot.addField("model", asString);

        model.setRootNodeCast(chatRequestRoot.getcName());
    }

    @Override
    public JsonModel getModel() {
        return model;
    }

    public JsonClass getChatRequestRoot() {
        return chatRequestRoot;
    }

    public JsonClass getChatResponseRoot() {
        return chatResponseRoot;
    }

    public JsonClass getMessageClass() {
        return messageClass;
    }

    @Override
    public JsonCastingLevel getCastingLevel() {
        return JsonCastingLevel.NEVER;
    }
}
