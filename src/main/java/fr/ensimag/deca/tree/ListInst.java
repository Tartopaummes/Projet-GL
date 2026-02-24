package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.SSA.InstBloc;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * 
 * @author gl34
 * @date 01/01/2025
 */
public class ListInst extends TreeList<AbstractInst> {

    /**
     * Implements non-terminal "list_inst" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler     contains "env_types" attribute
     * @param localEnv     corresponds to "env_exp" attribute
     * @param currentClass corresponds to "class" attribute (null in the main bloc).
     * @param returnType   corresponds to "return" attribute (void in the main bloc).
     */
    public void verifyListInst(DecacCompiler compiler, EnvironmentExp localEnv,
                               ClassDefinition currentClass, Type returnType, AbstractIdentifier methodOwnerName)
            throws ContextualError {
        for (AbstractInst i : getList()) {
            i.verifyInst(compiler, localEnv, currentClass, returnType, methodOwnerName); // For each instruction in the list, verify it
        }
    }

    public void codeGenListInst(DecacCompiler compiler) {
        for (AbstractInst i : getList()) {
            i.codeGenInst(compiler); // For each instruction in the list, generate the assembly code
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractInst i : getList()) {
            i.decompileInst(s);
            s.println();
        }
    }

    /**
     * this function generate the graph between the entry and exit, according to the instructions
     * @param entry
     * @param exit
     */
    public void genGraph(AbstractBloc entry, AbstractBloc exit) {
        // List of instructions used to store the instructions seen up until now
        ListInst currentList = new ListInst();

        for (AbstractInst inst : this.getList()) {
            if (inst.isIfThenElse()) {
                // instruction if, imply a change of bloc
                // return the last bloc the method created
                entry = ((IfThenElse)inst).genIfBloc(currentList, entry);
                currentList = new ListInst();
            } else if (inst.isWhile()) {
                // instruction while, creation of a bloc to manage instruction before loop
                entry = new InstBloc(currentList, entry);
                // return the While bloc
                entry = ((While)inst).genWhileBloc(entry);
                currentList = new ListInst();
            } else {
                // instruction which does not imply a change of bloc
                inst.transformSSAInst(entry);
                currentList.add(inst);
            }
        }
        if (!currentList.isEmpty()){
            // create a new blo with the last instructions
            entry = new InstBloc(currentList, entry);
        }
        exit.setParent(entry);
        entry.setSon(exit);

    }
}