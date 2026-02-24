package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.ListDeclVar;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;

public class EntryBloc extends AbstractBloc{

    private ListDeclVar listDeclVar;
    private int offset;
    private Register stackPointer;

    public EntryBloc(ListDeclVar listDeclVar, Register stackPointer, int offset){
        this.listDeclVar = listDeclVar;
        listDeclVar.transformSSADeclList(this);
        this.offset = offset;
        this.stackPointer = stackPointer;
    }

    public String toString(){
        return "EntryBloc : \n" + super.toString();
    }

    @Override
    public void codeGenBloc(DecacCompiler compiler){
        this.listDeclVar.codeGenDeclVar(compiler, stackPointer, offset);
        this.son.codeGenBloc(compiler);
    }

}
