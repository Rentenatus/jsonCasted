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
import de.jare.jsonpost.item.PostToolCall;
import de.jare.jsonpost.item.PostToolDef;
import de.jare.jsonpost.item.PostToolFunction;

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

        JsonClass toolFunctionClass = model.newJsonReflect(PostToolFunction.class);
        toolFunctionClass.addField("name", asString);
        toolFunctionClass.addField("description", asString);
        toolFunctionClass.addField("parameters", asString);

        JsonClass toolDefClass = model.newJsonReflect(PostToolDef.class);
        toolDefClass.addField("type", asString);
        toolDefClass.addField("function", toolFunctionClass);

        JsonClass toolCallClass = model.newJsonReflect(PostToolCall.class);
        toolCallClass.addField("id", asString);
        toolCallClass.addField("type", asString);
        toolCallClass.addField("name", asString);
        toolCallClass.addField("arguments", asString);

        messageClass = model.newJsonReflect(PostMessage.class);
        messageClass.addField("role", asString);
        messageClass.addField("content", asString);
        // list field with custom getter/setter names: construct the JsonField directly
        messageClass.add(new de.jare.jsoncasted.model.item.JsonField("tool_calls", toolCallClass, LIST,
                "getToolCalls", "setToolCalls", de.jare.jsoncasted.io.JsonValidationMethod.NONE));
        messageClass.addField("tool_call_id", asString, "getToolCallId", "setToolCallId");
        messageClass.addField("name", asString);

        chatRequestRoot = model.newJsonReflect(PostChatRequest.class);
        chatRequestRoot.addField("model", asString);
        chatRequestRoot.addField("messages", messageClass, LIST);
        chatRequestRoot.addField("tools", toolDefClass, LIST);
        chatRequestRoot.addField("tool_choice", asString, "getToolChoice", "setToolChoice");
        chatRequestRoot.addField("stream", asBoolean);
        chatRequestRoot.addField("format", asString);
        chatRequestRoot.addField("response_format", asString, "getResponseFormat", "setResponseFormat");

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
