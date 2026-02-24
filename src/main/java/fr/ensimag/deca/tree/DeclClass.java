package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Class declaration.
 *
 * @author gl34 - mattéo
 * @date 07/01/2025
 */
public class DeclClass extends AbstractDeclClass {

    private ClassDefinition superClassDefinition = null; //definition of the super class. is set in verifyClass

    public DeclClass(AbstractIdentifier name, AbstractIdentifier superClass) {
        Validate.notNull(name);
        this.name = name;
        this.superClass = superClass;
        fields = new ListDeclField();
        methods = new ListDeclMethod();
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        //set the superclass definition
        if (superClass == null) {
            // the class extend the predef Object class
            superClassDefinition = (ClassDefinition) compiler.environmentType.defOfType(compiler.environmentType.OBJECT_SYMB);

            //no verification because the Object class is always defined in the environmentType
        } else {
            //the class extends another defined class
            if (compiler.environmentType.defOfType(superClass.getName()) == null) {
                throw new ContextualError("Super class " + superClass.getName() + " not declared. Rule 1.3.", getLocation());
            }
            TypeDefinition type = compiler.environmentType.defOfType(superClass.getName());
            if (!type.isClass()) {
                throw new ContextualError("Super class " + superClass.getName() + " is not a class identifier. Rule 1.3.", getLocation());
            }
            superClassDefinition = (ClassDefinition) type;
            superClass.setDefinition(superClassDefinition);
        }
        //Set the definition of this class
        ClassType classType = new ClassType(name.getName(), getLocation(), superClassDefinition);
        ClassDefinition classDef = classType.getDefinition();

        classDef.setDeclClass(this);
        //initialize the numbers of fields of this class from the super class
        classDef.setNumberOfFields(superClassDefinition.getNumberOfFields());
        classDef.setNumberOfMethods(superClassDefinition.getNumberOfMethods());

        if (compiler.environmentType.declareType(name.getName(), classDef) != null) {
            throw new ContextualError("Class " + name.getName() + " already declared. Rule 1.3.", getLocation());
        }
        name.setDefinition(classDef);
    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler) throws ContextualError {
        Validate.notNull(superClassDefinition);
        verifyClassMembers(compiler, superClassDefinition);
    }

    @Override
    protected void codeGenClass(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        name.prettyPrint(s, prefix, false);
        if (superClass != null) {
            superClass.prettyPrint(s, prefix, false);
        }
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        name.iter(f);
        if (superClass != null) {
            superClass.iter(f);
        }
        fields.iter(f);
        methods.iter(f);
    }

    @Override
    public int genCodeMethodTableLEA(DecacCompiler compiler, int offset) {
        offset++;
        if (superClass != null) {
            // LEA instruction with the address of the method table of the superclass
            compiler.addInstruction(new LEA(compiler.getClassDefinition(superClass.getName(), superClass.getLocation()).getMethodTableAddr(), Register.R0));
            DAddr addr = new RegisterOffset(offset, Register.GB);
            // Set the address of the method table in the class definition
            compiler.getClassDefinition(name.getName(), getLocation()).setMethodTableAddr(addr);
            compiler.addInstruction(new STORE(Register.R0, addr));
        } else {
            compiler.addInstruction(new LEA(new RegisterOffset(1, Register.GB), Register.R0));
            DAddr addr = new RegisterOffset(offset, Register.GB);
            compiler.getClassDefinition(name.getName(), getLocation()).setMethodTableAddr(addr);
            compiler.addInstruction(new STORE(Register.R0, new RegisterOffset(offset, Register.GB)));
        }
        return offset;
    }

    @Override
    public int genCodeMethodTableSuper(DecacCompiler compiler, int offset) throws ContextualError {
        if (superClass != null) {
            // Generate the method table of the superclass
            offset = superClassDefinition.getDeclClass().genCodeMethodTableSuper(compiler, offset);

            return superClassDefinition.getDeclClass().genCodeMethodTableClass(compiler, offset);
        } else {
            return compiler.genCodeMethodTableObjectEquals(offset);
        }
    }

    @Override
    protected void genCodeFieldInitSuper(DecacCompiler compiler) {
        if(superClass != null) {
            // Generate the field initialization of the superclass
            superClassDefinition.getDeclClass().genCodeFieldInit(compiler);
        } else {
            // Nothing to do
        }
    }

}
