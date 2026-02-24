package fr.ensimag.deca.tree;

import fr.ensimag.deca.SSA.AbstractBloc;
import fr.ensimag.deca.SSA.WhileBloc;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateBoolean;
import fr.ensimag.ima.pseudocode.Label;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import org.apache.commons.lang.Validate;
import java.util.UUID;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;;
    }

    @Override
    public boolean isWhile(){ return true;}

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        //generate a random identifier (hexa in 128 bits) for the While statement
        UUID uuid = UUID.randomUUID();
        // Convert the UUID to a hexadecimal string
        String labelId = uuid.toString().replace("-", "");
        Label label_while_condition = new Label("while_condition" + labelId);
        Label label_while_start = new Label("while_start" + labelId);

        // Branch to the condition
        compiler.addComment("While " + labelId + " : Branch to condition");
        compiler.addInstruction(new BRA(label_while_condition));

        // Start of the while loop
        compiler.addLabel(label_while_start);
        compiler.addComment("While " + labelId + " : Start of the loop");
        compiler.newTSTO();
        body.codeGenListInst(compiler);
        compiler.endTSTO();

        // Condition
        compiler.addLabel(label_while_condition);
        compiler.addComment("While " + labelId + " : Evaluate condition");
        condition.codeGenInst(compiler);
        compiler.addInstruction(new CMP(new ImmediateBoolean(true), Register.R0));
        compiler.addInstruction(new BEQ(label_while_start));


    }





    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
                              ClassDefinition currentClass, Type returnType, AbstractIdentifier methodOwnerName)
            throws ContextualError {
        // Verify the condition
        Type condType = condition.verifyExpr(compiler, localEnv, currentClass);
        if (!condType.isBoolean())
            throw new ContextualError("Condition must be of type boolean. Rule 3.29.", condition.getLocation());

        // Verify instructions of "boby"
        body.verifyListInst(compiler, localEnv, currentClass, returnType, methodOwnerName);

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

    public AbstractBloc genWhileBloc(AbstractBloc entry) {
        WhileBloc whileBloc = new WhileBloc(null, entry, condition);
        this.body.genGraph(whileBloc, whileBloc);
        return whileBloc;
    }


}
