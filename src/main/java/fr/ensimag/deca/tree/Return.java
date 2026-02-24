package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BRA;

import java.io.PrintStream;

/**
 * Return Statement.
 *
 * @author gl34
 * @date 01/01/2025
 */

public class Return extends AbstractInst {

    private AbstractExpr returnExpr;
    private AbstractIdentifier methodOwnerName;

    public Return(AbstractExpr returnExpr) {
        this.returnExpr = returnExpr;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType, AbstractIdentifier methodOwnerName) throws ContextualError {
        if (returnType.isVoid()) {
            throw new ContextualError("Return statement in a void method. Rule 3.24.", getLocation());
        }
        this.methodOwnerName = methodOwnerName;

        returnExpr = returnExpr.verifyRValue(compiler, localEnv, currentClass, returnType);
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Generating code for return value");
        returnExpr.codeGenInst(compiler);
        compiler.addInstruction(new BRA(new Label("end." + methodOwnerName.getMethodDefinition().getDeclClass().getName().getName().getName() + "." + methodOwnerName.getName().getName())));
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        returnExpr.decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        returnExpr.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        returnExpr.iter(f);
    }
}
