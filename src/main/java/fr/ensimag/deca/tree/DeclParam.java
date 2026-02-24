package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class DeclParam extends AbstractDeclParam {

    public DeclParam(AbstractIdentifier type, AbstractIdentifier name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, true);
    }

    @Override
    public Type verifyDeclParam(DecacCompiler compiler) throws ContextualError {
        // Verify that the parameter is not of type void
        Type paramType = type.verifyType(compiler);
        if (paramType.isVoid()) {
            throw new ContextualError("Type void is not allowed for a parameter. Rule 2.9.", getLocation());
        }
        // Set the type of the parameter
        type.setType(paramType);
        return paramType;
    }

    @Override
    public void verifyDeclParamInit(DecacCompiler compiler, EnvironmentExp paramEnv, ClassDefinition currentClass) throws ContextualError {
        // Verify that the parameter is not of type void
        Type paramType = type.verifyType(compiler);

        // Set the type of the parameter
        type.setType(paramType);

        // Declare the parameter in the parameters' environment
        ParamDefinition paramDef = new ParamDefinition(paramType, getLocation());
        try {
            paramEnv.declare(name.getName(), paramDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError("Parameter " + name.getName() + " is already declared in this method. Rule 3.12.", getLocation());
        }
        name.setDefinition(paramDef);
    }

    public ExpDefinition verifyDeclParamEnv(DecacCompiler compiler) throws ContextualError {
        return new ParamDefinition(type.verifyType(compiler), getLocation());
    }
}
