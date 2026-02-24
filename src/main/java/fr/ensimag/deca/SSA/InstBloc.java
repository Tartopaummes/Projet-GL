package fr.ensimag.deca.SSA;

import fr.ensimag.deca.tree.ListInst;

public class InstBloc extends AbstractBloc{

    public InstBloc(ListInst listInst, AbstractBloc parent) {
        super(listInst, parent);
    }

    public String toString(){
        return "InstBloc : " + super.toString();
    }
}
