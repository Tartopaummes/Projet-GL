package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
 

    @Override
    protected String getOperatorName() {
        return "+";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.codeGenBinaryExp(compiler);
        compiler.addInstruction(new ADD(Register.R1, Register.R0));
        this.codeGenOverflowError(compiler, getType());
    }

}
