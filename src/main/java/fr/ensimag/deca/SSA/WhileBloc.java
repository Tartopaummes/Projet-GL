package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.ListInst;
import fr.ensimag.deca.tree.SSAVariable;
import fr.ensimag.deca.tree.While;
import fr.ensimag.ima.pseudocode.ImmediateBoolean;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;

public class WhileBloc extends AbstractBloc{

    private AbstractBloc loop_parent;
    private AbstractBloc loop_son;
    private boolean loop_printed;
    //Boolean for the second pass on loop, avoid infinite check on the loop
    private boolean secondPassGoing;
    private boolean startedSecondPass;
    private boolean codeGenAlready;
    private AbstractExpr condition;

    public WhileBloc(ListInst listInst, AbstractBloc parent, AbstractExpr condition) {
        super(listInst, parent);
        loop_parent = null;
        loop_son = null;
        loop_printed = false;
        this.condition = condition;
        this.condition.transformSSAInst(this);
    }

    @Override
    public void setParent(AbstractBloc parent) {
        if (this.loop_parent == null){
            loop_parent = parent;
        } else if (this.parent == null){
            this.parent = parent;
        } else {
            System.out.println("Every parent already set");
        }
    }

    @Override
    public void setSon(AbstractBloc son) {
        // the graph for the loop is created first, loop_parent is the first to be set
        if (this.loop_son == null){
            loop_son = son;
            son.startSecondPass();
        } else if (this.son == null){
            this.son = son;
            son.startSecondPass();
        } else {
            System.out.println("Every son already set");
        }
    }

    @Override
    public String toString(){
        if (loop_printed){
            this.loop_printed = false;
            return son.toString();
        } else {
            this.loop_printed = true;
            return "WhileBloc : \n" + loop_son.toString();

        }
    }

    @Override
    public void startSecondPass(){
        //check if the loop is set and the second pass can start

        if (this.loop_parent != null) {
            //System.out.println("\nStart second pass\n");
            //To know that second pass id going
            this.startedSecondPass = true;
            this.secondPassGoing = true;
            //modify the environement lastUsedVars
            this.updateEnv();
            condition.transformSSAInstLoop(this);
            //change SSAVariable in the loop
            this.loop_son.secondPass();
            this.startedSecondPass = false;
            //System.out.println("\nEnd second pass\n");
        }
    }

    public void updateEnv(){
        Hashtable<SymbolTable.Symbol, SSAVariable> envParent1 = this.parent.lastUsedVars;
        Hashtable<SymbolTable.Symbol, SSAVariable> envParent2 = this.loop_parent.lastUsedVars;
        this.lastUsedVars = envParent1;
        // check every variable looking for differences to make phi functions
        for (SymbolTable.Symbol name : envParent1.keySet()){
            if (!envParent1.get(name).equals(envParent2.get(name))){
                SSAVariable ssaVar = new SSAVariable(name, envParent1.get(name).getRepresents(), null);
                this.setLastUsedVar(name, ssaVar);
            }
        }
    }

    public void secondPass(){
        if (!this.secondPassGoing){
            this.secondPassGoing = true;
            this.lastUsedVars = new Hashtable<>(parent.lastUsedVars);
            this.loop_son.secondPass();
        } else
            if (!this.startedSecondPass){
            this.secondPassGoing = false;
            this.updateEnv();
            condition.transformSSAInstLoop(this);
            this.son.secondPass();
        } else {
            this.secondPassGoing = false;
            this.startedSecondPass = false;
            this.updateEnv();
            this.condition.transformSSAInstLoop(this);
        }
    }

    private Label label_while_condition;
    private String labelId;
    private Label label_while_start;

    @Override
    public void codeGenBloc(DecacCompiler compiler){
        if (!codeGenAlready) {
            codeGenAlready = true;
            //generate a random identifier (hexa in 128 bits) for the While statement
            UUID uuid = UUID.randomUUID();
            // Convert the UUID to a hexadecimal string
            labelId = uuid.toString().replace("-", "");
            label_while_condition = new Label("while_condition" + labelId);
            label_while_start = new Label("while_start" + labelId);

            // Branch to the condition
            compiler.addComment("While " + labelId + " : Branch to condition");
            compiler.newTSTO();
            compiler.addInstruction(new BRA(label_while_condition));

            // Start of the while loop
            compiler.addLabel(label_while_start);
            compiler.addComment("While " + labelId + " : Start of the loop");
            this.loop_son.codeGenBloc(compiler);


        } else {
            // Condition
            compiler.addLabel(label_while_condition);
            compiler.addComment("While " + labelId + " : Evaluate condition");
            condition.codeGenInst(compiler);
            compiler.addInstruction(new CMP(new ImmediateBoolean(true), Register.R0));
            compiler.addInstruction(new BEQ(label_while_start));
            compiler.endTSTO();
            this.son.codeGenBloc(compiler);
        }
    }

}
