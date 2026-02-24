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

public class TestSingleFile {
    public static void main(String[] args) throws IOException {

        // Get the file name from the command-line argument
        String fileName = "while/many.deca"; // TODO: Change this to the file_path you want to test
        Path filePath = Paths.get("src/test/deca/syntax/valid", fileName);

        // Check if the file exists
        if (!Files.exists(filePath)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }

        System.out.println("Processing file: " + filePath.getFileName());

        // Read the input file
        try (InputStream is = Files.newInputStream(filePath)) {
            // Create a lexer and parser
            DecaLexer lexer = new DecaLexer(CharStreams.fromStream(is));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            DecaParser parser = new DecaParser(tokens);

            // Create a new DecacCompiler
            DecacCompiler compiler = new DecacCompiler(new CompilerOptions(), filePath.toFile());

            // Set the compiler in the parser
            parser.setDecacCompiler(compiler);

            // Parse the program and get the tree
            AbstractProgram tree = parser.parseProgramAndManageErrors(System.err);

            // Check if the tree is null
            if (tree == null) {
                System.err.println("Parsing failed, the tree is null.");
            } else {
                // Print the tree
                System.out.println(tree.prettyPrint());
                tree.checkAllLocations();
            }
        } catch (Exception e) {
            System.err.println("Error processing file: " + filePath.getFileName());
            e.printStackTrace();
        }
    }
}