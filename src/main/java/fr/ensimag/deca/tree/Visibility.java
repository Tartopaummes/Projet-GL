package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.IndentPrintStream;


/**
 * Visibility of a field.
 *
 * @author gl34
 * @date 01/01/2025
 */

public enum Visibility {
    PUBLIC(""),
    PROTECTED("protected ");

    private final String str;

    private Visibility(String str) {
        this.str = str;
    }

    public void decompile(IndentPrintStream s) {
        s.print(this.str);
    }
}
