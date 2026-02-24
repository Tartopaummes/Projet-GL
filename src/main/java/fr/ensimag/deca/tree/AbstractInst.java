package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;

/**
 * Instruction
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractInst extends Tree {
    
    /**
     * Implements non-terminal "inst" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     * @param returnType
     *          corresponds to the "return" attribute (void in the main bloc).
     * @param methodOwnerName identifier of the method who contains the inst. null if not in a method
     */    
    protected abstract void verifyInst(DecacCompiler compiler,
                                       EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType, AbstractIdentifier methodOwnerName) throws ContextualError;

    /**
     * Generate assembly code for the instruction.
     * Change from void to int to help TSTO calculations
     * @param compiler
     */
    protected abstract void codeGenInst(DecacCompiler compiler);

    /**
     * Decompile the tree, considering it as an instruction.
     *
     * In most case, this simply calls decompile(), but it may add a semicolon if needed
     */
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
    }

    /**
     * Transforms the identifiers of variables in the instruction into SSAVariables to be added into the SSA graph.
     * IfThenElse and While statements get a different treatment so this function should never be called
     * on them and will throw a DecacInternalError if it is.
     * @param block The block containing the instruction, it is used to access the last used SSAVariables for
     *              different variables in the instruction.
     */
    public void transformSSAInst(AbstractBloc block) {
        // Do nothing by default
    }

    public void transformSSAInstLoop(AbstractBloc block) {
        // Do nothing by default
    }


    public boolean isIfThenElse(){
        return false;
    }
    public boolean isWhile(){
        return false;
    }
}
