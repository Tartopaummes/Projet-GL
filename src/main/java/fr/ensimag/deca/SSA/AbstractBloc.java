package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.AbstractInst;
import fr.ensimag.deca.tree.ListInst;
import fr.ensimag.deca.tree.SSAVariable;

import java.util.Hashtable;

/**
 * Represent a part of instruction from the program
 * It is used in the control flow graph
 */
public abstract class AbstractBloc {
    protected ListInst listInst;
    protected AbstractBloc parent;
    protected AbstractBloc son;
    // Hashtable to store the last used SSAVariable for a certain variable name
    protected Hashtable<SymbolTable.Symbol, SSAVariable> lastUsedVars = new Hashtable<>();

    protected AbstractBloc(ListInst listInst, AbstractBloc parent) {
        this.listInst = listInst;
        this.parent = parent;
        // set the link on the other side
        if (parent != null){
            parent.setSon(this);
            this.setEnv();
            //lastUsedVars = new Hashtable<>(parent.lastUsedVars);
        }
        this.son = null;
    }

    protected AbstractBloc() {
        this.listInst = null;
        this.parent = null;
        this.son = null;
    }

    /**
     * default method, for vlocs with only one parent
     * Override for phi functions
     */
    public void setEnv(){
        this.lastUsedVars = new Hashtable<>(parent.lastUsedVars);
    }

    /**
     * Return the last used SSAVariable for a certain variable name in this block
     * @param var The symbol representing the variable
     * @return The last used SSAVariable corresponding to the given name
     */
    public SSAVariable getLastUsedVar(SymbolTable.Symbol var) {
        return lastUsedVars.get(var);
    }

    /**
     * Set the last used SSAVariable for a given variable name in this block
     * @param var= The symbol representing the variable
     * @param ssaVar The SSAVariable to set the correspondence to
     * @return null if varName was not associated to any SSAVariable or the old SSAVariable if it was
     */
    public SSAVariable setLastUsedVar(SymbolTable.Symbol var, SSAVariable ssaVar) {
        return lastUsedVars.put(var, ssaVar);
    }

    public void setSon(AbstractBloc son) {
        if (son != null) {
            this.son = son;
            son.startSecondPass();
        } else {
            System.out.println("son already set");
        }
    }

    public void setParent(AbstractBloc parent) {
        if (this.parent == null) {
            this.parent = parent;
        } else {
            System.out.println( parent.toString());

            System.out.println("parent already set");
        }
    }

    public String toString() {
        String str;
        if (listInst != null) {
            str = listInst.toString() + "\n";
        } else {
            str = "No instruction bloc \n";
        }
        if (son != null) {
            str += son.toString();
        } else {
            str += "no son set \n";
        }
        return str;
    }

    public void startSecondPass(){
        // Only WhileBloc start a second pass on his loop
    }

    public void secondPass(){
        this.lastUsedVars = new Hashtable<>(parent.lastUsedVars);
        if (this.listInst != null){
            for (AbstractInst inst : this.listInst.getList()){
                inst.transformSSAInstLoop(this);
            }
        }

        if (this.son != null){
            this.son.secondPass();
        }
    }

    public void codeGenBloc(DecacCompiler compiler){
        this.listInst.codeGenListInst(compiler);
        this.son.codeGenBloc(compiler);
    }


}
