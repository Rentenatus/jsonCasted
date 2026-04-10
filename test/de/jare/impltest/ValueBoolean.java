package de.jare.impltest;

public class ValueBoolean implements ValueInterface {

    private final Boolean frage;

    public ValueBoolean(Boolean frage) {
        this.frage = frage;
    }

    public Boolean getFrage() {
        return frage;
    }

    @Override
    public String getText() {
        return String.valueOf(frage);
    }

}
