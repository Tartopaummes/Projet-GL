package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.*;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Register;

public class GraphSSA {

    private EntryBloc entry;
    private ExitBloc exit;

    public GraphSSA(ListDeclVar listDeclVar, ListInst listInst, Register stackPointer, int offset) {
        this.entry = new EntryBloc(listDeclVar, stackPointer, offset);
        this.exit = new ExitBloc(); // Every value is set to null

        //this function generate the graph, it is accessible as the sons blocs of entry, or parents blocs of exit
        listInst.genGraph(this.entry, this.exit);

    }

    @Override
    public String toString(){
        return this.entry.toString();
    }

    /**
     * this function has to mimic the codeGenMain function in Main.java
     * for the graph representation
     * @param compiler
     */
    public void codeGen(DecacCompiler compiler){
        //The TSTO for main program
        // this funciton goes through every bloc
        this.entry.codeGenBloc(compiler);
    }
}

