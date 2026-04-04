package de.jare.jsoncasted.wood;

import java.util.Objects;

public final class WoodProvider {

    private final String synonym;
    private final String filename;



    public WoodProvider(String synonym, String filename ) {
        this.synonym = requireText(synonym, "synonym");
        this.filename =  requireText(filename, "filename");
    }

    public String getSynonym() {
        return synonym;
    }

    public String getFilename() {
        return filename;
    }


    public boolean matchesSynonym(String value) {
        return synonym.equals(value);
    }

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

        return  synonym.equals(other.synonym)
                && filename.equals(other.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(synonym, filename );
    }
}
