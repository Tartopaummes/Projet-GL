package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.UnaryInstructionImmInt;

/**
 * @author Ensimag
 * @date 01/01/2025
 */
public class TSTO extends UnaryInstructionImmInt {
    public TSTO(ImmediateInteger i) {
        super(i);
    }
    private int value = 0;

    public TSTO(int i) {
        super(i);
    }

    public void increment(int memo){
        value += memo;
    }

    public void done(){
        operand = new ImmediateInteger(value);
    }
}
