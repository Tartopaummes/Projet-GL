package fr.ensimag.deca;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.instructions.*;

import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;

import java.util.LinkedList;

/**
 * Decac compiler instance.
 * <p>
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 * <p>
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl34
 * @date 01/01/2025
 */
public class DecacCompiler {
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);

    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        this.source = source;
    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     * java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }

    private final CompilerOptions compilerOptions;
    private final File source;
    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final IMAProgram program = new IMAProgram();


    /**
     * The global environment for types (and the symbolTable)
     */
    public final SymbolTable symbolTable = new SymbolTable(); // must create the SymbolTable first because it's use in the construction of EnvironmentType
    public final EnvironmentType environmentType = new EnvironmentType(this);


    public Symbol createSymbol(String name) {
        // return null; // A FAIRE: remplacer par la ligne en commentaire ci-dessous
        return symbolTable.create(name);
    }

    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String sourceFile = source.getAbsolutePath();
        String destFile = sourceFile.replaceFirst("\\.deca$", ".ass");
        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        try {
            return doCompile(sourceFile, destFile, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        }
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName name of the destination (assembly) file
     * @param out stream to use for standard output (output of decac -p)
     * @param err stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName,
                              PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);

        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }
        assert (prog.checkAllLocations());

        if (compilerOptions.getParse()) {
            //print the decompiled program
            System.out.println(prog.decompile());
            return false;
        }

        prog.verifyProgram(this);
        assert (prog.checkAllDecorations());
        if (compilerOptions.getVerification()) {
            // stop after verifications
            //just return
            return false;
        }

        addComment("start main program");
        prog.codeGenProgram(this);
        addComment("end main program");
        LOG.debug("Generated assembly code:" + nl + program.display());
        LOG.info("Output file assembly file is: " + destName);

        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(destName);
        } catch (FileNotFoundException e) {
            throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
        }

        LOG.info("Writing assembler file ...");

        program.display(new PrintStream(fstream));
        LOG.info("Compilation of " + sourceName + " successful.");
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err        Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError    When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     *                            compiler.
     *                            occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }

    /**
     * This method adds the code for the stack overflow error
     */
    private void addStackOverflowError() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("stackOverflow"));
            addInstruction(new WSTR("Error : Stack overflow"));
            addInstruction(new WNL());
            addInstruction(new ERROR());
        }
    }

    private void addNullDereferencingError() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("dereferencing.null"));
            addInstruction(new WSTR("Error : Null pointer dereferencing"));
            addInstruction(new WNL());
            addInstruction(new ERROR());
        }
    }


    /**
     * This method adds the code for the dereferencing of a null pointer error
     * The pointer should be in R2
     * Do nothing if the no-check option is set
     */
    public void codeGenNullDereferencing(GPRegister register) {
        if (!compilerOptions.getNo_check()) {
            addInstruction(new CMP(new NullOperand(), register));
            addInstruction(new BEQ(new Label("dereferencing.null")));
        }
    }


    /**
     * create a new TSTO and store it to change its value
     * according to the rest of the code
     * The TSTO is not generated if the option no_check if set
     */
    public void newTSTO() {
        if (!compilerOptions.getNo_check()) {
            TSTO tsto = new TSTO(0);
            addInstruction(tsto);
            addInstruction(new BOV(new Label("stackOverflow")));
            aliveTSTO.addFirst(tsto);
        }
    }

    private LinkedList<TSTO> aliveTSTO = new LinkedList<TSTO>();

    /**
     * Increment the current TSTO value
     * Do nothing if the no_check option is set
     *
     * @param memo
     */
    public void incrementTSTO(int memo) {
        if (!compilerOptions.getNo_check()) {
            aliveTSTO.getFirst().increment(memo);
        }
    }

    /**
     * Kill the current TSTO
     * Do nothing if the no_check option is set
     */
    public void endTSTO() {
        if (!compilerOptions.getNo_check()) {
            aliveTSTO.removeFirst().done();
        }
    }

    /**
     * allow program to know what registers are free and where to store values
     */
    private int minFreeRegister = 3;

    public int getMinFreeRegister() {
        return minFreeRegister;
    }

    public void setMinFreeRegister(int value) {
        minFreeRegister = value;
    }

    /**
     * Generate the code of the handlers for all execution errors if the option no_check is not set
     */
    public void codeGenExecutionErrorHandlers() {
        //generate all handler code for execution error
        if (!compilerOptions.getNo_check()) {
            addComment("Handlers for execution errors");
            addStackOverflowError();
            codeGenIntegerModulo0ErrorHandler();
            codeGenIntegerDivision0ErrorHandler();
            codeGenOverflowError();
            codeGenIOError();
            addNullDereferencingError();
            codeGenNoReturnInNonVoidError();
        }
    }

    /**
     * Generate the code for handle the Modulo by 0 error
     */
    private void codeGenIntegerModulo0ErrorHandler() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("integer_modulo_0_error"));
            //Print the message
            addInstruction(new WSTR(new ImmediateString("Error: Modulo by 0 encountered")));
            addInstruction(new WNL());

            //Exit the program with error
            addInstruction(new ERROR());
        }
    }

    /**
     * Generate the code for handle the division by 0 error
     */
    private void codeGenIntegerDivision0ErrorHandler() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("integer_division_0_error"));
            //Print the message
            addInstruction(new WSTR(new ImmediateString("Error: Division by 0 encountered")));
            addInstruction(new WNL());

            //Exit the program with error
            addInstruction(new ERROR());
        }
    }

    /**
     * Generate the code for handle the arithmetic overflow (or division by 0.O)
     */
    private void codeGenOverflowError() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("overflow_error"));
            //Print the message
            addInstruction(new WSTR(new ImmediateString("Error: Overflow during arithmetic operation")));
            addInstruction(new WNL());
            //Exit the program with error
            addInstruction(new ERROR());
        }
    }

    /**
     * Generate the code for input or output error
     */
    private void codeGenIOError() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("io_error"));
            //Print the message
            addInstruction(new WSTR(new ImmediateString("Error: Input/Output error")));
            addInstruction(new WNL());
            //Exit the program with error
            addInstruction(new ERROR());
        }
    }

    public void codeGenNoReturnInNonVoidError() {
        if (!compilerOptions.getNo_check()) {
            addLabel(new Label("no_return_in_non_void_error"));
            //Print the message
            addInstruction(new WSTR(new ImmediateString("Error: No return reached in a non-void method")));
            addInstruction(new WNL());
            //Exit the program with error
            addInstruction(new ERROR());
        }
    }

    /**
     * Generate method table for the object class (essential language)
     */
    public int genCodeMethodTableObject(int offset) {
        addComment("Generate method table for Object class");
        offset++;
        addInstruction(new LOAD(new NullOperand(), Register.R0));
        addInstruction(new STORE(Register.R0, new RegisterOffset(offset, Register.GB)));
        offset = genCodeMethodTableObjectEquals(offset);
        return offset;
    }

    public int genCodeMethodTableObjectEquals(int offset) {
        offset++;
        addInstruction(new LOAD(new LabelOperand(new Label("code.Object.equals")), Register.R0));
        addInstruction(new STORE(Register.R0, new RegisterOffset(offset, Register.GB)));
        return offset;
    }

    public ClassDefinition getClassDefinition(Symbol name, Location location) {
        try {
            return environmentType.defOfType(name).getType().asClassType("Expected a class definition for " + name + " but got something else", location).getDefinition();
        } catch (ContextualError e) {
            throw new DecacInternalError("Error in getClassDefinition");
        }
    }


    /**
     * Generate the code for  the equals method of the Object class
     */
    public void genCodeEqualsMethod() {
        addLabel(new Label("code.Object.equals"));
        addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R0)); // Load the first object (this)
        addInstruction(new LOAD(new RegisterOffset(-3, Register.LB), Register.R1)); // Load the second object (other)
        addInstruction(new CMP(Register.R0, Register.R1));
        addInstruction(new SEQ(Register.R0));
        addInstruction(new RTS());
        addComment("");
    }

    public void saveRegisters(int nbVariables) {
        for (int i = 2; i <= compilerOptions.getNbRegisters() - 1 && i <= nbVariables + 1; i++) {
            addInstruction(new PUSH(Register.getR(i)));
        }

    }

    public void restoreRegisters(int nbVariables) {
        int min = Math.min(compilerOptions.getNbRegisters() - 1, nbVariables + 1);
        for (int i = min; i >= 2 && i >= nbVariables + 1; i--) {
            addInstruction(new POP(Register.getR(i)));
        }
    }


}
