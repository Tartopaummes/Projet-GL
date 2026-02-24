package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue) super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        // Verify the validity of the target
        Type leftType = this.getLeftOperand().verifyLValue(compiler, localEnv, currentClass);
        getLeftOperand().setType(leftType);
        // verify the validity of the assigned expression
        this.setRightOperand(this.getRightOperand().verifyRValue(compiler, localEnv, currentClass, leftType));

        // The assign returns the type of the target
        this.setType(leftType);
        return this.getType();
    }

    @Override
    public void transformSSAInst(AbstractBloc block) {
        // Transform the right operand
        getRightOperand().transformSSAInst(block);

        // If the lValue is a variable, create a new SSAVariable - and change the last used SSAVariable - for this variable
        // Also replace it with the new SSAVariable.
        AbstractLValue lValue = getLeftOperand();
        if (lValue.isIdentifier()) {

            SSAVariable ssaVar = new SSAVariable(((Identifier) lValue).getName(), (Identifier) lValue, getRightOperand());
            setLeftOperand(ssaVar);
            block.setLastUsedVar(((Identifier) lValue).getName(), ssaVar);
        }

    }
    @Override
    public void transformSSAInstLoop(AbstractBloc block) {
        // Transform the right operand
        getRightOperand().transformSSAInstLoop(block);

        AbstractLValue lValue = getLeftOperand();
        if (lValue.isSSAVariable()) {
            block.setLastUsedVar(((SSAVariable) lValue).getName(), (SSAVariable)lValue);
            ((SSAVariable) lValue).setValue(getRightOperand());
        }
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        //get the identifier depending on the type of the leftoperand
        AbstractIdentifier leftIdentifier;
        if (this.getLeftOperand().isIdentifier()) {
            leftIdentifier = (AbstractIdentifier) this.getLeftOperand();
        } else if (this.getLeftOperand().isFieldAccess()) {
            leftIdentifier = (AbstractIdentifier) ((FieldAccess) this.getLeftOperand()).identifier;
        } else {
            throw new DecacInternalError("Left operand of assign is not an identifier or a field acces");
        }


        if (leftIdentifier.getDefinition().isExpression()) {

            compiler.addComment("Generating code for right operand of assign to " + leftIdentifier.getName().getName());
            this.getRightOperand().codeGenInst(compiler);
            compiler.addInstruction(new STORE(Register.R0, ((ExpDefinition) leftIdentifier.getDefinition()).getOperand()),
                    "Storing value in variable " + leftIdentifier.getName().getName());
            compiler.addInstruction(new STORE(Register.R0, ((ExpDefinition) leftIdentifier.getDefinition()).getOperand()));
        }
    }



    @Override
    protected String getOperatorName() {
        return "=";
    }

}

