package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.Type;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.io.PrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class FieldAccess extends AbstractSelection {
    private static final Logger log = Logger.getLogger(FieldAccess.class);

    public FieldAccess(AbstractExpr object, AbstractIdentifier field) {
        Validate.notNull(object);
        Validate.notNull(field);
        this.object = object;
        this.identifier = field;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        s.print(".");
        identifier.decompile(s);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type objType = object.verifyExpr(compiler, localEnv, currentClass);
        if (!objType.isClass()) {
            throw new ContextualError("Object is not a class. Rule 3.65.", object.getLocation());
        }
        ClassType objClassType = (ClassType) objType;
        ExpDefinition identDef = identifier.verifyIdentifier(objClassType.getDefinition().getMembers());
        if (!identDef.isField()) {
            throw new ContextualError("The identifier is not a field. Rule 3.65.", identifier.getLocation());
        }

        FieldDefinition fieldDef = (FieldDefinition) identDef;

        if (fieldDef.getVisibility() == Visibility.PROTECTED && (currentClass == null ||
                (!objClassType.isSubTypeOf(currentClass.getType())
                        && !currentClass.getType().isSubTypeOf(fieldDef.getContainingClass().getType())))) {
            throw new ContextualError("Protected field access not authorized. Rule 3.66.", identifier.getLocation());
        }
        Type fieldType = identDef.getType();
        setType(fieldType);
        return fieldType;
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        identifier.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        identifier.prettyPrint(s, prefix, true);
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(Register.R0, Register.R1));
        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            compiler.addInstruction(new WFLOAT());
        } else {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    protected void codeGenPrintHex(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(Register.R0, Register.R1));
        if (getType().isInt()) {
            compiler.addInstruction(new WINT());
        } else if (getType().isFloat()) {
            compiler.addInstruction(new WFLOATX());
        } else {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        // Load the address of the object in R0
        object.codeGenInst(compiler);
        compiler.codeGenNullDereferencing(Register.R0);
        // Load the value of the field in R0
        compiler.addInstruction(new LOAD(new RegisterOffset(identifier.getFieldDefinition().getIndex() + 1, Register.R0), Register.R0));
    }

    @Override
    boolean isFieldAccess() {
        return true;
    }

}
