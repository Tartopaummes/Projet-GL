package fr.ensimag.deca.context;

import fr.ensimag.deca.DecacCompiler;
import java.util.HashMap;
import java.util.Map;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

// A FAIRE: étendre cette classe pour traiter la partie "avec objet" de Déca
/**
 * Environment containing types. Initially contains predefined identifiers, more
 * classes can be added with declareClass().
 *
 * @author gl34
 * @date 01/01/2025
 */
public class EnvironmentType {
    public EnvironmentType(DecacCompiler compiler) {
        
        envTypes = new HashMap<Symbol, TypeDefinition>();
        
        Symbol intSymb = compiler.createSymbol("int");
        INT = new IntType(intSymb);
        envTypes.put(intSymb, new TypeDefinition(INT, Location.BUILTIN));

        Symbol floatSymb = compiler.createSymbol("float");
        FLOAT = new FloatType(floatSymb);
        envTypes.put(floatSymb, new TypeDefinition(FLOAT, Location.BUILTIN));

        Symbol voidSymb = compiler.createSymbol("void");
        VOID = new VoidType(voidSymb);
        envTypes.put(voidSymb, new TypeDefinition(VOID, Location.BUILTIN));

        Symbol booleanSymb = compiler.createSymbol("boolean");
        BOOLEAN = new BooleanType(booleanSymb);
        envTypes.put(booleanSymb, new TypeDefinition(BOOLEAN, Location.BUILTIN));

        Symbol stringSymb = compiler.createSymbol("string");
        STRING = new StringType(stringSymb);
        // not added to envTypes, it's not visible for the user.

        //predef type Object
        OBJECT_SYMB = compiler.createSymbol("Object");
        OBJECT = new ClassType(OBJECT_SYMB, Location.BUILTIN, null);
        ClassDefinition objectClassDef = OBJECT.getDefinition();
        envTypes.put(OBJECT_SYMB, objectClassDef);
        //Declare env_exp_object with just the equals method in the Object's EnvironementExp
        Symbol equalsSymb = compiler.createSymbol("equals");
        Signature equalsSignature = new Signature();
        equalsSignature.add(OBJECT);
        objectClassDef.setNumberOfMethods(1);//There is only one method is the object class (equals)
        try {
            ((ClassDefinition) (envTypes.get(OBJECT_SYMB))).getMembers().declare(equalsSymb, new MethodDefinition(BOOLEAN, Location.BUILTIN, equalsSignature, 0));
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new InternalError(e.getMessage());
        }
    }

    private final Map<Symbol, TypeDefinition> envTypes;

    public TypeDefinition defOfType(Symbol s) {
        return envTypes.get(s);
    }

    /**
     * Add a new type to the type environment
     * @param s symbol of the type
     * @param d definition of the type
     * @return the previous definition of the type if it was already registered, or null if it was not
     */
    public TypeDefinition declareType(Symbol s, TypeDefinition d) {
        return envTypes.put(s, d);
    }

    public final VoidType    VOID;
    public final IntType     INT;
    public final FloatType   FLOAT;
    public final StringType  STRING;
    public final BooleanType BOOLEAN;
    public final ClassType OBJECT;
    public final Symbol OBJECT_SYMB;

}
