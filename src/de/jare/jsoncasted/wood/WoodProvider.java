/* <copyright>
 * Copyright (C) 2026, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.wood;

import java.util.Objects;

/**
 * Represents a JSON resource provider with a synonym and filename.
 *
 * <p>A WoodProvider defines an external JSON resource that can be referenced
 * from other JSON files using the synonym as a namespace.</p>
 *
 * <p>Example in JSON:</p>
 * <pre>{@code
 * {
 *   "_woodProviders": [
 *     {"synonym": "save", "filename": "./data/config.json"}
 *   ]
 * }
 * </pre>
 *
 * <p>References can then use the synonym: {@code "_woodLink": "save::123456"}</p>
 */
public final class WoodProvider {

    private final String synonym;
    private final String filename;

    /**
     * Constructs a WoodProvider with the specified synonym and filename.
     *
     * @param synonym the namespace synonym for references (must not be null or blank).
     * @param filename the path to the JSON file (must not be null or blank).
     * @throws NullPointerException if synonym or filename is null.
     * @throws IllegalArgumentException if synonym or filename is blank.
     */
    public WoodProvider(String synonym, String filename) {
        this.synonym = requireText(synonym, "synonym");
        this.filename = requireText(filename, "filename");
    }

    /**
     * Returns the synonym for this provider.
     *
     * @return the namespace synonym.
     */
    public String getSynonym() {
        return synonym;
    }

    /**
     * Returns the filename for this provider.
     *
     * @return the path to the JSON file.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Checks if the given value matches this provider's synonym.
     *
     * @param value the value to check.
     * @return {@code true} if the value matches the synonym, {@code false} otherwise.
     */
    public boolean matchesSynonym(String value) {
        return synonym.equals(value);
    }

    /**
     * Validates and trims a text value.
     *
     * @param value the value to validate.
     * @param fieldName the name of the field for error messages.
     * @return the trimmed value.
     * @throws NullPointerException if value is null.
     * @throws IllegalArgumentException if value is blank.
     */
    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    @Override
    public String toString() {
        return "WoodProvider{" 
                + "synonym='" + synonym + '\''
                + ", filename='" + filename + '\''
                + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WoodProvider other)) {
            return false;
        }

        return synonym.equals(other.synonym) 
                && filename.equals(other.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(synonym, filename);
    }
}
