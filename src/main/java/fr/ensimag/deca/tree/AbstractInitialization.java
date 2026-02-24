package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.EntryBloc;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Initialization (of variable, field, ...)
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractInitialization extends Tree {
    
    /**
     * Implements non-terminal "initialization" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param t corresponds to the "type" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */
    protected abstract void verifyInitialization(DecacCompiler compiler,
            Type t, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Method to check whether an initialization is empty or not
     * @return Returns true if the initialization is empty and false if not
     */
    public abstract boolean isNoInitialization();

    /**
     * Transform the initialization of a variable to SSA form
     * @param block The block that contains the initialization
     * @return The transformed expression
     */
    public abstract AbstractExpr transformSSAInit(EntryBloc block);
    public abstract AbstractExpr transformSSAInitLoop(EntryBloc block);
    public abstract void codeGenInst(DecacCompiler compiler);

}
