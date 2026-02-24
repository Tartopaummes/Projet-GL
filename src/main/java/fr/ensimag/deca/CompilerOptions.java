package fr.ensimag.deca;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl34
 * @date 01/01/2025
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }
    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();

    //Added, maybe not needed
    private boolean parse = false;
    private boolean verification = false;
    private boolean no_check = false;
    private int registers = 16;
    private boolean reg_set = false;
    public boolean optimize = false;


    public boolean getParse() {
        return this.parse;
    }
    public boolean getVerification() { return this.verification; }
    public boolean getNo_check() { return this.no_check; }
    public int getNbRegisters() { return this.registers; }
    public boolean getReg_set() { return this.reg_set; }





    public void parseArgs(String[] args) throws CLIException {
        // Test first argument for banner display
        if (args.length > 0 && args[0].equals("-b")) {
            if(args.length > 1){
                throw new CLIException("Invalid argument after -b, no argument expected, got : " + args[1]);
            }else{
                printBanner = true;
            }
            return;
        }

        int current = 0;
        //Scan options for actual arguments
        while (current < args.length) {
            String arg = args[current];
            if (current != 0 && arg.equals("-b")) { // -b can only be without other args
                throw new CLIException("Invalid argument before -b, no argument expected, got : " + args[current - 1]);
            } else if (arg.equals("-p")) {   //stop after tree building, and decompile it
                if(verification){
                    throw new CLIException("Options -p and -v are incompatible");
                } else {
                    if(parse){
                        throw new CLIException("Option -p already set");
                    } else {
                        parse = true;
                    }
                }

            } else if (arg.equals("-v")){   //stop after verifications
                if(parse){
                    throw new CLIException("Options -p and -v are incompatible");
                } else {
                    if(verification){
                        throw new CLIException("Option -v already set");
                    } else {
                        verification = true;
                    }
                }

            }else if (arg.equals("-o")){   //optimize option
                this.optimize = true;

            } else if (arg.equals("-n")){   //delete tests (from 11.1 and 11.3 in documentation)
                if(no_check){
                    throw new CLIException("Option -n already set");
                } else {
                    no_check = true;
                }

            } else if (arg.equals("-r")){   //specify the number of registers authorized
                if(reg_set){
                    throw new CLIException("Option -r already set to "+registers+", cannot set it to another value");
                } else {
                    reg_set = true;
                    if(current+1 >= args.length){
                        throw new CLIException("No argument after -r, integer between 4 and 16 expected");
                    }
                    try {
                        registers = Integer.parseInt(args[current+1]);
                        current++; //skip the next args because it has just been parsed
                        if ((registers < 4) || (registers > 16)){
                            throw new CLIException("Invalid argument after -r, integer between 4 and 16 expected, got : " + registers);
                        }
                    } catch (NumberFormatException e){
                        throw new CLIException("Invalid argument after -r, integer expected, got : " + args[current + 1]);
                    }
                }

            } else if (arg.equals("-d")){ // debug mode incrementation
                debug++; // Might need to add a limit

            } else if (arg.equals("-P")){ // parallel mode
                if(parallel){
                    throw new CLIException("Option -P already set");
                } else {
                    parallel = true;
                }

            } else { //Files arguments
                startParseFile(current, args.length, args);
                break; //Stop parsing options after files
            }
            current++;
        }

        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }
    }


    /**
     * Method to parse files from the command line arguments. This method will parse all files until the end of the arguments, or until a non-file argument is found.
     * @param start
     * @param end
     * @param args
     * @throws CLIException
     */
    protected void startParseFile(int start, int end, String[] args) throws CLIException {
        for(int i = start; i < end; i++){
            File file = new File(args[i]);
            if (!file.exists()) {
                throw new CLIException("File " + args[i] + " does not exist");
            }
            if (!sourceFiles.contains(file)){ //Avoid duplicates
                sourceFiles.add(file);
            }
        }

    }

    protected void displayUsage() {
        System.out.println("usage: decac [[-p | -v] [-n] [-r X] [-d]* [-P] [-o] <deca file>...] | [-b]");
        System.out.println(
                "\n" +
                        "   -b   (banner)        displays banner with team name\n" +
                        "   -p   (parse)         stops decac after the tree-building stage, and displays\n" +
                        "                        the tree decompiled\n" +
                        "   -v   (verification)  stops decac after the verification step (produces no\n" +
                        "                        output in the absence of errors)\n" +
                        "   -n   (no check)      suppresses runtime tests\n" +
                        "   -r X (registers)     limits the number of registers available\n" +
                        "                        to R0 ... R{X-1}, with 4 <= X <= 16\n" +
                        "   -d   (debug)         enable debug traces. Repeat the option several times to\n" +
                        "                        get more traces\n" +
                        "   -P   (parallel)      if there are several source files, launches compilation\n" +
                        "                        of the files in parallel (to speed up compilation)\n" +
                        "   -o   (optimize)      generates optimized assembler code\n");
    }
}
