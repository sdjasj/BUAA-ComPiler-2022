package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;

import java.io.IOException;
import java.util.ArrayList;

public class VarDeclNode extends ParserNode {
    private BTypeNode bTypeNode;
    private ArrayList<VarDefNode> varDefNodes;

    public VarDeclNode(BTypeNode bTypeNode) {
        this.bTypeNode = bTypeNode;
        varDefNodes = new ArrayList<>();
    }

    public void addVarDefNode(VarDefNode varDefNode) {
        if (varDefNode != null) {
            varDefNodes.add(varDefNode);
        }
    }

    @Override
    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        for (VarDefNode varDefNode : varDefNodes) {
            varDefNode.setbTypeNode(bTypeNode);
            varDefNode.generateIntermediate(intermediateVisitor);
        }
    }
}
