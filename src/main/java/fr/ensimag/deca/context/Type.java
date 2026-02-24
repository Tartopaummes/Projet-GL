package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;

/**
 * Deca Type (internal representation of the compiler)
 *
 * @author gl34
 * @date 01/01/2025
 */

public abstract class Type {


    /**
     * True if this and otherType represent the same type (except for float and int : int is same type as float and int; but float is not same type as int)   (in the case of
     * classes, this means they represent the same class).
     */
    public abstract boolean sameType(Type otherType);

    /**
     * True if this and otherType represent the same type (in the case of
     * classes, this means they represent the same class).
     */
    public boolean exactSameType(Type otherType) {
        return sameType(otherType);
    }

    private final Symbol name;

    public Type(Symbol name) {
        this.name = name;
    }

    public Symbol getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    public boolean isClass() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isNull() {
        return false;
    }

    /**
     * @return true if the type is Float or Int
     */
    public boolean isTypeArith() {
        return isFloat() || isInt();
    }
    public boolean isClassOrNull() {
        return false;
    }

    /**
     * Returns the same object, as type ClassType, if possible. Throws
     * ContextualError(errorMessage, l) otherwise.
     *
     * Can be seen as a cast, but throws an explicit contextual error when the
     * cast fails.
     */
    public ClassType asClassType(String errorMessage, Location l)
            throws ContextualError {
        throw new ContextualError(errorMessage, l);
    }

}
