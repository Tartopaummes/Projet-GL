package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        
        harmonizeOperandTypes(this, compiler, localEnv, currentClass);

        Type leftType = getLeftOperand().getType();
        Type rightType = getRightOperand().getType();


        //condition rule 3.33
        //types can be arithmetic (int or float)
        if (leftType.isTypeArith() && rightType.isTypeArith()) {
            this.setType(compiler.environmentType.BOOLEAN);
            return this.getType();
        }

        //types can be classes or null
        if ((leftType.isClass() || leftType.isNull()) && (rightType.isClass() || rightType.isNull())) {
            this.setType(compiler.environmentType.BOOLEAN);
            return this.getType();
        }

        //types can be both booleans
        if (leftType.isBoolean() && rightType.isBoolean()) {
            this.setType(compiler.environmentType.BOOLEAN);
            return this.getType();
        }
        throw new ContextualError("Unsupported operation '" + getOperatorName() + "' between types " + leftType + " and " + rightType + ". (Rule 3.33)", getLocation());
    }


}
