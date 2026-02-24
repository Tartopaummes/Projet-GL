package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl34
 * @date 01/01/2025
 */
public class Signature {
    List<Type> args = new ArrayList<Type>();

    public void add(Type t) {
        args.add(t);
    }
    
    public Type paramNumber(int n) {
        return args.get(n);
    }
    
    public int size() {
        return args.size();
    }

    @Override
    public boolean equals(Object other) {
        //if each param has the same type in the 2 signatures, the signatures are equal
        if (other instanceof Signature) {
            Signature otherSig = (Signature) other;
            if (this.size() != otherSig.size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                Type arg = paramNumber(i);
                Type otherArg = otherSig.paramNumber(i);
                if (!arg.exactSameType(otherArg)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
