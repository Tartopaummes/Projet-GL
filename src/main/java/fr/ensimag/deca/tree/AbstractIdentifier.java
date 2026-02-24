package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.DecacInternalError;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractIdentifier extends AbstractLValue {

    @Override
    public boolean isIdentifier() {
        return true;
    }
    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    public abstract ClassDefinition getClassDefinition();

    public abstract Definition getDefinition();

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    public abstract FieldDefinition getFieldDefinition();

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    public abstract MethodDefinition getMethodDefinition();

    public abstract SymbolTable.Symbol getName();

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    public abstract ExpDefinition getExpDefinition();

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     *
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     *
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    public abstract VariableDefinition getVariableDefinition();

    public abstract void setDefinition(Definition definition);



    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     * @return the type corresponding to this identifier
     *         (corresponds to the "type" attribute)
     */
    public abstract Type verifyType(DecacCompiler compiler) throws ContextualError;

    /**
     * Verify the existence of the identifier in the environment and return its definition
     * @param localEnv The environment in which the identifier should be checked
     * @return The definition of the identifier in the given environment if it exists
     * @throws ContextualError Thrown if the identifier does not exist in the given environment (Rule 0.1)
     */
    public abstract ExpDefinition verifyIdentifier(EnvironmentExp localEnv) throws ContextualError;

    /**
     * Verify if a method is declared in the given environment
     * @param localEnv The environment in which the method should be checked
     * @return The definition of the method in the given environment if it exists
     * @throws ContextualError Thrown if the method does not exist in the given environment (Rule 3.72)
     */
    public abstract MethodDefinition verifyMethod(EnvironmentExp localEnv) throws ContextualError;
}
