package fr.ensimag.ima.pseudocode;

public class ImmediateBoolean extends DVal {
    private int value;

    public ImmediateBoolean(boolean value) {
        super();
        if (value){
            this.value = 1;
        } else {
            this.value = 0;
        }
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
