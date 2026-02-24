package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class New extends AbstractExpr {

    private AbstractIdentifier identifier;
    private ClassDefinition classDefinition;

    public New(AbstractIdentifier identifier) {
        Validate.notNull(identifier);
        this.identifier = identifier;
    }

    public ClassDefinition getClassDefinition() {
        return classDefinition;
    }

    public void setClassDefinition(ClassDefinition classDefinition) {
        this.classDefinition = classDefinition;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new ");
        identifier.decompile(s);
        s.print("()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        identifier.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        identifier.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type objType = identifier.verifyType(compiler);
        if (!objType.isClass()) {
            throw new ContextualError("The object " + identifier.getName().getName() + " is not a class. Rule 3.42.", identifier.getLocation());
        }
        // Set the class definition
        setClassDefinition((ClassDefinition) compiler.environmentType.defOfType(identifier.getName()));
        setType(objType);
        return objType;
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Allocation of a new object of class " + identifier.getName().getName());
        // Allocate memory for the object on the heap
        compiler.addInstruction(new NEW(compiler.getClassDefinition(identifier.getName(), getLocation()).getNumberOfFields() + 1, Register.R0),
                                "Allocate memory for the object on the heap");

        // Store the address of the method table in the object (first field)
        compiler.addInstruction(new LEA(classDefinition.getMethodTableAddr(), Register.R1));
        compiler.addInstruction(new STORE(Register.R1, new RegisterOffset(0, Register.R0)),
                                "Store the address of the method table in the object (first field)");
        compiler.addInstruction(new PUSH(Register.R0));

        // Call the initialization method of the class
        compiler.addInstruction(new BSR(new Label("init." + identifier.getName().getName())),
                "Call the initialization method of the class");
        compiler.addInstruction(new POP(Register.R0));
    }
}
