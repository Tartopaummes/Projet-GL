package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public abstract class AbstractDeclMethod extends Tree {
    protected AbstractIdentifier type;
    protected AbstractIdentifier name;
    protected ListDeclParam params;
    protected AbstractDeclClass owner;

    /**
     * Verify the declaration of a method. (Pass 2 of [Syntaxe Contextuelle])
     * @param compiler The compiler
     * @param currentClass The class definition
     * @param superClass The super class definition
     * @throws ContextualError if contextual error (see list of errors for pass 2)
     */
    public MethodDefinition verifyDeclMethod(DecacCompiler compiler, ClassDefinition currentClass, ClassDefinition superClass) throws ContextualError {
        // Check if the method is already declared in the super as a field or with a different signature
        ExpDefinition methodDefSuper = superClass != null ? superClass.getMembers().get(name.getName()) : null;
        if (methodDefSuper != null && !methodDefSuper.isMethod()) {
            throw new ContextualError("Method " + name.getName() + " redeclares a field. Rule 2.3.", getLocation());
        }

        Type returnType = type.verifyType(compiler);
        // Check if the method is already declared in the super with a different return type
        if (methodDefSuper != null && !returnType.sameType(methodDefSuper.getType()) && !(returnType.isClass() && methodDefSuper.getType().isClass() && (((ClassType) returnType).isSubTypeOf((ClassType) methodDefSuper.getType())))) {
            throw new ContextualError("Redefined method " + name.getName().getName() +
                    "'s return type must be a subtype of the inherited method's return type. Rule 2.7.", getLocation());
        }


        // Update the method signature
        Signature methodSignature = new Signature();
        params.verifyListDeclParam(compiler, methodSignature);
        // create the method definition
        MethodDefinition methodDef = new MethodDefinition(returnType, getLocation(), methodSignature, currentClass.getNumberOfMethods());
        methodDef.setDeclClass(currentClass.getDeclClass());
        //Set the identifier definition
        name.setDefinition(methodDef);

        if (methodDefSuper != null &&
                !(((MethodDefinition) methodDefSuper).getSignature().equals(name.getMethodDefinition().getSignature()))) {
            throw new ContextualError("Redefined method " + name.getName().getName() +
                    " doesn’t have the same signature as the inherited method. Rule 2.7.", getLocation());
        }
        return methodDef;
    }

    /**
     * Verify the initialization of a method. (Pass 3 of [Syntaxe Contextuelle])
     * @param compiler The compiler
     * @param localEnv The local environment
     * @param currentClass The class definition
     * @throws ContextualError if contextual error (see list of errors for pass 3)
     */
    public void verifyDeclMethodInit(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type returnType = type.verifyType(compiler);
        type.setType(returnType);

        EnvironmentExp paramEnv = params.verifyListDeclParamInit(compiler, currentClass);
        // Verify the method body
        verifyMethodBody(compiler, localEnv, paramEnv, currentClass, returnType);
    }

    /**
     * Verify the body of a method. (Pass 3 of [Syntaxe Contextuelle])
     * @param compiler The compiler
     * @param localEnv The local environment
     * @param currentClass The class definition
     * @param returnType The return type of the method
     * @throws ContextualError if contextual error (see list of errors for pass 3)
     */
    public abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, EnvironmentExp paramEnv,
                                          ClassDefinition currentClass, Type returnType) throws ContextualError;


    protected int genCodeMethodTable(DecacCompiler compiler, int offset, String className) {
        offset++;
        compiler.addInstruction(new LOAD(new LabelOperand(new Label("code." + className + "." + name.getName())), Register.R0));
        compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(offset, Register.GB)));
        return offset;
    }

    public abstract void genCodeMethodCode(DecacCompiler compiler);
}
