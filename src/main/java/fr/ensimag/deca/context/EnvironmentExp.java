package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;

import java.util.HashMap;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 * 
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 * 
 * The dictionary at the head of this list thus corresponds to the "current" 
 * block (eg class).
 * 
 * Searching a definition (through method get) is done in the "current" 
 * dictionary and in the parentEnvironment if it fails. 
 * 
 * Insertion (through method declare) is always done in the "current" dictionary.
 * 
 * @author gl34
 * @date 01/01/2025
 */
public class EnvironmentExp {
    // Data structure representing an environment (name -> definition association)
    //stacking is managed by parentEnvironment
    HashMap<Symbol, ExpDefinition> currentEnvironment;
    EnvironmentExp parentEnvironment;
    
    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
        this.currentEnvironment = new HashMap<>();
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;

        public DoubleDefException(String msg) {
            super(msg);
        }

        public DoubleDefException() {
            super();
        }
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     *
     * @param key symbol to get
     *
     * @return The ExpDefinition of the symbol, or null if the symbol is undefined
     */
    public ExpDefinition get(Symbol key) {
        if (currentEnvironment.containsKey(key)) {
            return currentEnvironment.get(key);
        }
        if (parentEnvironment != null) {
            return parentEnvironment.get(key);
        }
        return null;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined without checking the parent environment.
     *
     * @param key symbol to get
     *
     * @return The ExpDefinition of the symbol, or null if the symbol is undefined
     */
    public ExpDefinition getInEnv(Symbol key) {
        if (currentEnvironment.containsKey(key)) {
            return currentEnvironment.get(key);
        }
        return null;
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary 
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {
        if (currentEnvironment.containsKey(name)) {
            throw new DoubleDefException(name.getName() + " is already defined.");
        }
        currentEnvironment.put(name, def);
    }

}
