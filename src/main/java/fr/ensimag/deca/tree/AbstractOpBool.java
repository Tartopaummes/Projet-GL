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
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type type1 = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        //both types must be boolean (condition rule 3.33)
        if (type1.isBoolean() && type2.isBoolean()) {
            this.setType(compiler.environmentType.BOOLEAN);
            return this.getType();
        }
        throw new ContextualError("Unsupported operation '" + getOperatorName() + "' between types " + type1 + " and " + type2 + ". Operation '" + getOperatorName() + "' supported only between booleans. (Rule 3.33)", getLocation());
    }

}
