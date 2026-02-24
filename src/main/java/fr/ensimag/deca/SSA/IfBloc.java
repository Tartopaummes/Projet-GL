package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.AbstractExpr;
import fr.ensimag.deca.tree.AbstractInst;
import fr.ensimag.deca.tree.ListInst;
import fr.ensimag.ima.pseudocode.ImmediateBoolean;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;

import java.util.Hashtable;
import java.util.UUID;

public class IfBloc extends AbstractBloc{

    private AbstractBloc elseSon;
    private AbstractExpr condition;
    private Label label_endIf;

    public IfBloc(ListInst listInst, AbstractBloc parent, AbstractExpr condition) {
        super(listInst, parent);
        elseSon = null;
        this.condition = condition;
    }

    @Override
    public String toString(){
        return "IfBloc : \n Then :\n" + son.toString() + " Else : \n" + this.elseSon.toString();
    }

    @Override
    public void setSon(AbstractBloc son) {
        if (this.son == null){
            this.son = son;
            son.startSecondPass();
        } else if (this.elseSon == null){
            this.elseSon = son;
            son.startSecondPass();
        } else {
            System.out.println("IfBloc : the 2 sons are already set");
        }
    }

    @Override
    public void secondPass(){
        this.lastUsedVars = new Hashtable<>(parent.lastUsedVars);
        if (this.listInst != null){
            for (AbstractInst inst : this.listInst.getList()){
                inst.transformSSAInstLoop(this);
            }
        }
        this.son.secondPass();
        this.elseSon.secondPass();
    }

    @Override
    public void codeGenBloc(DecacCompiler compiler){
        if (this.listInst != null){
            this.listInst.codeGenListInst(compiler);
        }
        //generate a random identifier (hexa in 128 bits) for the ifThenElse statement
        UUID uuid = UUID.randomUUID();
        // Convert the UUID to a hexadecimal string
        String labelId = uuid.toString().replace("-", "");
        Label label_else = new Label("else_" + labelId);
        Label label_end_if = new Label("end_if_" + labelId);
        this.label_endIf = label_end_if;
        //evaluate the condition and branch depending on the result
        compiler.addComment("if " + labelId + ": Evaluate condition");
        this.condition.codeGenInst(compiler);

        compiler.addInstruction(new CMP(new ImmediateBoolean(false), Register.R0));
        compiler.addInstruction(new BEQ(label_else));

        //then
        compiler.addComment("then");
        // One TSTO per branch, evaluate the condition is part of the main program
        compiler.newTSTO();
        this.son.codeGenBloc(compiler);
        compiler.endTSTO();

        //branch to the end of the if
        compiler.addInstruction(new BRA(label_end_if));
        //else
        compiler.addLabel(label_else);
        compiler.addComment("else ");
        compiler.newTSTO();
        this.elseSon.codeGenBloc(compiler);
        compiler.endTSTO();
        //end_if


    }

    public void endIf(DecacCompiler compiler){
        compiler.addLabel(this.label_endIf);
    }

}
