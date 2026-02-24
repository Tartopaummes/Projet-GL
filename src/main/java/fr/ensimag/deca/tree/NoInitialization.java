package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.EntryBloc;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.LOAD;

import java.io.PrintStream;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl34
 * @date 01/01/2025
 */
public class NoInitialization extends AbstractInitialization {

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // Nothing to do
        type = t;
    }

    private Type type;


    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
    }

    @Override
    public AbstractExpr transformSSAInit(EntryBloc block) {
        return null;
    }

    @Override
    public AbstractExpr transformSSAInitLoop(EntryBloc block) {
        return null;
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public boolean isNoInitialization() {
        return true;
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        if (type.isInt()) {
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0)); // For int, the initialization is 0
        } else if (type.isFloat()) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0), Register.R0)); // For float, the initialization is 0.0
        } else if (type.isBoolean()) {
            compiler.addInstruction(new LOAD(new ImmediateBoolean(false), Register.R0)); // For boolean, the initialization is false
        } else if (type.isClass()) {
            compiler.addInstruction(new LOAD(new NullOperand(), Register.R0)); // For object, the initialization is null
        } else {
            throw new UnsupportedOperationException("Type not supported for NoInitialization");
        }

    }
}
