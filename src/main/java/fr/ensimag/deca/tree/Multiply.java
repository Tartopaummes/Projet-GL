package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.MUL;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class Multiply extends AbstractOpArith {
    public Multiply(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "*";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.codeGenBinaryExp(compiler);
        compiler.addInstruction(new MUL(Register.R1, Register.R0));
        this.codeGenOverflowError(compiler, getType());
    }


}
