package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        harmonizeOperandTypes(this, compiler, localEnv, currentClass);

        Type leftType = getLeftOperand().getType();
        Type rightType = getRightOperand().getType();

        //both types must be arithmetic (int or float) (condition rule 3.33)
        if (leftType.isTypeArith() && rightType.isTypeArith()) {
            this.setType(compiler.environmentType.BOOLEAN);
            return getType();
        }
        throw new ContextualError("Unsupported operation '" + getOperatorName() + "' between types " + leftType + " and " + rightType + ". Operation '" + getOperatorName() + "' supported only between integer(s) and(or) float(s). (Rule 3.33)", getLocation());

    }


}
