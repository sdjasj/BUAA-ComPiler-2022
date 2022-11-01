package SyntaxTree;

import MySymbolTable.SymbolTable;

public class CondNode extends ParserNode {
    private LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }
}
