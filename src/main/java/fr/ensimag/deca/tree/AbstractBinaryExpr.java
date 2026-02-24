package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import org.apache.commons.lang.Validate;

/**
 * Binary expressions.
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractBinaryExpr extends AbstractExpr {

    public AbstractExpr getLeftOperand() {
        return leftOperand;
    }

    public AbstractExpr getRightOperand() {
        return rightOperand;
    }

    protected void setLeftOperand(AbstractExpr leftOperand) {
        Validate.notNull(leftOperand);
        this.leftOperand = leftOperand;
    }

    protected void setRightOperand(AbstractExpr rightOperand) {
        Validate.notNull(rightOperand);
        this.rightOperand = rightOperand;
    }

    private AbstractExpr leftOperand;
    private AbstractExpr rightOperand;

    public AbstractBinaryExpr(AbstractExpr leftOperand,
            AbstractExpr rightOperand) {
        Validate.notNull(leftOperand, "left operand cannot be null");
        Validate.notNull(rightOperand, "right operand cannot be null");
        Validate.isTrue(leftOperand != rightOperand, "Sharing subtrees is forbidden");
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(" " + getOperatorName() + " ");
        getRightOperand().decompile(s);
        s.print(")");
    }

    @Override
    public void transformSSAInst(AbstractBloc block) {
        // For a binary expression, transform both operands of the operation if they aren't identifiers
        // If they are, replace them with the corresponding SSAVariables.
        if (leftOperand.isIdentifier()) {
            leftOperand = block.getLastUsedVar(((AbstractIdentifier) leftOperand).getName());
        } else {
            leftOperand.transformSSAInst(block);
        }

        if (rightOperand.isIdentifier()) {
            rightOperand = block.getLastUsedVar(((AbstractIdentifier) rightOperand).getName());
        } else {
            rightOperand.transformSSAInst(block);
        }
    }

    @Override
    public void transformSSAInstLoop(AbstractBloc block) {
        // For a binary expression, transform both operands of the operation if they aren't identifiers
        // If they are, replace them with the corresponding SSAVariables.
        // this funciton is a second pass for the while loop, but have the same purpose as the one over, except for assign
        if (leftOperand.isIdentifier()) {
            //System.out.println("Transforming left operand of " + getOperatorName() + " to SSA var " +((leftOperand.isSSAVariable()) ? (SSAVariable) leftOperand : (Identifier) leftOperand).getName().getName() + " at " + getLocation());
            leftOperand = block.getLastUsedVar(((leftOperand.isSSAVariable()) ? (SSAVariable) leftOperand : (Identifier) leftOperand).getName());
            //System.out.println("Left operand is now " + leftOperand);
        } else {
            leftOperand.transformSSAInstLoop(block);
        }

        if (rightOperand.isIdentifier()) {
            rightOperand = block.getLastUsedVar(((AbstractIdentifier) rightOperand).getName());
        } else {
            rightOperand.transformSSAInstLoop(block);
        }
    }

    abstract protected String getOperatorName();

    @Override
    protected void iterChildren(TreeFunction f) {
        leftOperand.iter(f);
        rightOperand.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        leftOperand.prettyPrint(s, prefix, false);
        rightOperand.prettyPrint(s, prefix, true);
    }

    /**
     * Converts the operands to float types if one of the two operands is a float and the other is an int.
     */
    protected void harmonizeOperandTypes(AbstractBinaryExpr expr, DecacCompiler compiler,
                                         EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type leftType = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type rightType = getRightOperand().verifyExpr(compiler, localEnv, currentClass);


        if (leftType.isInt() && rightType.isFloat()) {
            AbstractExpr converted = new ConvFloat(getLeftOperand());
            // Set the type of the converted expression to FLOAT
            converted.setType(compiler.environmentType.FLOAT);
            getRightOperand().setType(compiler.environmentType.FLOAT);
            setLeftOperand(converted);

        } else if (leftType.isFloat() && rightType.isInt()) {
            AbstractExpr converted = new ConvFloat(getRightOperand());
            // Set the type of the converted expression to the expected type
            getLeftOperand().setType(compiler.environmentType.FLOAT);
            converted.setType(compiler.environmentType.FLOAT);
            setRightOperand(converted);
        }
    }

    /**
     * same code for every binary expression, this function set left and right operand values
     * to R0 and R1, so the function using this will have the values ready to be use
     * @param compiler the compiler used to compile this piece of code
     * @return the maximum number of register needed to do the calculation (i.e. useful for TSTO)
     */
    public void codeGenBinaryExp(DecacCompiler compiler){
        compiler.addComment("Code for binary expression: " + getOperatorName());
        compiler.addComment("Getting left operand");
        getLeftOperand().codeGenInst(compiler); //left operand evaluation
        //Store left operant value, in a register if possible, PUSH if not
        boolean pushed = (compiler.getMinFreeRegister() >= compiler.getCompilerOptions().getNbRegisters());
        if (pushed){
            compiler.addInstruction(new PUSH(Register.R0), "Push left operand value");
            compiler.incrementTSTO(1);
        } else {
            compiler.addInstruction(new LOAD(Register.R0, Register.getR(compiler.getMinFreeRegister())), "Load left operand value");
            compiler.setMinFreeRegister(compiler.getMinFreeRegister() + 1);
        }
        compiler.addComment("Getting right operand");
        getRightOperand().codeGenInst(compiler);

        //left operand value move to register 1
        compiler.addInstruction(new LOAD(Register.R0, Register.R1), "Move left operand value to R1");
        if (pushed){
            compiler.addInstruction(new POP(Register.R0), "Pop result to R0");
        } else {
            compiler.setMinFreeRegister(compiler.getMinFreeRegister() - 1);
            compiler.addInstruction(new LOAD(Register.getR(compiler.getMinFreeRegister()), Register.R0), "Load result to R0");
        }

        compiler.addComment("Finalizing binary expression " + getOperatorName() + ", loading result into R0");
    }

    public void codeGenPrint(DecacCompiler compiler){
        Type left = leftOperand.getType();
        Type right = rightOperand.getType();
        if (left.isFloat() || right.isFloat()){
            FloatType.codeGenPrint(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }

    public void codeGenPrintHex(DecacCompiler compiler){
        Type left = leftOperand.getType();
        Type right = rightOperand.getType();
        if (left.isFloat() || right.isFloat()){
            FloatType.codeGenPrintHex(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }


    protected void codeGenOverflowError(DecacCompiler compiler, Type exprType) {
        if (!compiler.getCompilerOptions().getNo_check() && !exprType.isInt()) {
            compiler.addInstruction(new BOV(new Label("overflow_error")), "Overflow check for previous operation");
        }
    }

}
