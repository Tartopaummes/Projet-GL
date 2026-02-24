package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

/**
 * Deca Identifier
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Identifier extends AbstractIdentifier {
    
    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("Identifier " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * ClassDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a class definition.
     */
    @Override
    public ClassDefinition getClassDefinition() {
        try {
            return (ClassDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a class identifier, you can't call getClassDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * MethodDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a method definition.
     */
    @Override
    public MethodDefinition getMethodDefinition() {
        try {
            return (MethodDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a method identifier, you can't call getMethodDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * FieldDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public FieldDefinition getFieldDefinition() {
        try {
            return (FieldDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a field identifier, you can't call getFieldDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a
     * VariableDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public VariableDefinition getVariableDefinition() {
        try {
            return (VariableDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    /**
     * Like {@link #getDefinition()}, but works only if the definition is a ExpDefinition.
     * 
     * This method essentially performs a cast, but throws an explicit exception
     * when the cast fails.
     * 
     * @throws DecacInternalError
     *             if the definition is not a field definition.
     */
    @Override
    public ExpDefinition getExpDefinition() {
        try {
            return (ExpDefinition) definition;
        } catch (ClassCastException e) {
            throw new DecacInternalError(
                    "Identifier "
                            + getName()
                            + " is not a Exp identifier, you can't call getExpDefinition on it");
        }
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    @Override
    public Symbol getName() {
        return name;
    }

    private Symbol name;

    public Identifier(Symbol name) {
        Validate.notNull(name);
        this.name = name;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type expType = verifyLValue(compiler, localEnv, currentClass);
        setType(expType);
        return expType;
    }

    /**
     * Implements non-terminal "type" of [SyntaxeContextuelle] in the 3 passes
     * @param compiler contains "env_types" attribute
     */
    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        TypeDefinition typeDef = compiler.environmentType.defOfType(name);
        if (typeDef == null ) {
            throw new ContextualError("Undeclared type identifier " + name.getName() + ". Rule 0.2.", getLocation());
        }
        this.setDefinition(typeDef);
        return typeDef.getType();
    }

    public ExpDefinition verifyIdentifier(EnvironmentExp localEnv) throws ContextualError {
        ExpDefinition def = localEnv.get(name);
        if (def != null && !def.isExpression()) {
            throw new ContextualError("Identifier " + name.getName() + " is not an expression.", getLocation());
        }
        this.setDefinition(def);
        if (def == null) {
            throw new ContextualError("Undeclared identifier " + name.getName() + ". Rule 0.1.", getLocation());
        }
        return def;
    }

    @Override
    public MethodDefinition verifyMethod(EnvironmentExp localEnv) throws ContextualError {
        if (localEnv == null) {
            throw new ContextualError("Method " + name.getName() + " is not a method identifier. Rule 3.72.", getLocation());
        }
        ExpDefinition methodDef = localEnv.get(name);
        if (methodDef == null || !methodDef.isMethod()) {
            throw new ContextualError("Method " + name.getName() + " is not a method identifier. Rule 3.72.", getLocation());
        }
        return (MethodDefinition) methodDef;
    }
    
    private Definition definition;

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(name.toString());
    }

    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        // The new integer is loaded in R0 as that is where variable assignations take values from
        if (definition.isExpression()) {
             if (this.getType() == compiler.environmentType.INT) {
                compiler.addInstruction(new WINT());
            } else if (this.getType() == compiler.environmentType.FLOAT) {
                compiler.addInstruction(new WFLOAT());
            }
        }
    }

    @Override
    public void codeGenPrintHex(DecacCompiler compiler) {
        // The new integer is loaded in R0 as that is where variable assignations take values from
        if (definition.isExpression()) {
            if (this.getType() == compiler.environmentType.INT) {
                compiler.addInstruction(new WINT());
            } else if (this.getType() == compiler.environmentType.FLOAT) {
                compiler.addInstruction(new WFLOATX());
            }
        }
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        // The new integer is loaded in R0 as that is where variable assignations take values from
        if (definition.isField()) {
            compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R2), "Loading value of " + name.getName());
        }
        if (definition.isExpression()) {
            compiler.addInstruction(new LOAD(((ExpDefinition)definition).getOperand(), Register.R0), "Loading value of " + name.getName());
        } else {
            throw new DecacInternalError("Definition is not an expression");
        }
    }


    @Override
    String prettyPrintNode() {
        return "Identifier (" + getName() + ")";
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Definition d = getDefinition();
        if (d != null) {
            s.print(prefix);
            s.print("definition: ");
            s.print(d);
            s.println();
        }
    }

}
