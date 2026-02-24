package fr.ensimag.deca.tree;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.InlinePortion;
import fr.ensimag.ima.pseudocode.Label;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class AsmDeclMethod extends AbstractDeclMethod {
    private String code;

    public AsmDeclMethod(AbstractDeclClass owner, AbstractIdentifier type, AbstractIdentifier name, ListDeclParam params, String code) {
        Validate.notNull(owner);
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(params);
        Validate.notNull(code);
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.params = params;
        this.code = code;
    }

    public void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv, EnvironmentExp paramEnv, ClassDefinition currentClass, Type returnType) throws ContextualError {
        // Do nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println();
        type.decompile(s);
        s.print(" ");
        name.decompile(s);
        s.print("(");
        params.decompile(s);
        s.print(")");
        s.indent();
        s.println("asm(\"" + code + "\");");
        s.unindent();
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        name.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        s.println("Assembly code: " + code);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        //nothing to do
    }

    @Override
    public void genCodeMethodCode(DecacCompiler compiler) {
        // just print the code
        compiler.addComment("Code for method " + name.getName());
        compiler.addLabel(new Label("code." + owner.getName().getName().toString() + "." + name.getName()));
        //parse the EOL
        code = code.replace("\\n", "\n");
        String[] allLines = code.split("\n");
        for (String line : allLines) {
            compiler.add(new InlinePortion(line));
        }
    }
}
