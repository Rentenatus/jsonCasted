package de.jare.jsoncasted.lang;

import java.util.Objects;

/**
 * Kontextinformationen zu einer Exception innerhalb eines JsonNode-Baums.
 */
public class JsonExceptionEntry {

    private final JsonNode ownerNode;
    private final String path;
    private final Exception exception;

    public JsonExceptionEntry(JsonNode ownerNode, String path, Exception exception) {
        this.ownerNode = ownerNode;
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.exception = Objects.requireNonNull(exception, "exception must not be null");
    }

    public JsonNode getOwnerNode() {
        return ownerNode;
    }

    public String getPath() {
        return path;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "JsonExceptionEntry{"
                + "path='" + path + '\''
                + ", exception=" + exception
                + '}';
    }
}
