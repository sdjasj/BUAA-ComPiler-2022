package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;

import java.io.IOException;
import java.util.ArrayList;

public class CompUnitNode extends ParserNode {
    private ArrayList<DeclNode> declNodes;
    private ArrayList<FuncDefNode> funcDefNodes;
    private MainFuncDefNode mainFuncDefNode;

    public CompUnitNode(ArrayList<DeclNode> declNodes, ArrayList<FuncDefNode> funcDefNodes,
                        MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }

    @Override
    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        for (DeclNode declNode : declNodes) {
            declNode.generateIntermediate(intermediateVisitor);
        }
        mainFuncDefNode.generateIntermediate(intermediateVisitor);
        for (FuncDefNode funcDefNode : funcDefNodes) {
            funcDefNode.generateIntermediate(intermediateVisitor);
        }
    }
}
