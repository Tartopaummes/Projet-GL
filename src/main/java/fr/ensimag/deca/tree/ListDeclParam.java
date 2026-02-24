package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.Iterator;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class ListDeclParam extends TreeList<AbstractDeclParam> {

    @Override
    public void decompile(IndentPrintStream s) {
        Iterator<AbstractDeclParam> iterator = iterator();
        boolean firstExpr = true;
        while (iterator.hasNext()) {
            if (!firstExpr) {
                s.print(", ");
            }
            iterator.next().decompile(s);
            firstExpr = false;
        }
    }


    public void verifyListDeclParam(DecacCompiler compiler, Signature methodSignature)
            throws ContextualError {
        for (AbstractDeclParam declParam : getList()) {
            methodSignature.add(declParam.verifyDeclParam(compiler));
        }
    }

    /**
     * Verify the list of parameter declarations for pass 3 of [Syntaxe Contextuelle]
     * @param compiler The compiler
     * @param currentClass The current class definition
     * @return The environment of the parameters
     * @throws ContextualError If an error occurred (see list of errors for pass 3)
     */
    public EnvironmentExp verifyListDeclParamInit(DecacCompiler compiler, ClassDefinition currentClass)
            throws ContextualError {
        EnvironmentExp paramEnv = new EnvironmentExp(currentClass.getMembers());
        for (AbstractDeclParam declParam : getList()) {
            declParam.verifyDeclParamInit(compiler, paramEnv, currentClass);
        }
        return paramEnv;
    }
}
