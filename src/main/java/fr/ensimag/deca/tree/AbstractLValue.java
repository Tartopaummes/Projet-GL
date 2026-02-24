package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractLValue extends AbstractExpr {

    public Type verifyLValue(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type expType;
        if (isIdentifier()) {
            // If LValue is an identifier, verify it exists and return its type
            expType = verifyLValueIdent(localEnv);
        } else {
            // Else, it is a field selection
            expType = verifyExpr(compiler, localEnv, currentClass);
        }
        return expType;
    }

    public Type verifyLValueIdent(EnvironmentExp localEnv) throws ContextualError {
        if (!isIdentifier()) {
            throw new DecacInternalError("Expected an identifier for LValue");
        }
        return ((Identifier)this).verifyIdentifier(localEnv).getType();
    }
}
