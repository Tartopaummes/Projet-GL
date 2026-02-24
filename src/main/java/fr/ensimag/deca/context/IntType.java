package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.instructions.WINT;

/**
 *
 * @author Ensimag
 * @date 01/01/2025
 */
public class IntType extends Type {

    public IntType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        return otherType.isInt() || otherType.isFloat();
    }

    @Override
    public boolean exactSameType(Type otherType) {
        return otherType.isInt();
    }

    public static void codeGenPrint(DecacCompiler compiler){
        compiler.addInstruction(new WINT());
    }


}
