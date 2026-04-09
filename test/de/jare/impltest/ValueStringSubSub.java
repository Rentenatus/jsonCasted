package de.jare.impltest;

public class ValueStringSubSub extends ValueStringSub implements ValueInterface {

    private final Boolean frage;

    public ValueStringSubSub(String text, Boolean frage) {
        super(text);
        this.frage = frage;
    }

    public Boolean getFrage() {
        return frage;
    }

    @Override
    public String getText() {
        return super.getText() + " = " + String.valueOf(frage);
    }
}
