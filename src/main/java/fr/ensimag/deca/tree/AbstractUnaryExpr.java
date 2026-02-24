package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Unary expression.
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractUnaryExpr extends AbstractExpr {

    public AbstractExpr getOperand() {
        return operand;
    }
    private AbstractExpr operand;
    public AbstractUnaryExpr(AbstractExpr operand) {
        Validate.notNull(operand);
        this.operand = operand;
    }


    protected abstract String getOperatorName();
  
    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        s.print(getOperatorName());
        getOperand().decompile(s);
        s.print(")");
    }

    @Override
    public void transformSSAInst(AbstractBloc block) {
        // For a unary expression, transform the operand of the operation if it isn't an identifier
        // If it is, replace it with the corresponding SSAVariable.
        if (operand.isIdentifier()) {
            operand = block.getLastUsedVar(((Identifier) operand).getName());
        } else {
            operand.transformSSAInst(block);
        }
    }

    @Override
    public void transformSSAInstLoop(AbstractBloc block) {
        // For a unary expression, transform the operand of the operation if it isn't an identifier
        // If it is, replace it with the corresponding SSAVariable.
        if (operand.isIdentifier()) {
            operand = block.getLastUsedVar(((Identifier) operand).getName());
        } else {
            operand.transformSSAInstLoop(block);
        }
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        operand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        operand.prettyPrint(s, prefix, true);
    }


    public void codeGenPrint(DecacCompiler compiler){
        this.codeGenInst(compiler);
        Type type = operand.getType();
        if (type.isFloat()){
            FloatType.codeGenPrint(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }

    public void codeGenPrintHex(DecacCompiler compiler){
        Type type = operand.getType();
        if (type.isFloat()){
            FloatType.codeGenPrintHex(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }
}
