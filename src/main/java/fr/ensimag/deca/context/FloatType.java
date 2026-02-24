package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;

/**
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class FloatType extends Type {

    public FloatType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        return otherType.isFloat();
    }

    public static void codeGenPrint(DecacCompiler compiler){
        compiler.addInstruction(new WFLOAT());
    }

    public static void codeGenPrintHex(DecacCompiler compiler){
        compiler.addInstruction(new WFLOATX());
    }


}
