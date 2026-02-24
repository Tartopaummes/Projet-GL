package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import org.apache.commons.lang.Validate;

/**
 * Print statement (print, println, ...).
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType, AbstractIdentifier methodOwnerName)
            throws ContextualError {
        for (AbstractExpr expr : getArguments().getList()) { // For each argument in the list
            Type returnTypeExpr = expr.verifyExpr(compiler, localEnv, currentClass);
            //Contextual error (rule 3.31) : Can print only type int, float or string
            if (returnTypeExpr != compiler.environmentType.INT && returnTypeExpr != compiler.environmentType.FLOAT && returnTypeExpr != compiler.environmentType.STRING) {
                throw new ContextualError("Can't print type " + returnTypeExpr + ". Type must be int, float or string. (Rule 3.31)", expr.getLocation());
            }
        }
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        for (AbstractExpr expr : getArguments().getList()) {
            compiler.addComment("Preparation of the print instruction, expression evaluation");
            expr.codeGenInst(compiler);
            compiler.addInstruction(new LOAD(Register.R0, Register.R1));
            compiler.addComment("Actual print instruction");
            if(printHex) {
                expr.codeGenPrintHex(compiler);
            } else {
                expr.codeGenPrint(compiler);
            }
        }
    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {

        if (printHex) {
            s.print("print" + this.getSuffix() + "x(");
        } else {
            s.print("print" + this.getSuffix() + "(");
        }
        arguments.decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
