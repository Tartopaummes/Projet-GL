package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.Iterator;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    @Override
    public void decompile(IndentPrintStream s) {
        Iterator<AbstractDeclMethod> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next().decompile(s);
            s.println();
        }
    }

    public void verifyListDeclMethod(DecacCompiler compiler, ClassDefinition currentClass, ClassDefinition superClass) throws ContextualError {
        currentClass.setNumberOfMethods(superClass.getNumberOfMethods());
        for (AbstractDeclMethod declMethod : getList()) {
            try {
                // Add the method to the class definition
                currentClass.getMembers().declare(
                        declMethod.name.getName(),
                        declMethod.verifyDeclMethod(compiler, currentClass, superClass)
                );
            } catch (EnvironmentExp.DoubleDefException e) {
                // If the name of the method is already used for a method, throw an error
                if (currentClass.getMembers().get(declMethod.name.getName()).isMethod()) {
                    throw new ContextualError("Method " + declMethod.name.getName() + " is already declared in class. Rule 2.6.", declMethod.getLocation());
                } else { // Else, it has to redeclare a field, throw an error
                    throw new ContextualError("Method " + declMethod.name.getName() + " redeclares a field. Rule 2.3.", declMethod.getLocation());
                }
            }
            currentClass.incNumberOfMethods();
        }
    }

    public void verifyListDeclMethodInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclMethod declMethod : getList()) {
            declMethod.verifyDeclMethodInit(compiler, localEnv, currentClass);
        }
    }
}
