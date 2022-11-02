package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;

public class CondNode extends ParserNode {
    private LOrExpNode lOrExpNode;

    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor, String trueLable,
                                     String falseLable) {
        lOrExpNode.generateIntermediate(intermediateVisitor, trueLable, falseLable);
    }
}
