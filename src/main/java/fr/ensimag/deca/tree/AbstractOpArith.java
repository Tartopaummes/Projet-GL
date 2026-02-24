package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type type1 = getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type type2 = getRightOperand().verifyExpr(compiler, localEnv, currentClass);
        if (type1.isInt() && type2.isFloat()) {
            AbstractExpr converted = new ConvFloat(getLeftOperand());
            // Set the type of the converted expression to the expected type
            converted.setType(compiler.environmentType.FLOAT);
            getRightOperand().setType(compiler.environmentType.FLOAT);
            type1 = compiler.environmentType.FLOAT;
            setLeftOperand(converted);
        } else if (type1.isFloat() && type2.isInt()) {
            AbstractExpr converted = new ConvFloat(getRightOperand());
            // Set the type of the converted expression to the expected type
            getLeftOperand().setType(compiler.environmentType.FLOAT);
            converted.setType(compiler.environmentType.FLOAT);
            type2 = compiler.environmentType.FLOAT;
            setRightOperand(converted);
        }

        //condition rule 3.33
        this.setType(typeArithmOp(compiler, type1, type2));

        return this.getType();
    }

    /**
     * Return the type of the result of an arithmetic operation between 2 given types
     *
     * @param compiler contains types
     * @param leftType    type of the left operand
     * @param rightType    type of the right operand
     * @return type of the operation result
     * @throws ContextualError when the operation between leftType and rightType is forbidden
     */
    private Type typeArithmOp(DecacCompiler compiler, Type leftType, Type rightType) throws ContextualError {
        if (leftType == null || rightType == null) {
            throw new InternalError("Operands must have a valid type, but one or both are null.");
        }
        if (leftType.isInt() && rightType.isInt()) {
            return compiler.environmentType.INT;
        }
        if ((leftType.isInt() && rightType.isFloat()) || (leftType.isFloat() && rightType.isInt()) || (leftType.isFloat() && rightType.isFloat())) {
            return compiler.environmentType.FLOAT;
        }
        throw new ContextualError("Unsupported operation '" + getOperatorName() + "' between types " + leftType + " and " + rightType + ". Operation '" + getOperatorName() + "' supported only between integer(s) and(or) float(s). (Rule 3.33)", getLocation());
    }
}
