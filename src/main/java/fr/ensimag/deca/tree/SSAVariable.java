package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class SSAVariable extends AbstractIdentifier {

    private Definition definition;
    private final SymbolTable.Symbol name;
    // Value of this SSAVariable
    private AbstractExpr value;
    // The identifier represented by this SSAVariable
    private final Identifier represents;

    public SSAVariable(SymbolTable.Symbol name, Identifier represents, AbstractExpr value) {
        Validate.notNull(name);
        Validate.notNull(represents);
        //Validate.notNull(value);
        this.represents = represents;
        this.value = value;
        this.name = name;
        setDefinition(represents.getDefinition());
        setType(represents.getType());
    }

    public void setValue(AbstractExpr value) {
        this.value = value;
    }

    @Override
    protected void checkDecoration() {
        if (getDefinition() == null) {
            throw new DecacInternalError("SSAVariable " + this.getName() + " has no attached Definition");
        }
    }

    @Override
    public Definition getDefinition() {
        return definition;
    }

    @Override
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public AbstractExpr getValue() {
        return value;
    }

    public Identifier getRepresents() {
        return represents;
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
                    "SSAVariable "
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
                    "SSAVariable "
                            + getName()
                            + " is not a variable identifier, you can't call getVariableDefinition on it");
        }
    }

    @Override
    public SymbolTable.Symbol getName() {
        return name;
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
    public void decompile(IndentPrintStream s) {
        throw new DecacInternalError("SSAVariable should not be decompiled as it should not be in the tree.");
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
        if (value != null && value.isLiteral()) {
            value.codeGenInst(compiler);
        } else {
            represents.codeGenInst(compiler);
        }
    }


    @Override
    String prettyPrintNode() {
        return "SSAVariable (" + represents.getName() + ")";
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

    public MethodDefinition verifyMethod(EnvironmentExp localEnv) throws ContextualError {
        throw new DecacInternalError("SSAVariable should not be used with methods, only with fields and variables");
    }

    @Override
    public ExpDefinition getExpDefinition() {
        throw new DecacInternalError("SSAVariable should not be used with expressions, only with fields and variables");
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
                           ClassDefinition currentClass) throws ContextualError {
        throw new DecacInternalError("SSAVariables should not be present in the tree, they are used to optimize the code.");
    }

    @Override
    public Type verifyType(DecacCompiler compiler) throws ContextualError {
        throw new DecacInternalError("SSAVariables should not be present in the tree, they are used to optimize the code.");
    }

    public ExpDefinition verifyIdentifier(EnvironmentExp localEnv) throws ContextualError {
        throw new DecacInternalError("SSAVariables should not be present in the tree, they are used to optimize the code.");
    }

    @Override
    public ClassDefinition getClassDefinition() {
        throw new DecacInternalError("SSAVariable should not be used with classes, only with fields and variables");
    }

    @Override
    public MethodDefinition getMethodDefinition() {
        throw new DecacInternalError("SSAVariable should not be used with methods, only with fields and variables");
    }

    @Override
    public boolean isSSAVariable(){
        return true;
    }
}
