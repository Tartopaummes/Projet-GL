package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.util.UUID;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Or extends AbstractOpBool {

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }

    public void codeGenInst(DecacCompiler compiler){
        //generate a random identifier (hexa in 128 bits) for the ifThenElse statement
        UUID uuid = UUID.randomUUID();
        // Convert the UUID to a hexadecimal string
        String labelId = uuid.toString().replace("-", "");
        Label label_end_or = new Label("end_or_" + labelId);
        Label label_true_or = new Label("true_or_" + labelId);
        int not = 0;
        while (getLeftOperand() instanceof Not){
            not++;
            setLeftOperand(((Not) getLeftOperand()).getOperand());
        }
        getLeftOperand().codeGenInst(compiler);
        compiler.addInstruction(new CMP(0, Register.R0));
        if (not%2 == 0){
            compiler.addInstruction(new BNE(label_true_or));
        } else {
            compiler.addInstruction(new BEQ(label_true_or));
        }

        not = 0;
        while (getRightOperand() instanceof Not){
            not++;
            setRightOperand(((Not) getRightOperand()).getOperand());
        }
        getRightOperand().codeGenInst(compiler);

        if (not%2 != 0){
            compiler.addInstruction(new CMP(0, Register.R0));
            compiler.addInstruction(new SEQ(Register.R0));
        }
        compiler.addInstruction(new BRA(label_end_or));
        compiler.addLabel(label_true_or);
        compiler.addInstruction(new LOAD(1, Register.R0));
        compiler.addLabel(label_end_or);

    }

}
