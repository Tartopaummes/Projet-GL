package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

public class ClassicDeclMethod extends AbstractDeclMethod {
    private ListDeclVar variables;
    private ListInst instructions;

    public ClassicDeclMethod(AbstractDeclClass owner, AbstractIdentifier type, AbstractIdentifier name, ListDeclParam params, ListDeclVar variables, ListInst instructions) {
        Validate.notNull(owner);
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(params);
        Validate.notNull(variables);
        Validate.notNull(instructions);
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.params = params;
        this.variables = variables;
        this.instructions = instructions;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println();
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(") {");
        s.println();
        s.indent();
        variables.decompile(s);
        instructions.decompile(s);
        s.unindent();
        s.print("}");
    }

    public void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, EnvironmentExp paramEnv, ClassDefinition currentClass, Type returnType) throws ContextualError {
        variables.verifyListDeclVariable(compiler, paramEnv, currentClass);
        instructions.verifyListInst(compiler, paramEnv, currentClass, returnType, this.name);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        variables.prettyPrint(s, prefix, false);
        instructions.prettyPrint(s, prefix, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        name.iter(f);
        params.iter(f);
        variables.iter(f);
        instructions.iter(f);
    }

    @Override
    public void genCodeMethodCode(DecacCompiler compiler) {
        compiler.addComment("Code for method " + name.getName());
        compiler.addLabel(new Label("code." + owner.getName().getName().toString() + "." + name.getName()));
        compiler.newTSTO();
        compiler.addComment("Save registers");
        //compiler.saveRegisters(variables.size() + 1); // +1 for this (implicit parameter)

        // Load this in R2
        compiler.addInstruction(new PUSH(Register.R2));
        compiler.incrementTSTO(1);
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R2));

        //DAddr thisAddr = new RegisterOffset(-2, Register.LB);
        //compiler.addInstruction(new LOAD(thisAddr, Register.R2));
        // Tell the compiler that this is the operand of this (for simplicity this is always at offset -2)


        loadParameters(compiler);
        compiler.addComment("Code for method");
        variables.codeGenDeclVar(compiler, Register.LB);
        instructions.codeGenListInst(compiler);
        if (!compiler.getCompilerOptions().getNo_check() && !type.getType().isVoid()) {
            compiler.addInstruction(new BRA(new Label("no_return_in_non_void_error")));
        }

        compiler.addLabel(new Label("end." + owner.getName().getName().toString() + "." + name.getName()));
        compiler.addComment("Restore registers");
        //compiler.restoreRegisters(variables.size() + 1);
        compiler.endTSTO();
        compiler.addInstruction(new POP(Register.R2));
        compiler.addInstruction(new RTS()); // return
    }

    private void loadParameters(DecacCompiler compiler) {
        compiler.addComment("Load parameters");
        // load the first parameters in the registers until max register
        int offset = 3;
        for (AbstractDeclParam param : params.getList()) {
            DAddr paramAddr = new RegisterOffset(-offset, Register.LB);
            //compiler.addInstruction(new LOAD(paramAddr, Register.getR(offset))); not yet
            param.name.getExpDefinition().setOperand(paramAddr);
            offset++;
        }
    }

}
