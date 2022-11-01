package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import MySymbolTable.SymbolTable;


import java.util.ArrayList;

//InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
public class InitValNode extends ParserNode {
    private ExpNode expNode;
    private ArrayList<InitValNode> initValNodes;
    boolean arrayInitVal;

    public InitValNode() {
        initValNodes = new ArrayList<>();
        arrayInitVal = true;
    }

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public void addInitValNode(InitValNode initValNode) {
        if (initValNode != null) {
            initValNodes.add(initValNode);
        }
    }

    public boolean isArrayInitVal() {
        return arrayInitVal;
    }

    public int getConstVal(SymbolTable symbolTable) {
        return expNode.getConstVal(symbolTable);
    }

    public ArrayList<Integer> getConstArrayVal(SymbolTable symbolTable) {
        ArrayList<Integer> constArrayVals = new ArrayList<>();
        for (InitValNode constInitValNode : initValNodes) {
            if (constInitValNode.isArrayInitVal()) {
                constArrayVals.addAll(constInitValNode.getConstArrayVal(symbolTable));
            } else {
                constArrayVals.add(constInitValNode.getConstVal(symbolTable));
            }
        }
        return constArrayVals;
    }

    public Operand generateMidCodeAndReturnTempVarAsVar(IntermediateVisitor intermediateVisitor) {
        return expNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
    }

    public ArrayList<Operand> generateMidCodeAndReturnTempVarAsArray(
        IntermediateVisitor intermediateVisitor) {
        ArrayList<Operand> operands = new ArrayList<>();
        for (InitValNode initValNode : initValNodes) {
            if (initValNode.isArrayInitVal()) {
                operands.addAll(
                    initValNode.generateMidCodeAndReturnTempVarAsArray(intermediateVisitor));
            } else {
                operands.add(initValNode.generateMidCodeAndReturnTempVarAsVar(intermediateVisitor));
            }
        }
        return operands;
    }
}
