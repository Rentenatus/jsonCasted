/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus  
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsonpost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** 
 * @author Janusch Rentenatus
 */
public class PostJsonConnection {

    HttpURLConnection connection = null;
    final Object monitor = new Object();

    public void createHttpURLConnection(String url, int timeoutDelay) throws MalformedURLException, ProtocolException, IOException {
        createHttpURLConnection(url, timeoutDelay, "POST", true);
    }

    public void createHttpURLConnection(String url, int timeoutDelay, java.util.Map<String, String> headers) throws MalformedURLException, ProtocolException, IOException {
        createHttpURLConnection(url, timeoutDelay, "POST", true, headers);
    }

    /**
     * Creates the HTTP connection with an explicit method (POST | GET | ...).
     *
     * @param doOutput whether the connection allows a request body (POST) - GET connections read only
     */
    public void createHttpURLConnection(String url, int timeoutDelay, String method, boolean doOutput) throws MalformedURLException, ProtocolException, IOException {
        createHttpURLConnection(url, timeoutDelay, method, doOutput, null);
    }

    /**
     * Creates the HTTP connection with explicit method, body flag and extra headers
     * (e.g. Authorization: Bearer ... for hosted providers).
     */
    public void createHttpURLConnection(String url, int timeoutDelay, String method, boolean doOutput, java.util.Map<String, String> headers) throws MalformedURLException, ProtocolException, IOException {
        synchronized (monitor) {
            if (connection != null) {
                throw new IllegalStateException("Connection allredy exists.");
            }
            connection = (HttpURLConnection) new URL(url).openConnection();
            // Setzen des Timeouts
            connection.setReadTimeout(timeoutDelay);
            connection.setConnectTimeout(timeoutDelay);
            // Setzen der Methode
            connection.setRequestMethod(method);
            // Setzen des Inhaltstyps auf JSON
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            if (headers != null) {
                for (java.util.Map.Entry<String, String> h : headers.entrySet()) {
                    connection.setRequestProperty(h.getKey(), h.getValue());
                }
            }
            // Aktivieren Sie das Schreiben auf die Verbindung
            connection.setDoOutput(doOutput);
        }
    }

    public void write(String post) throws IOException {
        // Erstellen Sie einen Ausgabestream, um das JSON-Objekt an die Verbindung zu senden
        try ( OutputStream output = getOutputStream()) {
            // Schreiben Sie das JSON-Objekt als UTF-8-kodierten Text
            output.write(post.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String read() throws IOException {
        // bei non-2xx den Error-Body lesen, damit der Aufrufer die API-Fehlermeldung sieht
        int code;
        synchronized (monitor) {
            code = connection.getResponseCode();
        }
        if (code >= 400) {
            InputStream err;
            synchronized (monitor) {
                err = connection.getErrorStream();
            }
            if (err != null) {
                String body = new String(err.readAllBytes(), StandardCharsets.UTF_8);
                throw new IOException("HTTP " + code + ": " + body);
            }
            throw new IOException("HTTP " + code);
        }
        StringBuilder answer = null;
        String line;
        // Erstellen Sie einen Eingabestream, um die Antwort von der Verbindung zu lesen
        try ( InputStream input = getInputStream()) {
            // Erstellen Sie einen Eingabestream-Reader, um den Text zu lesen
            try ( InputStreamReader reader = new InputStreamReader(input, "UTF-8")) {
                // Erstellen Sie einen gepufferten Leser, um den Text zeilenweise zu lesen
                try ( BufferedReader buffer = new BufferedReader(reader)) {
                    // Lesen Sie die erste Zeile der Antwort
                    while ((line = buffer.readLine()) != null) {
                        if (answer == null) {
                            answer = new StringBuilder();
                        } else {
                            answer.append('\n');
                        }
                        answer.append(line);
                    }
                }
            }
        }
        return String.valueOf(answer);
    }

    protected OutputStream getOutputStream() throws IOException {
        synchronized (monitor) {
            return connection.getOutputStream();
        }
    }

    protected InputStream getInputStream() throws IOException {
        synchronized (monitor) {
            return connection.getInputStream();
        }
    }

    public void disconnect() {
        synchronized (monitor) {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }
    }

    public boolean isOpen() {
        synchronized (monitor) {
            return (connection != null);
        }
    }

}
