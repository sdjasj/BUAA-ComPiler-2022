package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;
import SyntaxTree.ParserNode;

import java.io.IOException;

//Decl → ConstDecl | VarDecl
public class DeclNode extends ParserNode {
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode) {
        super();
        this.constDeclNode = constDeclNode;
    }

    public DeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
    }

    public boolean isConst() {
        return constDeclNode != null;
    }

    @Override
    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        if (isConst()) {
            constDeclNode.generateIntermediate(intermediateVisitor);
        } else {
            varDeclNode.generateIntermediate(intermediateVisitor);
        }
    }
}
