package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.SSA.IfBloc;
import fr.ensimag.deca.SSA.JoinBloc;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;
import java.util.UUID;

import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

/**
 * Full if/else if/else statement.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class IfThenElse extends AbstractInst {
    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    @Override
    public boolean isIfThenElse(){
        return true;
    }

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    /**
     * Constructor for the IfThenElse class.
     * THIS CONSTRUCTOR SHOULD ONLY BE USED BY THE PARSER WHEN CREATING THE TREE FOR IF_THEN_ELSE.
     *
     * @param condition The condition expression for the if statement.
     * @param thenBranch The list of instructions to execute if the condition is true.
     * @throws NullPointerException if condition or thenBranch is null
     */
    public IfThenElse(AbstractExpr condition, ListInst thenBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = null;
    }

    public void setElseBranch(ListInst elseBranch) {
        this.elseBranch = elseBranch;
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType, AbstractIdentifier methodOwnerName) throws ContextualError {
        // Verify the condition
        Type condType = condition.verifyExpr(compiler, localEnv, currentClass);
        if (!condType.isBoolean()) {
            throw new ContextualError("Condition must be of type boolean. Rule 3.29.", condition.getLocation());
        }

        // Verify instructions of branch "then"
        thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType, methodOwnerName);

        // Verify instructions of branch  "else" if exists
        if (elseBranch != null) {
            elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType, methodOwnerName);
        }
    }


    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        //generate a random identifier (hexa in 128 bits) for the ifThenElse statement
        UUID uuid = UUID.randomUUID();
        // Convert the UUID to a hexadecimal string
        String labelId = uuid.toString().replace("-", "");
        Label label_else = new Label("else_" + labelId);
        Label label_end_if = new Label("end_if_" + labelId);
        //evaluate the condition and branch depending on the result
        compiler.addComment("if " + labelId + ": Evaluate condition");

        condition.codeGenInst(compiler);

        compiler.addInstruction(new CMP(new ImmediateBoolean(false), Register.R0));
        compiler.addInstruction(new BEQ(label_else));

        //then
        compiler.addComment("then");
        // One TSTO per branch, evaluate the condition is part of the main program
        compiler.newTSTO();
        thenBranch.codeGenListInst(compiler);
        compiler.endTSTO();

        //branch to the end of the if
        compiler.addInstruction(new BRA(label_end_if));
        //else
        compiler.addLabel(label_else);
        compiler.addComment("else ");
        compiler.newTSTO();
        elseBranch.codeGenListInst(compiler);
        compiler.endTSTO();

        //end_if
        compiler.addLabel(label_end_if);
    }



    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if (");
        condition.decompile(s);
        s.print(") {");
        s.indent();
        s.println();
        thenBranch.decompile(s);
        s.unindent();
        s.print("} else {");
        s.indent();
        s.println();
        elseBranch.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }

    public JoinBloc genIfBloc(ListInst listInst, AbstractBloc entry) {
        IfBloc ifBloc = new IfBloc(listInst, entry, condition);
        JoinBloc joinBloc = new JoinBloc(null, null);
        joinBloc.setIfBloc(ifBloc);
        this.thenBranch.genGraph(ifBloc, joinBloc);
        this.elseBranch.genGraph(ifBloc, joinBloc);

        return joinBloc;
    }
}
