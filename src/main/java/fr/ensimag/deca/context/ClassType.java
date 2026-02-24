package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Type defined by a class.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class ClassType extends Type {

    protected ClassDefinition definition;

    public ClassDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
        this.definition = new ClassDefinition(this, location, superClass);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }


    @Override
    public boolean sameType(Type otherType) {
        return this.equals(otherType);
    }

    /**
     * Return true if potentialSuperType is a superclass of this class.
     */
    public boolean isSubTypeOf(ClassType potentialSuperType) {
        if (isNull()) {
            //For all class A, null is a subtype of type_class(A).
            return true;
        }
        if (this.definition.getSuperClass() == null) {
            return false;
        } else if (this.definition.getType().equals(potentialSuperType)) {
            return true;

        } else if (this.definition.getSuperClass().getType().equals(potentialSuperType)) {
            return true;
        } else {
            return this.definition.getSuperClass().getType().isSubTypeOf(potentialSuperType);
        }
    }


}
