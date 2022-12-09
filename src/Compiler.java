import IntermediateCode.IntermediateVisitor;
import Lexer.Lexer;

import java.io.File;
import java.io.IOException;
import Lexer.Token;
import MipsCode.MipsVisitor;
import MipsCode.MulOptimizer;
import Parser.Parser;
import MySymbolTable.SymbolTable;
import SyntaxTree.CompUnitNode;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Compiler {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File filePath = new File("testfile.txt");
        byte[] bytes = Files.readAllBytes(Paths.get(filePath.getAbsolutePath()));
        String program = new String(bytes, StandardCharsets.UTF_8);
        Lexer lexer = new Lexer(program);
        ArrayList<Token> tokens = lexer.analyzeLexicals();
//        File file = new File("error.txt");
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        PrintStream ps = new PrintStream("error.txt");
//        System.setErr(ps);
//        File file = new File("output.txt");
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        PrintStream ps = new PrintStream("output.txt");
//        System.setOut(ps);
        SymbolTable symbolTable = new SymbolTable();
        Parser parser = new Parser(tokens);
        parser.setOutputParser(false);
        parser.setSymbolTable(symbolTable);
        CompUnitNode compUnitNode = parser.parseCompUnit();
        IntermediateVisitor intermediateVisitor = new IntermediateVisitor();
        compUnitNode.generateIntermediate(intermediateVisitor);
        intermediateVisitor.optimize();

        File file = new File("output.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintStream ps = new PrintStream("output.txt");
        System.setOut(ps);

        intermediateVisitor.output();
//        intermediateVisitor.testPrint();

        MulOptimizer.init();
        MipsVisitor mipsVisitor = new MipsVisitor();
        intermediateVisitor.IntermediateToMips(mipsVisitor);
        file = new File("mips.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        ps = new PrintStream("mips.txt");
        System.setOut(ps);
        mipsVisitor.output();
        ps.close();
    }
}
