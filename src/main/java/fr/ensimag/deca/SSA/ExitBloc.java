package fr.ensimag.deca.SSA;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tree.ListInst;

public class ExitBloc extends AbstractBloc{

    public ExitBloc() {
        super();
    }

    public String toString(){
        return "ExitBloc : \n" + super.toString();
    }

    @Override
    public void codeGenBloc(DecacCompiler compiler){
        //Do nothing
    }

}
