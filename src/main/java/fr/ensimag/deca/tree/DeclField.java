package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class DeclField extends AbstractDeclField {
    public DeclField(AbstractDeclClass owner, AbstractIdentifier type, AbstractIdentifier name, Visibility visibility, AbstractInitialization initialization) {
        Validate.notNull(owner);
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(visibility);
        Validate.notNull(initialization);
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.visibility = visibility;
        this.initialization = initialization;
    }

    public DeclField(AbstractDeclClass owner, AbstractIdentifier type, AbstractIdentifier name, Visibility visibility) {
        Validate.notNull(owner);
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(visibility);
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.visibility = visibility;
        this.initialization = new NoInitialization();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        visibility.decompile(s);
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }

    @Override
    public FieldDefinition verifyDeclField(DecacCompiler compiler, ClassDefinition currentClass, ClassDefinition superClass) throws ContextualError {
        Type fieldType = type.verifyType(compiler);
        type.setType(fieldType);
        // Verify that the field is not of type void
        if (type.getType().isVoid()) {
            throw new ContextualError("Field cannot be of type void. Rule 2.5.", getLocation());
        }
        // Verify that the field is not already declared in the super
        ExpDefinition fieldDefSuper = superClass != null ? superClass.getMembers().get(name.getName()) : null;
        if (fieldDefSuper != null && !fieldDefSuper.isField()) {
            throw new ContextualError("Field " + name.getName() + " overrides a method from super class. Rule 2.5.", getLocation());
        }
        // Return the field definition
        FieldDefinition fieldDef = new FieldDefinition(type.getType(), getLocation(), visibility, currentClass, currentClass.getNumberOfFields());
        name.setDefinition(fieldDef);
        return fieldDef;
    }

    public void verifyDeclFieldInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type fieldType = type.verifyType(compiler);
        initialization.verifyInitialization(compiler, fieldType, localEnv, currentClass);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        s.println(prefix + visibility.toString() + " ");
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
    }
}
