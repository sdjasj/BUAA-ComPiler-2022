package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;

public class BlockItemNode extends ParserNode {
    private DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode) {
        this.declNode = declNode;
    }

    public BlockItemNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
    }

    public boolean isDecl() {
        return declNode != null;
    }

    public boolean isStmt() {
        return stmtNode != null;
    }

    public boolean intFuncHasReturnInTheLastStmt() {
        if (isDecl()) {
            return false;
        }
        return stmtNode.intFuncHasReturnInTheLastStmt();
    }

    @Override
    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        if (isDecl()) {
            declNode.generateIntermediate(intermediateVisitor);
        } else {
            stmtNode.generateIntermediate(intermediateVisitor);
        }
    }
}
