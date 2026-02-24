package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FieldDefinition;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public abstract class AbstractDeclField extends Tree {
    protected AbstractIdentifier type;
    protected AbstractIdentifier name;
    protected Visibility visibility;
    protected AbstractInitialization initialization;

    /**
     * Verify the declaration of a field. (Pass 2 of [Syntaxe Contextuelle])
     * @param compiler The compiler
     */
    public abstract FieldDefinition verifyDeclField(DecacCompiler compiler, ClassDefinition currentClass, ClassDefinition superClass) throws ContextualError;

    public abstract void verifyDeclFieldInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError;
    protected AbstractDeclClass owner;

    /**
     * Generate the code for the initialization of the field.
     * Register R1 contains the address of the object.
     *
     * @param compiler
     */
    public void genCodeFieldInit(DecacCompiler compiler) {
        compiler.addComment("Initialisation of field " + owner.getName().getName().getName() + "." + name.getName());
        initialization.codeGenInst(compiler); // Put the value of the initialization in R0
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1)); // R1 contains the address of the object (this might not be true after the evaluation of the initialization)
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(getIndex(compiler) + 1, Register.R1)));
        name.getExpDefinition().setOperand(new RegisterOffset(getIndex(compiler) + 1, Register.R2));
    }

    private int getIndex(DecacCompiler compiler) {
        try {
            return ((FieldDefinition) (compiler.getClassDefinition(owner.getName().getName(), getLocation()).getMembers().get(name.getName()))).getIndex();
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error in got index of field; owner : " + owner.getName().getName() + " field : " + name.getName());
        }
    }

    protected void genCodeFieldImplicitInit(DecacCompiler compiler) {
        compiler.addComment("Implicit Initialisation of field " + owner.getName().getName().getName() + "." + name.getName());
        if (type.getType().isInt()) {
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0)); // For int, the initialization is 0
        } else if (type.getType().isFloat()) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), Register.R0)); // For float, the initialization is 0.0
        } else if (type.getType().isBoolean()) {
            compiler.addInstruction(new LOAD(new ImmediateBoolean(false), Register.R0)); // For boolean, the initialization is false
        } else if (type.getType().isClass()) {
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0)); // For object, the initialization is null
        } else {
            throw new UnsupportedOperationException("Type not supported for NoInitialization in ImplicitInit");
        }
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(getIndex(compiler) + 1, Register.R1)));
        name.getExpDefinition().setOperand(new RegisterOffset(getIndex(compiler) + 1, Register.R2));
    }

}
