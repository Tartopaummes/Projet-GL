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
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import fr.ensimag.ima.pseudocode.instructions.INT;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

public class Cast extends AbstractExpr {

    private AbstractIdentifier type;
    private AbstractExpr expr;

    public Cast(AbstractIdentifier type, AbstractExpr expr) {
        Validate.notNull(type);
        Validate.notNull(expr);
        this.type = type;
        this.expr = expr;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        type.decompile(s);
        s.print(") (");
        expr.decompile(s);
        s.print(")");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        expr.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
    }
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        // Verify if the target type exists (T2) exist
        Type targetType = type.verifyType(compiler);
        type.setType(targetType);

        // Vérifier expression to cast and get her type (T1)
        Type exprType = expr.verifyExpr(compiler, localEnv, currentClass);

        // Vérifier compatibility for conversion
        if (exprType.isVoid()) {
            throw new ContextualError("Invalid cast: cannot cast from void. Rule 3.39.", getLocation());
        }
        else if (isAssignCompatible(compiler.environmentType, exprType, targetType)) {   // Compatibility for affectation : T1 -> T2
            setType(targetType);
            return targetType;
        }
        else{
            throw new ContextualError("Invalid cast from " + exprType + " to " + targetType + ". Rule 3.39.", getLocation());}

    }

    /**
     * Vérifie la compatibilité pour l'affectation entre deux types (assign_compatible).
     * T1 -> T2 est assign_compatible si :
     * - T2 est float et T1 est int ;
     * - T2 est un supertype de T1 dans l'environnement de types.
     */
    private boolean isAssignCompatible(EnvironmentType env, Type T1, Type T2) throws ContextualError {
        if (T2.isTypeArith() && T1.isTypeArith() && T2.isTypeArith()) {
            return true;
        }

        if (T1.sameType(T2)) {
            return true;
        }
        if (T1.isClass() && T2.isClass()) {
            ClassType classT1 = T1.asClassType("Invalid type: T1 is not a class type.", getLocation());
            ClassType classT2 = T2.asClassType("Invalid type: T2 is not a class type.", getLocation());
            if (classT1.isSubTypeOf(classT2) || classT2.isSubTypeOf(classT1)) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void codeGenPrint(DecacCompiler compiler) {
        if (type.getType().isFloat()) {
            FloatType.codeGenPrint(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }

    @Override
    public void codeGenPrintHex(DecacCompiler compiler) {
        if (type.getType().isFloat()) {
            FloatType.codeGenPrintHex(compiler);
        } else {
            IntType.codeGenPrint(compiler);
        }
    }

    @Override
    public void codeGenInst(DecacCompiler compiler) {
        expr.codeGenInst(compiler);
        if (type.getType().isFloat() && expr.getType().isInt()) {
            compiler.addInstruction(new FLOAT(Register.R0, Register.R0), "Converting int to float");
        }
        if (type.getType().isInt() && expr.getType().isFloat()) {
            compiler.addInstruction(new INT(Register.R0, Register.R0), "Converting float to int");
        }
        //TODO for the moment the cast checking is done only in part B (before execution), so the execution error "impossible cast" will never happen.
        //Maybe add codegen to check the cast at the execution. (cf p224)
    }

}
