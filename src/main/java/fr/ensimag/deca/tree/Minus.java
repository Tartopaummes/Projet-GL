package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.SUB;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.codeGenBinaryExp(compiler);
        compiler.addInstruction(new SUB(Register.R1, Register.R0));
        this.codeGenOverflowError(compiler, getType());
    }


}
