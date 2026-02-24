package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SEQ;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type type = getOperand().verifyExpr(compiler, localEnv, currentClass);
        //Condition rule 3.37
        if (type.isBoolean()) {
            this.setType(compiler.environmentType.BOOLEAN);
            return this.getType();
        }
        throw new ContextualError("Unsupported unary operation '" + getOperatorName() + "' with type " + type + ". Type must be boolean. (Rule 3.37)", getLocation());
    }


    @Override
    protected String getOperatorName() {
        return "!";
    }

    public void codeGenInst(DecacCompiler compiler){
        this.getOperand().codeGenInst(compiler);
        compiler.addInstruction(new CMP(0, Register.R0));
        compiler.addInstruction(new SEQ(Register.R0));
    }

}
