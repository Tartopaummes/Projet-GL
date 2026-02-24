package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.util.UUID;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class And extends AbstractOpBool {

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    public void codeGenInst(DecacCompiler compiler) {
        //generate a random identifier (hexa in 128 bits) for the ifThenElse statement
        UUID uuid = UUID.randomUUID();
        // Convert the UUID to a hexadecimal string
        String labelId = uuid.toString().replace("-", "");
        Label label_end_and = new Label("end_and_" + labelId);
        Label label_false_and = new Label("false_and_" + labelId);

        // Add comment for label creation
        compiler.addComment("Starting code generation for AND operation");
        compiler.addComment("Generated unique labels: " + label_end_and.toString() + " and " + label_false_and.toString());

        // Manage not operand, and squash them together if there are multiple to change branching accordingly
        int not = 0;
        while (getLeftOperand() instanceof Not) {
            not++;
            setLeftOperand(((Not) getLeftOperand()).getOperand());
        }
        compiler.addComment("Generating code for left operand of AND operation");
        getLeftOperand().codeGenInst(compiler);
        compiler.addInstruction(new CMP(0, Register.R0), "Compare left operand to false for lazy AND");
        if (not % 2 == 0) {
            compiler.addInstruction(new BEQ(label_false_and), "AND is false if left operand is false (even number of NOTs)");
        } else {
            compiler.addInstruction(new BNE(label_false_and), "AND is false if left operand is true (uneven number of NOTs)");
        }

        // Do the same thing for the right operand
        not = 0;
        while (getRightOperand() instanceof Not) {
            not++;
            setRightOperand(((Not) getRightOperand()).getOperand());
        }
        compiler.addComment("Generating code for right operand of AND operation");
        getRightOperand().codeGenInst(compiler);

        // If you reach this point, the left operand is true so the value of and is equal to right's value
        if (not % 2 != 0) {
            compiler.addInstruction(new CMP(0, Register.R0), "Inverse right value (uneven number of NOTs)");
            // Set return to new right value
            compiler.addInstruction(new SEQ(Register.R0));
        }
        compiler.addInstruction(new BRA(label_end_and));
        compiler.addLabel(label_false_and);
        compiler.addInstruction(new LOAD(0, Register.R0));
        compiler.addLabel(label_end_and);
        compiler.addComment("ending code generation for AND operation");
    }


    @Override
    protected String getOperatorName() {
        return "&&";
    }


}
