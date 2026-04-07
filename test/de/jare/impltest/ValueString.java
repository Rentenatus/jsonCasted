package de.jare.impltest;

public class ValueString implements ValueInterface {

    private final String text;

    public ValueString(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

}
