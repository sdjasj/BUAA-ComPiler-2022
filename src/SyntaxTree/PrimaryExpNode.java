package SyntaxTree;

import ErrorTool.FuncRealParamsType;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import MySymbolTable.SymbolTable;
import MySymbolTable.SymbolType;

import java.io.IOException;
import java.io.Serializable;

//PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖

public class PrimaryExpNode extends ParserNode implements Serializable {
    private ExpNode expNode;
    private LValNode lValNode;
    private NumberNode numberNode;

    public PrimaryExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }

    public boolean isExp() {
        return expNode != null;
    }

    public boolean isLval() {
        return lValNode != null;
    }

    public boolean isNumber() {
        return numberNode != null;
    }

    public int getConstVal(SymbolTable symbolTable) {
        if (isNumber()) {
            return numberNode.getConstVal();
        } else if (isExp()) {
            return expNode.getConstVal(symbolTable);
        } else {
            return lValNode.getConstVal(symbolTable);
        }
    }

    public FuncRealParamsType getFuncRealParamsType(SymbolTable symbolTable) {
        if (!isLval()) {
            return new FuncRealParamsType(SymbolType.VAR, 0);
        }
        return lValNode.getFuncRealParamsType(symbolTable);
    }

    public Operand generateMidCodeAndReturnTempVar(IntermediateVisitor intermediateVisitor) {
        if (isExp()) {
            return expNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
        } else if (isLval()) {
            Operand operand = lValNode.generateMidCodeAndUseAsRight(intermediateVisitor);
//            if (operand == null) {
//                System.err.println(lValNode.getIdent().getValue());
//            }
            return operand;
        } else {
            String number = String.valueOf(numberNode.getConstVal());
            return Operand.getNewOperand(number, Operand.OperandType.NUMBER);
        }
    }
}
