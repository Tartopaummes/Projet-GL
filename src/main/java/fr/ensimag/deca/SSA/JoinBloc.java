package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.Identifier;
import fr.ensimag.deca.tree.ListInst;
import fr.ensimag.deca.tree.SSAVariable;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public class JoinBloc extends AbstractBloc{

    private AbstractBloc otherParent = null;
    private boolean continue_print;
    private boolean then_done;
    private boolean codeGenAlready = false;
    private IfBloc ifBloc;

    public JoinBloc(ListInst listInst, AbstractBloc parent) {
        super(listInst, parent);
        continue_print = false;
    }

    public void setIfBloc(IfBloc ifBloc) {
        this.ifBloc = ifBloc;
    }

    public void endIf(DecacCompiler compiler){
        this.ifBloc.endIf(compiler);
    }

    @Override
    public void setParent(AbstractBloc parent) {
        if (this.parent == null){
            this.parent = parent;
            this.lastUsedVars = new Hashtable<>(parent.lastUsedVars);
        } else if (this.otherParent == null){
            this.otherParent = parent;
            this.setEnv();
        } else {
            System.out.println("every parent already set");
        }


    }

    public String toString(){
        if (this.continue_print){
            return "JoinBloc : \n" + super.toString();
        } else {
            this.continue_print = true;
            return " JoinBloc : Back to Else";
        }

    }

    @Override
    public void setEnv(){
        Hashtable<SymbolTable.Symbol, SSAVariable> envParent1 = this.parent.lastUsedVars;
        Hashtable<SymbolTable.Symbol, SSAVariable> envParent2 = this.otherParent.lastUsedVars;
        this.lastUsedVars = envParent1;
        // check every variable looking for differences to make phi functions
        for (SymbolTable.Symbol name : envParent1.keySet()){
            if (!envParent1.get(name).equals(envParent2.get(name))){
                this.lastUsedVars.replace(name, new SSAVariable(name, envParent1.get(name).getRepresents(), null));
            }
        }

    }

    @Override
    public void secondPass(){
        if (this.then_done) {
            this.setEnv();
            this.son.secondPass();
            this.then_done = false;
        } else {
            this.then_done = true;
        }
    }

    @Override
    public void codeGenBloc(DecacCompiler compiler){
        if (this.codeGenAlready){
            endIf(compiler);
            this.son.codeGenBloc(compiler);
        } else {
            this.codeGenAlready = true;
        }
    }
}
