package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;
import java.util.UUID;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class InstanceOf extends AbstractExpr {
    private AbstractExpr expr;
    private AbstractIdentifier type;

    public InstanceOf(AbstractExpr leftOperand, AbstractIdentifier rightOperand) {
        expr=leftOperand;
        type=rightOperand;
    }

    //@Override
    //protected String getOperatorName() {
      //  return "instanceof";
    //}

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        // Verify the left operand (object to check)
        Type leftType = expr.verifyExpr(compiler, localEnv, currentClass);

        // The left operand must be a class type or null
        if (!(leftType.isClassOrNull() )) {
            throw new ContextualError("Left operand of 'instanceof' must be an object or null. Rule 3.40.", getLocation());
        }

        // Verify the right operand

        Type rightType ;
        try {
            rightType = type.verifyType(compiler);
            type.setType(rightType);
        } catch (ContextualError e) {
            // Add error-specific logging here if needed
            throw new ContextualError("Right operand of 'instanceof' must be a class not a null type. Rule 3.40.", getLocation());

        }
        // The right operand must be a class type
        if (!rightType.isClass()) {
            throw new ContextualError("Right operand of 'instanceof' must be a class. Rule 3.40.", getLocation());
        }

        // The result of the 'instanceof' operation is always a boolean
        this.setType(compiler.environmentType.BOOLEAN);
        return this.getType();
    }


    @Override
    public void decompile(IndentPrintStream s) {
        //throw new UnsupportedOperationException("not yet implemented");
        s.print("(");
        expr.decompile(s);
        s.print(" instanceof ");
        type.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {

    }

    @Override
    protected void iterChildren(TreeFunction f) {

    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        compiler.addInstruction(new PUSH(Register.R2), "Save register used in loop"); // Save register 2 used for the loop
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R2), "Load Object class address"); // Load the address of the Object class in R2
        expr.codeGenInst(compiler); // Get the address of the object in R0

        UUID uuid = UUID.randomUUID();
        String labelID = uuid.toString().replace("-", "");// Convert the UUID to a hexadecimal string
        Label startLabel = new Label("instanceof_start" + labelID);
        Label endLabelFalse = new Label("instanceof_end_false" + labelID);
        Label endLabelTrue = new Label("instanceof_end_true" + labelID);
        Label endLabel = new Label("instanceof_end" + labelID);

        // Test for null pointer
        compiler.addInstruction(new CMP(new NullOperand(), Register.R0), "Test for null pointer");
        compiler.addInstruction(new BEQ(endLabelFalse), "Return false if null pointer");

        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0),
                "Load class address in method table"); // Load the address of the class in R0

        // Retrieve the class definition of the right operand
        ClassDefinition classDef = compiler.getClassDefinition(type.getType().getName(), getLocation());
        compiler.addInstruction(new LEA(classDef.getMethodTableAddr(), Register.R1), // Load the address of the class in R1 the one we want
                "Load the class with which we compare");                              // to check if the object is an instance of


        // Loop through the class hierarchy to check if the object is an instance of the class
        compiler.addLabel(startLabel);

        // If the class is found, branch to the end of the loop
        compiler.addInstruction(new CMP(Register.R0, Register.R1), "Check if we reached the class");
        compiler.addInstruction(new BEQ(endLabelTrue));

        // If we reach the Object class, the object is not an instance of the class
        compiler.addInstruction(new CMP(Register.R0, Register.R2), "Check if we reached the Object class");
        compiler.addInstruction(new BEQ(endLabelFalse));

        // Load the address of the superclass in R0
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0), "Load superclass address");
        compiler.addInstruction(new BRA(startLabel));

        compiler.addLabel(endLabelFalse);
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0), "Object is not an instance of the class");
        compiler.addInstruction(new BRA(endLabel));

        compiler.addLabel(endLabelTrue);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), Register.R0), "Object is an instance of the class");

        compiler.addLabel(endLabel);
        compiler.addInstruction(new POP(Register.R2), "Restore R2"); // Restore register 2
    }

}
