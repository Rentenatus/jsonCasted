/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost;

import de.jare.jsoncasted.item.JsonItem;
import de.jare.jsoncasted.model.JsonBuildException;
import de.jare.jsoncasted.model.item.JsonClass;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import de.jare.jsoncasted.parserwriter.JsonParser;
import de.jare.jsoncasted.parserwriter.JsonWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import de.jare.jsoncasted.parserwriter.JsonItemDefinition;

/**
 *
 * @author Janusch Rentenatus
 */
public class PostJsonClient {

    public Object post(Object request, String url, int timeoutDelay, JsonItemDefinition definition, final JsonClass writeClass, final JsonClass readClass) throws JsonBuildException, IOException {
        String answer = post(request, url, timeoutDelay, definition, writeClass);
        return buildObject(answer, definition, readClass);
    }

    public String post(Object request, String url, int timeoutDelay, JsonItemDefinition definition, final JsonClass writeClass) throws JsonBuildException, IOException {
        String post = "{}";
        boolean hasWrite = request != null && writeClass != null;
        if (hasWrite) try {
            post = JsonWriter.writeToString(request, definition, writeClass);
        } catch (JsonParseException | IOException ex) {
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

    public Object buildObject(String answer, JsonItemDefinition definition, final JsonClass readClass) throws JsonBuildException, IOException {

        JsonItem obj = null;
        try {

            obj = JsonParser.parse(answer, definition, readClass);
        } catch (JsonParseException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }

        return obj.buildInstance();
    }

}
