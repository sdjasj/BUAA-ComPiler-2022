package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;
import SyntaxTree.ParserNode;

import java.io.IOException;
import java.util.ArrayList;

//ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'

public class ConstDeclNode extends ParserNode {
    private BTypeNode bTypeNode;
    private ArrayList<ConstDefNode> constDefNodes;

    public ConstDeclNode(BTypeNode bTypeNode, ArrayList<ConstDefNode> constDefNodes) {
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        for (ConstDefNode constDefNode : constDefNodes) {
            constDefNode.setbTypeNode(bTypeNode);
            constDefNode.generateIntermediate(intermediateVisitor);
        }
    }
}
