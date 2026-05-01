package de.jare.jsoncasted.lang;

import java.util.Objects;

/**
 * Context information for an exception that occurred within a JsonNode tree.
 *
 * <p>This class captures the node where the exception occurred, the path to that node,
 * and the exception itself, providing detailed context for error reporting and debugging.</p>
 */
public class JsonExceptionEntry {

    private final JsonNode ownerNode;
    private final String path;
    private final Exception exception;

    /**
     * Constructs a JsonExceptionEntry with the node, path, and exception.
     *
     * @param ownerNode the JsonNode where the exception occurred (may be null).
     * @param path the path to the node where the exception occurred (must not be null).
     * @param exception the exception that occurred (must not be null).
     * @throws NullPointerException if path or exception is null.
     */
    public JsonExceptionEntry(JsonNode ownerNode, String path, Exception exception) {
        this.ownerNode = ownerNode;
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.exception = Objects.requireNonNull(exception, "exception must not be null");
    }

    /**
     * Returns the JsonNode where the exception occurred.
     *
     * @return the owner node, or {@code null} if not available.
     */
    public JsonNode getOwnerNode() {
        return ownerNode;
    }

    /**
     * Returns the path to the node where the exception occurred.
     *
     * @return the path string.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the exception that occurred.
     *
     * @return the exception.
     */
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
