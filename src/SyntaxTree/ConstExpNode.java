package SyntaxTree;

import MySymbolTable.SymbolTable;

import java.io.IOException;

public class ConstExpNode extends ParserNode {
    private AddExpNode addExpNode;

    public ConstExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public int getConstVal(SymbolTable symbolTable) {
        return addExpNode.getConstVal(symbolTable);
    }
}
