package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.SSA.EntryBloc;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.Register;

import java.util.Iterator;

/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl34
 * @date 01/01/2025
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        Iterator<AbstractDeclVar> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next().decompile(s);
            s.println();
        }
    }

    /**
     * Generates the assembly code for the list of declarations
     *
     * @param compiler The compiler used to generate the code
     * @param stackPointer The register containing the stack pointer, changes between main and methods
     */
    public void codeGenDeclVar(DecacCompiler compiler, Register stackPointer) {
        int offset = 1; // The offset with which to store new variables in the stack
        for (AbstractDeclVar declVar : getList()) {
            declVar.codeGenDeclVar(compiler, offset, stackPointer);
            offset ++; // Increment the offset for the next variable
            //compiler.incrementTSTO(1);
        }
        compiler.incrementTSTO(offset);
        if (offset > 1) {
            compiler.addInstruction(new ADDSP(offset - 1));
        }

    }

    /**
     * Generates the assembly code for the list of declarations
     *
     * @param compiler     The compiler used to generate the code
     * @param stackPointer The register containing the stack pointer, changes between main and methods
     */
    public void codeGenDeclVar(DecacCompiler compiler, Register stackPointer, int offset) {
        compiler.addInstruction(new ADDSP(offset + 1));
        for (AbstractDeclVar declVar : getList()) {
            offset++; // Increment the offset for the next variable
            declVar.codeGenDeclVar(compiler, offset, stackPointer);
            //compiler.incrementTSTO(1);
        }
        compiler.incrementTSTO(offset);
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the "env_exp_r" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */    
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclVar declVar : getList()) {
            declVar.verifyDeclVar(compiler, localEnv, currentClass);
        }
    }

    /**
     * Transform all the variable declarations in a list into SSA form
     * @param block The block containing the list of variable declarations
     */
    public void transformSSADeclList(EntryBloc block) {
        for (AbstractDeclVar declVar : getList()) {
            declVar.transformSSADecl(block);
        }
    }


}
