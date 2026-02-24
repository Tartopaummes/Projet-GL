package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.OPP;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type type = getOperand().verifyExpr(compiler, localEnv, currentClass);
        //Condition rule 3.37
        if (type.isInt()) {
            this.setType(compiler.environmentType.INT);
            return this.getType();
        }
        if (type.isFloat()) {
            this.setType(compiler.environmentType.FLOAT);
            return this.getType();
        }
        throw new ContextualError("Unsupported unary operation '" + getOperatorName() + "' with type " + type + ". Type must be int or float. (Rule 3.37)", getLocation());
    }


    @Override
    protected String getOperatorName() {
        return "-";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.getOperand().codeGenInst(compiler);
        compiler.addInstruction(new OPP(Register.R0, Register.R0), "Opposite");
    }


}
