package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import org.apache.log4j.Logger;

/**
 *
 * @author gl34
 * @date 01/01/2025
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        for (AbstractDeclClass classDecl : getList()) {
            classDecl.verifyClass(compiler);
        }
        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClassMembers: start");
        for (AbstractDeclClass classDecl : getList()) {
            classDecl.verifyClassMembers(compiler);
        }
        LOG.debug("verify listClassMembers: end");
    }
    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClassBody: start");
        for (AbstractDeclClass classDecl : getList()) {
            classDecl.verifyClassBody(compiler);
        }
        LOG.debug("verify listClassBody: end");
    }


    public int genCodeMethodTable(DecacCompiler compiler, int offset) throws ContextualError {
        for (AbstractDeclClass classDecl : getList()) {
            offset = classDecl.genCodeMethodTable(compiler, offset);
        }
        return offset;
    }

    public void genCodeFieldInit(DecacCompiler compiler) {
        compiler.addComment("Field and method initialization");
        for (AbstractDeclClass classDecl : getList()) {
            compiler.addComment("Field initialisation for class " + classDecl.getName().getName());
            compiler.addLabel(new Label("init." + classDecl.getName().getName()));
            classDecl.genCodeFieldFullInit(compiler);
            compiler.addInstruction(new RTS());
            compiler.addComment("");
        }
    }

    public void genCodeMethodCode(DecacCompiler compiler) {
        compiler.addComment("Method code");
        compiler.addComment("Method code for class Object");
        compiler.genCodeEqualsMethod();
        for (AbstractDeclClass classDecl : getList()) {
            compiler.addComment("Methods codes for class " + classDecl.getName().getName());
            classDecl.genCodeMethodCode(compiler);
            compiler.addComment("");
        }
    }
}
