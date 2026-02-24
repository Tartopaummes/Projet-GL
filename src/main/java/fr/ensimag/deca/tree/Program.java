package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.*;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");
        //throw new UnsupportedOperationException("not yet implemented");

        //Phase 1
        classes.verifyListClass(compiler);

        //Phase 2
        classes.verifyListClassMembers(compiler);

        //Phase 3
        //Class body verification
         classes.verifyListClassBody(compiler);
        // Main verification
        main.verifyMain(compiler);
        LOG.debug("verify program: end");
    }

    @Override
    public void codeGenProgram(DecacCompiler compiler) {
        // A FAIRE: compléter ce squelette très rudimentaire de code


        /** Methode Table Création (Pass 1) */
        compiler.addComment("Method table");
        // Generate the method table for the Object class
        int offset = 0;
        offset = compiler.genCodeMethodTableObject(offset);
        compiler.addComment("");

        // Generate the methode table for the other classes
        try {
            offset = classes.genCodeMethodTable(compiler, offset);
        } catch (ContextualError e) {
            e.printStackTrace();
            return;
        }

        // Generate the code for the classes
        //classes.codeGenListClass(compiler); // Generating the classes code

        // Generate the code for the main block
        compiler.addComment("Start of main program");
        main.codeGenMain(compiler, offset); // Generating the main code variables will be stored right after the method table (offset + 1)
        compiler.addComment("End of main program");
        compiler.addInstruction(new HALT());

        //Errors
        compiler.codeGenExecutionErrorHandlers();

        /** Field Initialization (Pass2) */
        classes.genCodeFieldInit(compiler);

        /** Method Encoding (Pass 2) */
        classes.genCodeMethodCode(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
