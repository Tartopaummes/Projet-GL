package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class This extends AbstractExpr {

    private boolean addDuringSyntaxAnalysis;

    public This(boolean addDuringSyntaxAnalysis) {
        this.addDuringSyntaxAnalysis = addDuringSyntaxAnalysis;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        if (!isImplicit()) {
            s.print("this");
        }
    }

    @Override
    boolean isImplicit() {
        return addDuringSyntaxAnalysis;
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
        if (currentClass == null) {
            throw new ContextualError("'this' used outside of a class. Rule 3.43.", getLocation());
        }
        ClassType classType = currentClass.getType();
        this.setType(classType);
        return classType;
    }

    @Override
    String prettyPrintNode() {
        return "This (" + addDuringSyntaxAnalysis + ")";
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Loading the address of the current object (this)");
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R0)); // this is always at offset -2
    }


}
