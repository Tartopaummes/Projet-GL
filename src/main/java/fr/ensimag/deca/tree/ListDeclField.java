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

public class ListDeclField extends TreeList<AbstractDeclField> {

    @Override
    public void decompile(IndentPrintStream s) {
        Iterator<AbstractDeclField> iterator = iterator();
        while (iterator.hasNext()) {
            iterator.next().decompile(s);
            s.println();
        }
    }

    /**
     * Verify the list of field declarations for pass 2 of [Syntaxe Contextuelle]
     * @param compiler The compiler
     */
    public void verifyListDeclField(DecacCompiler compiler, ClassDefinition currentClass, ClassDefinition superClass) throws ContextualError {
        currentClass.setNumberOfFields(superClass.getNumberOfFields());
        for (AbstractDeclField declField : getList()) {
            try {
                // Add the field to the class definition
                currentClass.getMembers().declare(
                        declField.name.getName(),
                        declField.verifyDeclField(compiler, currentClass, superClass)
                );
            } catch (EnvironmentExp.DoubleDefException e) {
                throw new ContextualError("Field " + declField.name.getName() + " is already declared in class. Rule 2.4.", declField.getLocation());
            }
            currentClass.incNumberOfFields();
        }
    }

    /**
     * Verify the list of field declarations for pass 3 of [Syntaxe Contextuelle]
     * @param compiler The compiler
     * @param localEnv The environment in which the field is declared, i.e. the class environment
     * @param currentClass The current class definition
     * @throws ContextualError If an error occurred
     */
    public void verifyListDeclFieldInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        for (AbstractDeclField declField : getList()) {
            declField.verifyDeclFieldInit(compiler, localEnv, currentClass);
        }
    }
}
