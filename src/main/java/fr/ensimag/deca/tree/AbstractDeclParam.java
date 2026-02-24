package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import org.apache.commons.lang.Validate;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public abstract class AbstractDeclParam extends Tree {
    protected AbstractIdentifier type;
    protected AbstractIdentifier name;

    /**
     * Verify the declaration of a list of parameters. (Pass 2 of [Syntaxe Contextuelle])
     * @param compiler The compiler
     */
    public abstract Type verifyDeclParam(DecacCompiler compiler) throws ContextualError;

    /**
     * Verify the initialization of a list of parameters. (Pass 3 of [Syntaxe Contextuelle])
     * @param compiler The compiler
     * @param localEnv The local environment
     * @param currentClass The class definition
     */
    public abstract void verifyDeclParamInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError;
}
