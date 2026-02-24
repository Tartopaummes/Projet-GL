package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "/";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.codeGenBinaryExp(compiler); //the right operand is in R1 and left operand is in R0


        if (getLeftOperand().getType().isInt()) {
            //test if the right operand (R1) is not equal to 0
            if (!compiler.getCompilerOptions().getNo_check()) {
                compiler.addComment("Check if the left operand of the division is not equal to 0 : ");
                compiler.addInstruction(new CMP(new ImmediateInteger(0), Register.R1));
                compiler.addInstruction(new BEQ(new Label("integer_division_0_error")));
            }
            compiler.addInstruction(new QUO(Register.R1, Register.R0));
        } else {
            //No need to check the division by 0.0 because this vervification is done in codeGenOverflowError
            compiler.addInstruction(new DIV(Register.R1, Register.R0));
            this.codeGenOverflowError(compiler, getType());
        }
    }


}
