package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.SSA.GraphSSA;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Register;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify Main: start");
        // A FAIRE: Appeler méthodes "verify*" de ListDeclVarSet et ListInst.
        // Vous avez le droit de changer le profil fourni pour ces méthodes
        // (mais ce n'est à priori pas nécessaire).

        // Placeholder for the moment
        EnvironmentExp envExp = new EnvironmentExp(null); // For the moment no environment because no class or declaration variable
        Type returnType = compiler.environmentType.VOID; // Return void for the main

        declVariables.verifyListDeclVariable(compiler, envExp, null); //for variables
        // envExp is completed by verifyListDeclVariable and can be used by verifyListInst
        insts.verifyListInst(compiler, envExp, null, returnType, null); // Verification of the list of instructions

        LOG.debug("verify Main: end");
        // throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler, int offset) {
        // A FAIRE: traiter les déclarations de variables.
        compiler.addComment("Beginning of main instructions:");
        //The TSTO for main program
        compiler.newTSTO();
        if (compiler.getCompilerOptions().optimize){
            new GraphSSA(declVariables, insts, Register.GB, offset).codeGen(compiler);
        } else {
            declVariables.codeGenDeclVar(compiler, Register.GB, offset);
            insts.codeGenListInst(compiler);
        }
        compiler.endTSTO();
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
