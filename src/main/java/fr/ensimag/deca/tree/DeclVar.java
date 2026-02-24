package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacFatalError;
import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.SSA.EntryBloc;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;

/**
 * @author gl34
 * @date 01/01/2025
 */
public class DeclVar extends AbstractDeclVar {

    final private AbstractIdentifier type;
    private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
                                 EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        // Verify if the type exists
        Type varType = type.verifyType(compiler);
        if (varType.isVoid()) {
            throw new ContextualError("Variable type cannot be void. Rule 3.17.", this.getLocation());
        }

        // And set the variable type to it
        type.setType(varType);

        // Verify if the initialization is valid (correct typing)
        initialization.verifyInitialization(compiler, varType, localEnv, currentClass);

        // Create a new variable definition to describe our variable
        ExpDefinition def = new VariableDefinition(varType, getLocation());

        // Declare the variable in the local environment and throw if it fails
        try {
            localEnv.declare(varName.getName(), def);
            varName.setDefinition(def);
            varName.setType(varType);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(e.getMessage() + " Rule 3.17.", this.getLocation());
        }
    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler, int offset, Register stackPointer) {
        // Declaring variable
        VariableDefinition varDef;
        // Type of the variable
        Type varType;
        try {
            // Trying to get the variable definition from the symbol, fails if not variable
            varDef = varName.getVariableDefinition();
        } catch (DecacInternalError e) {
            throw new DecacInternalError("Expected a variable and did not get one", e);
        }
        try {
            // Trying to get the type definition from the symbol, fails if not type
            varType = type.getType();
        } catch (DecacInternalError e) {
            throw new DecacInternalError("Expected a type and did not get one", e);
        }
        // Store the variable here (offset from bottom of stack)
        RegisterOffset varRegister = new RegisterOffset(offset, stackPointer);
        // Set the variable location
        varDef.setOperand(varRegister);
        // If empty initialization, set the value to 0
        if (initialization.isNoInitialization()) {
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        } else {
            // ELse, generate the code for the expression
            ((Initialization) initialization).getExpression().codeGenInst(compiler);
        }
        // Store the value of R0 in the variable
        compiler.addInstruction(new STORE(Register.R0, varDef.getOperand()));
        compiler.addInstruction(new ADDSP(1));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        varName.decompile(s);
        initialization.decompile(s);
        s.print(";");

    }

    @Override
    protected void transformSSADecl(EntryBloc block) {
        // Transform the right operand
        AbstractExpr initValue = initialization.transformSSAInit(block);

        // If the lValue is a variable, create a new SSAVariable - and change the last used SSAVariable - for this variable
        // Also replace it with the new SSAVariable.

        SSAVariable ssaVar = new SSAVariable(varName.getName(), (Identifier) varName, initValue);
        varName = ssaVar;
        //System.out.println("Assigning new SSA var " + ssaVar + " to " + varName.getName().getName() + " at " +getLocation() + " with value " + initValue);
        block.setLastUsedVar(varName.getName(), ssaVar);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
