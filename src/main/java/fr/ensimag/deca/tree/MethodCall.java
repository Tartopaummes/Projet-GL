package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.Type;
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

public class MethodCall extends AbstractSelection {

    private ListExpr arguments;

    public MethodCall(AbstractExpr object, AbstractIdentifier method, ListExpr arguments) {
        Validate.notNull(object);
        Validate.notNull(method);
        Validate.notNull(arguments);
        this.object = object;
        this.identifier = method;
        this.arguments = arguments;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        object.decompile(s);
        if (!object.isImplicit()) {
            s.print(".");
        }
        identifier.decompile(s);
        s.print("(");
        arguments.decompile(s);
        s.print(")");
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type objectType;
        MethodDefinition methodDef;
        if (!object.isImplicit()) {
            objectType = object.verifyExpr(compiler, localEnv, currentClass);
            ClassType objectClassType = objectType.asClassType("Object is not a class. Rule 3.71.", object.getLocation());
            methodDef = identifier.verifyMethod(objectClassType.getDefinition().getMembers());
        } else {
            methodDef = identifier.verifyMethod(null);
        }
        identifier.setDefinition(methodDef);

        if (arguments.size() != methodDef.getSignature().size()) {
            throw new ContextualError("Method " + identifier.getName() + " required " + methodDef.getSignature().size() + " argument(s) but found " + arguments.size() + " argument(s). Rule 3.74.", getLocation());
        }
        arguments.verifyMethodArguments(compiler, localEnv, currentClass, methodDef.getSignature());

        Type returnType = methodDef.getType();
        setType(returnType);
        return returnType;
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        object.iter(f);
        identifier.iter(f);
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        object.prettyPrint(s, prefix, false);
        identifier.prettyPrint(s, prefix, false);
        arguments.prettyPrint(s, prefix, true);
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addComment("Method Call " + object.getType().toString() + "." + identifier.getName().getName());
        compiler.addComment("Evaluation of the arguments and pushing them on the stack");
        compiler.newTSTO();
        object.codeGenInst(compiler);
        compiler.incrementTSTO(arguments.size() + 1);
        compiler.addInstruction(new ADDSP(arguments.size()));
        compiler.addInstruction(new PUSH(Register.R0));
        int i = 1;
        for (AbstractExpr expr : arguments.getList()) {
            expr.codeGenInst(compiler);
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(-i, Register.SP)));
            i++;
        }
        // Test for null pointer
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R0), "Load object for null pointer test");
        compiler.codeGenNullDereferencing(Register.R0);
        compiler.addComment("Actual Method Call");
        compiler.addComment("Load the address of the method table in R0");
        //compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.R0));
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));
        int methodIndex = ((MethodDefinition) identifier.getDefinition()).getIndex();
        compiler.addComment("Branch");
        compiler.addInstruction(new BSR(new RegisterOffset(methodIndex + 1, Register.R0)));
        compiler.addComment("Method Call End");
        compiler.addInstruction(new SUBSP(arguments.size() + 1));
        compiler.endTSTO();

    }


    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        if (getType().isFloat()) {
            FloatType.codeGenPrint(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }

    @Override
    public void codeGenPrintHex(DecacCompiler compiler) {
        if (getType().isFloat()) {
            FloatType.codeGenPrintHex(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }

}