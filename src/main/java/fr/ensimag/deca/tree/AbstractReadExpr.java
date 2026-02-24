package fr.ensimag.deca.tree;


import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.BOV;

/**
 * read...() statement.
 *
 * @author gl34
 * @date 01/01/2025
 */
public abstract class AbstractReadExpr extends AbstractExpr {

    public AbstractReadExpr() {
        super();
    }

    protected void codeGenInputError(DecacCompiler compiler) {
        if (!compiler.getCompilerOptions().getNo_check()) {
            compiler.addInstruction(new BOV(new Label("io_error")), "Overflow or syntax error check for previous operation");
        }
    }


}
