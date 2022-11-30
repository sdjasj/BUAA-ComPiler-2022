package SyntaxTree;

import ErrorTool.FuncRealParamsType;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import MySymbolTable.SymbolTable;

import java.util.ArrayList;

//FuncRParams → Exp { ',' Exp }
public class FuncRParamsNode extends ParserNode {
    private ArrayList<ExpNode> expNodes;

    public FuncRParamsNode() {
        expNodes = new ArrayList<>();
    }

    public void addExpNode(ExpNode expNode) {
        if (expNode != null) {
            expNodes.add(expNode);
        }
    }

    public int getRealParamsNum() {
        return expNodes == null ? 0 : expNodes.size();
    }

    public ArrayList<FuncRealParamsType> getFuncRealParamsType(SymbolTable symbolTable) {
        ArrayList<FuncRealParamsType> funcRealParamsTypes = new ArrayList<>();
        for (int i = 0; i < expNodes.size(); i++) {
            funcRealParamsTypes.add(expNodes.get(i).getFuncRealParamsType(symbolTable));
        }
        return funcRealParamsTypes;
    }

    public ArrayList<Operand> generateMidCodeAndReturnTempVar(
        IntermediateVisitor intermediateVisitor) {
        ArrayList<Operand> varTs = new ArrayList<>();
        for (ExpNode expNode : expNodes) {
            varTs.add(expNode.generateMidCodeAndReturnTempVar(intermediateVisitor));
        }
        return varTs;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }
}
