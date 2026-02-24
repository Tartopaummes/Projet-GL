package fr.ensimag.deca.tree;

import fr.ensimag.deca.CompilerOptions;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TestAll {
    public static void main(String[] args) throws IOException {
        // Directory containing the test files
        Path testFilesDir = Paths.get("src/test/deca/syntax/valid");
        String GREEN = "\033[0;32m";   // GREEN
        String RESET = "\033[0m";  // Text Reset
        String RED = "\033[0;31m";     // RED
        String PURPLE = "\033[0;35m";  // PURPLE

        // Get all .deca files in the directory and subdirectories
        List<Path> testFiles = Files.walk(testFilesDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".deca"))
                .collect(Collectors.toList());

        // Process each file one by one
        for (Path entry : testFiles) {
            System.out.println(PURPLE + "======================================" + RESET);
            System.out.println(PURPLE + "Processing file: " + RESET + entry.getFileName());
            System.out.println(PURPLE + "======================================" + RESET);

            // Read the input file
            try (InputStream is = Files.newInputStream(entry)) {
                // Create a lexer and parser
                DecaLexer lexer = new DecaLexer(CharStreams.fromStream(is));
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                DecaParser parser = new DecaParser(tokens);

                // Create a new DecacCompiler
                DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), entry.toFile());

                // Set the compiler in the parser
                parser.setDecacCompiler(compiler);

                // Parse the program and get the tree
                AbstractProgram tree = parser.parseProgramAndManageErrors(System.err);

                // Check if the tree is null
                if (tree == null) {
                    System.out.println(RED + "======================================" + RESET);
                    System.err.println("Parsing failed, the tree is null.");
                    System.out.println(RED + "======================================" + RESET);
                    System.exit(1);
                } else {
                    // Print the tree
                    System.out.println(tree.prettyPrint());
                    tree.checkAllLocations();
                    System.out.println(GREEN + "======================================" + RESET);
                    System.out.println(GREEN + "File processed successfully: " + RESET + entry.getFileName() + " and all locations checked");
                    System.out.println(GREEN + "======================================" + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "======================================" + RESET);
                System.err.println(RED + "Error processing file: " + RESET + entry.getFileName());
                System.out.println(RED + "======================================" + RESET);
                e.printStackTrace();
                System.exit(1);
            }
        }
        System.out.println(GREEN + "======================================" + RESET);
        System.out.println(GREEN + "All files processed successfully" + RESET);
        System.out.println(GREEN + "======================================" + RESET);
    }
}