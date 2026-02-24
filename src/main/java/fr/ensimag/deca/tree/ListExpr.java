package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.Iterator;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl34
 * @date 01/01/2025
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        Iterator<AbstractExpr> iterator = iterator();
        boolean firstExpr = true;
        while (iterator.hasNext()) {
            if (!firstExpr) {
                s.print(", ");
            }
            iterator.next().decompile(s);
            firstExpr = false;
        }
    }

    public void verifyMethodArguments(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Signature signature)
            throws ContextualError {
        int i = 0;
        for (AbstractExpr expr : getList()) {
            set(i, expr.verifyRValue(compiler, localEnv, currentClass, signature.paramNumber(i)));
            i++;
        }
    }
}
