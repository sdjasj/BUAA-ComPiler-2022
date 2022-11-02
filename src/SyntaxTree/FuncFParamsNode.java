package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;

import java.io.IOException;
import java.util.ArrayList;

public class FuncFParamsNode extends ParserNode {
    private ArrayList<FuncFParamNode> funcFParams;

    public FuncFParamsNode() {
        funcFParams = new ArrayList<>();
    }

    public void addParams(FuncFParamNode funcFParamNode) {
        if (funcFParamNode != null) {
            funcFParams.add(funcFParamNode);
        }
    }

    public int getParamsNum() {
        return funcFParams.size();
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        for (FuncFParamNode funcFParam : funcFParams) {
            funcFParam.generateIntermediate(intermediateVisitor);
        }
    }
}
