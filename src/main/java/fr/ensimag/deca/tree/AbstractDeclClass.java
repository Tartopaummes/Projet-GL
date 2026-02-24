package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */
public abstract class AbstractDeclClass extends Tree {

    /**
     * Pass 1 of [SyntaxeContextuelle]. Verify that the class declaration is OK
     * without looking at its content.
     */
    protected abstract void verifyClass(DecacCompiler compiler)
            throws ContextualError;

    /**
     * Pass 2 of [SyntaxeContextuelle]. Verify that the class members (fields and
     * methods) are OK, without looking at method body and field initialization.
     */
    protected abstract void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError;

    protected void verifyClassMembers(DecacCompiler compiler, ClassDefinition superClass)
            throws ContextualError {
        // Check if the class is declared
        if (compiler.environmentType.defOfType(name.getName()) == null) {
            throw new DecacInternalError("Pass 1 malfunctioned, class " + name.getName() + " not declared.");
        }
        // Check if the class is a class
        TypeDefinition classType = compiler.environmentType.defOfType(name.getName());
        if (!classType.isClass()) {
            throw new DecacInternalError("Class " + name.getName() + " is not a class identifier.");
        }
        ClassDefinition currentClass = (ClassDefinition) classType;
        // Verify the fields and methods
        fields.verifyListDeclField(compiler, currentClass, superClass);
        methods.verifyListDeclMethod(compiler, currentClass, superClass);

        // Update the class definition with the fields and methods
        compiler.environmentType.declareType(name.getName(), currentClass);
    }

    /**
     * Pass 3 of [SyntaxeContextuelle]. Verify that instructions and expressions
     * contained in the class are OK.
     *
     * @param compiler The compiler, contains the type environment
     */
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
            TypeDefinition classType = compiler.environmentType.defOfType(name.getName());
            if (!classType.isClass()) {
                throw new DecacInternalError("Class " + name.getName() + " is not a class identifier. Rule 3.5.");
            }
            ClassDefinition currentClass = (ClassDefinition) classType;
            EnvironmentExp localEnv = currentClass.getMembers();
            if (localEnv == null) {
                throw new DecacInternalError("Class " + name.getName() + " has no defined type. It should have been generated in pass 1 and 2. Rule 3.5");
            }
            fields.verifyListDeclFieldInit(compiler, localEnv, currentClass);
            methods.verifyListDeclMethodInit(compiler, localEnv, currentClass);
    }

    protected abstract void codeGenClass(DecacCompiler compiler);

    protected AbstractIdentifier name;
    protected AbstractIdentifier superClass; //superClass is null if the class has no explicit super class (i.e. its super class is Object)
    protected ListDeclField fields;
    protected ListDeclMethod methods;

    public void addField(AbstractDeclField field) {
        fields.add(field);
    }

    public void addMethod(AbstractDeclMethod method) {
        methods.add(method);
    }


    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        name.decompile(s);
        s.print(" extends ");
        if (superClass != null) {
            superClass.decompile(s);
        } else {
            s.print("Object");
        }
        s.print("{");
        s.println();
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.print("}");
    }


    public AbstractIdentifier getName() {
        return name;
    }

    /**
     * Generate the code for all the method table of the class and return the offset of the last method
     *
     * @param compiler
     * @param offset
     * @return the offset of the last method
     */
    public int genCodeMethodTable(DecacCompiler compiler, int offset) throws ContextualError {
        compiler.addComment("Method table for class " + name.getName());
        offset = genCodeMethodTableLEA(compiler, offset); // Generate the LEA instruction

        offset = genCodeMethodTableSuper(compiler, offset); // Generate the method table of the superclass recursively

        offset = genCodeMethodTableClass(compiler, offset); // Generate the method table of the current class
        compiler.addComment("");
        return offset;

    }

    public abstract int genCodeMethodTableLEA(DecacCompiler compiler, int offset) throws ContextualError;

    public abstract int genCodeMethodTableSuper(DecacCompiler compiler, int offset) throws ContextualError;

    public int genCodeMethodTableClass(DecacCompiler compiler, int offset) {
        for (AbstractDeclMethod method : methods.getList()) { // Generate the method table of the current class
            offset = method.genCodeMethodTable(compiler, offset, name.getName().getName());
        }
        return offset;
    }


    public void genCodeFieldFullInit(DecacCompiler compiler) {
        genCodeFieldImplicitInit(compiler);
        genCodeFieldInitSuper(compiler);
        for (AbstractDeclField field : fields.getList()) {
            field.genCodeFieldInit(compiler);
            compiler.addComment("");
        }
    }

    public void genCodeFieldInit(DecacCompiler compiler) {
        genCodeFieldInitSuper(compiler);
        for (AbstractDeclField field : fields.getList()) {
            field.genCodeFieldInit(compiler);
            compiler.addComment("");
        }
    }

    protected abstract void genCodeFieldInitSuper(DecacCompiler compiler);

    public void genCodeMethodCode(DecacCompiler compiler) {
        for (AbstractDeclMethod method : methods.getList()) {
            method.genCodeMethodCode(compiler);
            compiler.addComment("");
        }
    }

    private void genCodeFieldImplicitInit(DecacCompiler compiler) {
        compiler.addComment("Implicit Initialisation of fields");
        for (AbstractDeclField field : fields.getList()) {
            field.genCodeFieldImplicitInit(compiler);
            compiler.addComment("");
        }
    }
}
