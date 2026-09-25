/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.item.builder.JsonBuilder;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.io.JsonItemDefinition;
import de.jare.jsoncasted.io.JsonParseException;
import de.jare.jsoncasted.io.JsonParser;
import de.jare.jsoncasted.io.JsonObjectWriter;
import de.jare.jsoncasted.io.JsonWriteException;
import de.jare.jsoncasted.io.convertservice.WoodResolution;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple client for posting JSON requests to a server and receiving JSON responses. It handles serialization and
 * deserialization of JSON objects based on provided definitions and classes.
 *
 * @author Janusch Rentenatus
 */
public class PostJsonClient {

    /**
     * Posts a JSON request to the specified URL and returns the deserialized response object.
     *
     * @param request The request object to be serialized and sent.
     * @param url The URL to which the request is posted.
     * @param timeoutDelay The timeout delay for the HTTP request.
     * @param definition The JSON item definition for serialization and deserialization.
     * @param writeClass The JSON class for writing the request.
     * @param readClass The JSON class for reading the response.
     * @return The deserialized response object.
     * @throws JsonBuildException
     * @throws IOException
     */
    public Object post(Object request, String url, int timeoutDelay, JsonItemDefinition definition, final JsonClass writeClass, final JsonClass readClass) throws JsonBuildException, IOException {
        String answer = post(request, url, timeoutDelay, definition, writeClass);
        return buildObject(answer, definition, readClass);
    }

    /**
     * Posts a JSON request to the specified URL and returns the raw JSON response as a string.
     *
     * @param request The request object to be serialized and sent.
     * @param url The URL to which the request is posted.
     * @param timeoutDelay The timeout delay for the HTTP request.
     * @param definition The JSON item definition for serialization and deserialization.
     * @param writeClass The JSON class for writing the request.
     * @return The raw JSON response as a string.
     * @throws JsonBuildException
     * @throws IOException
     */
    public String post(Object request, String url, int timeoutDelay, JsonItemDefinition definition, final JsonClass writeClass) throws JsonBuildException, IOException {
        String post = "{}";
        boolean hasWrite = request != null && writeClass != null;
        if (hasWrite) try {
            post = JsonObjectWriter.writeToString(request, definition, writeClass);
        } catch (JsonParseException | IOException | JsonWriteException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }

        // Erstellen Sie eine URL, die auf den lokalen Server zeigt
        String answer = "";
        // Erstellen Sie eine HTTP-Verbindung zu der URL
        final PostJsonConnection connectionGuard = new PostJsonConnection();
        try {
            connectionGuard.createHttpURLConnection(url, timeoutDelay);
            if (hasWrite) {
                connectionGuard.write(post);
                answer = connectionGuard.read();
            } else {
                answer = "Can't write request.";
            }
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception ex) {
            throw new JsonBuildException("Exception occurred: " + ex.getMessage(), ex);
        } finally {
            // Schließen Sie die Verbindung 
            connectionGuard.disconnect();
        }
        return answer;
    }

    /**
     * Builds an object from the JSON response string using the provided definition and read class.
     *
     * @param answer The JSON response string to be deserialized.
     * @param definition The JSON item definition for deserialization.
     * @param readClass The JSON class for reading the response.
     * @return The deserialized object.
     * @throws JsonBuildException
     * @throws IOException
     */
    public Object buildObject(String answer, JsonItemDefinition definition, final JsonClass readClass) throws JsonBuildException, IOException {
        JsonItem obj = null;
        try {
            WoodResolution reso = JsonParser.parse(answer, definition.getDescriptor(), readClass.getcName());
            if (reso.hasExceptions()) {
                final List<JsonParseException> exceptions = reso.getUnmodifiableExceptions();
                for (Exception exception : exceptions) {
                    Logger.getGlobal().log(Level.SEVERE, "Parsing error: ", exception);
                }
            }
            obj = reso.getAnswer();
        } catch (JsonParseException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Error caught during parsing: ", ex);
        }
        return JsonBuilder.buildInstance(definition.getModel(), true, obj);
    }

}
