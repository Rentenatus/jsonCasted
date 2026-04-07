 
package de.jare.impltest;

public class ValueInteger implements ValueInterface {
    
    private final Integer zahl;
    
    public ValueInteger(Integer zahl) {
        this.zahl = zahl;
    }

    public Integer getZahl() {
        return zahl;
    }
    
    @Override
    public String getText() {
        return String.valueOf(zahl);
    }
    
}
