package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class Null extends AbstractExpr {

    public Null() {
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("null");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // No children
    }

    @Override
    protected void prettyPrintChildren(java.io.PrintStream s, String prefix) {
        // No children
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        NullType nullType = new NullType(compiler.createSymbol("null"));
        this.setType(nullType);
        return nullType;
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        // The new Null is loaded in R0 as that is where variable assignations take values from
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0), "Loading null in R0");
    }
}
