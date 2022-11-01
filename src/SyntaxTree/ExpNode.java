package SyntaxTree;

import ErrorTool.FuncRealParamsType;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import MySymbolTable.SymbolTable;

import java.io.IOException;
import java.io.Serializable;

public class ExpNode extends ParserNode implements Serializable {
    private AddExpNode addExpNode;

    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public int getConstVal(SymbolTable symbolTable) {
        return addExpNode.getConstVal(symbolTable);
    }

    public FuncRealParamsType getFuncRealParamsType(SymbolTable symbolTable) {
        return addExpNode.getFuncRealParamsType(symbolTable);
    }


    public Operand generateMidCodeAndReturnTempVar(IntermediateVisitor intermediateVisitor) {
        return addExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
    }
}
