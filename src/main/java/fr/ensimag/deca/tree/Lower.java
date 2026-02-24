package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SLT;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Lower extends AbstractOpIneq {

    public Lower(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }


    @Override
    protected String getOperatorName() {
        return "<";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.codeGenBinaryExp(compiler);
        compiler.addInstruction(new CMP(Register.R1, Register.R0));
        compiler.addInstruction(new SLT(Register.R0));
    }

}
