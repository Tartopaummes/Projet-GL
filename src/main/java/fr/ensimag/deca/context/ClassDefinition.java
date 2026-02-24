package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.AbstractDeclClass;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;
import org.apache.commons.lang.Validate;

/**
 * Definition of a class.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class ClassDefinition extends TypeDefinition {


    public void setNumberOfFields(int numberOfFields) {
        this.numberOfFields = numberOfFields;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void incNumberOfFields() {
        this.numberOfFields++;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int n) {
        Validate.isTrue(n >= 0);
        numberOfMethods = n;
    }
    
    public int incNumberOfMethods() {
        numberOfMethods++;
        return numberOfMethods;
    }

    public AbstractDeclClass getDeclClass() {
        return declClass;
    }

    public void setDeclClass(AbstractDeclClass declClass) {
        this.declClass = declClass;
    }

    public DAddr getMethodTableAddr() {
        return MethodTable;
    }

    public void setMethodTableAddr(DAddr addr) {
        MethodTable = addr;
    }

    private int numberOfFields = 0;
    private int numberOfMethods = 0;
    private AbstractDeclClass declClass;
    private DAddr MethodTable;
    
    @Override
    public boolean isClass() {
        return true;
    }
    
    @Override
    public ClassType getType() {
        // Cast succeeds by construction because the type has been correctly set
        // in the constructor.
        return (ClassType) super.getType();
    };

    public ClassDefinition getSuperClass() {
        return superClass;
    }

    private final EnvironmentExp members;
    private final ClassDefinition superClass; 

    public EnvironmentExp getMembers() {
        return members;
    }

    public ClassDefinition(ClassType type, Location location, ClassDefinition superClass) {
        super(type, location);
        EnvironmentExp parent;
        if (superClass != null) {
            parent = superClass.getMembers();
        } else {
            parent = null;
        }
        members = new EnvironmentExp(parent);
        this.superClass = superClass;
    }

}
