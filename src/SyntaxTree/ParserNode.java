package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;

import java.io.IOException;

public class ParserNode {
    protected int blockDepth;
    protected SymbolTable RecordSymbolTable;

    public ParserNode() {

    }

    public void setBlockDepth(int blockDepth) {
        this.blockDepth = blockDepth;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {}

    public void setSymbolTable(SymbolTable symbolTable) {
        this.RecordSymbolTable = symbolTable;
    }


}
