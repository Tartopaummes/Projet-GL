package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.REM;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type type1 = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        // both type must be integer (condition rule 3.33)
        if (type1.isInt() && type2.isInt()) {
            this.setType(compiler.environmentType.INT);
            return this.getType();
        }
        throw new ContextualError("Unsupported operation '%' between types " + type1 + " and " + type2 + ". Operation '%' supported only between integers. (Rule 3.33)", getLocation());
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.codeGenBinaryExp(compiler);

        //test if the right operand (R1) is not equal to 0
        if (!compiler.getCompilerOptions().getNo_check()) {
            compiler.addComment("Check if the left operand of the modulo is not equal to 0 : ");
            compiler.addInstruction(new CMP(new ImmediateInteger(0), Register.R1));//We can compare to 0 car '%' is only between integers
            compiler.addInstruction(new BEQ(new Label("integer_modulo_0_error")));
        }
        compiler.addComment("Modulo : ");
        compiler.addInstruction(new REM(Register.R1, Register.R0));
    }


}
