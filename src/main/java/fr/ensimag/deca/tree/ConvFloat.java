package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class ConvFloat extends AbstractUnaryExpr {
    public ConvFloat(AbstractExpr operand) throws InternalError {
        super(operand);
        if (operand.getType() != null && !operand.getType().isInt()) {
            throw new InternalError("The operand in the convFloat must be type int but is type" + operand.getType());
        }
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        throw new UnsupportedOperationException("Must not call verifyExpr on ConvFloat.");
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Generating the code for a conv float");
        this.getOperand().codeGenInst(compiler);
        compiler.addInstruction(new FLOAT(Register.R0, Register.R0), "Converting int to float");
    }


    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

}
