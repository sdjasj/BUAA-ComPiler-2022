package SyntaxTree;

import MySymbolTable.SymbolTable;
import SyntaxTree.ParserNode;

import java.io.IOException;
import java.util.ArrayList;

public class ConstInitValNode extends ParserNode {
    private ConstExpNode constExpNode;
    private ArrayList<ConstInitValNode> constInitValNodes;
    private boolean arrayInitVal;

    public ConstInitValNode() {
        constInitValNodes = new ArrayList<>();
        arrayInitVal = true;
    }

    public ConstInitValNode(ConstExpNode constExpNode) {
        this.constExpNode = constExpNode;
        arrayInitVal = false;
    }

    public void addConstInitValNode(ConstInitValNode constInitValNode) {
        if (constInitValNode != null) {
            constInitValNodes.add(constInitValNode);
        }
    }

    public boolean isArrayInitVal() {
        return arrayInitVal;
    }

    public int getConstVal(SymbolTable symbolTable) {
        return constExpNode.getConstVal(symbolTable);
    }

    public ArrayList<Integer> getConstArrayVal(SymbolTable symbolTable) {
        ArrayList<Integer> constArrayVals = new ArrayList<>();
        for (ConstInitValNode constInitValNode : constInitValNodes) {
            if (constInitValNode.isArrayInitVal()) {
                constArrayVals.addAll(constInitValNode.getConstArrayVal(symbolTable));
            } else {
                constArrayVals.add(constInitValNode.getConstVal(symbolTable));
            }
        }
        return constArrayVals;
    }
}
