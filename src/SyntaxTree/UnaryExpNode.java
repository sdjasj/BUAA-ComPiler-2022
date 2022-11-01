package SyntaxTree;

import ErrorTool.FuncRealParamsType;
import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.SingleCalculateCode;
import IntermediateCode.FunctionCode.FunctionCallCode;
import IntermediateCode.FunctionCode.FunctionPushCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.Token;
import Lexer.TokenType;
import MySymbolTable.SymbolTable;
import MySymbolTable.SymbolTableItem;
import MySymbolTable.SymbolType;

import java.io.Serializable;
import java.util.ArrayList;

//UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,
//函数调用也需要覆盖FuncRParams的不同情况
//| UnaryOp UnaryExp // 存在即可
public class UnaryExpNode extends ParserNode implements Serializable {
    private PrimaryExpNode primaryExpNode;
    private boolean isFuncCall;
    private Token ident;
    private FuncRParamsNode funcRParamsNode;
    private TokenType unaryOp;
    private UnaryExpNode unaryExpNode;

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
        isFuncCall = false;
        ident = null;
        funcRParamsNode = null;
        unaryOp = null;
        unaryExpNode = null;
    }

    public UnaryExpNode(Token ident, FuncRParamsNode funcRParamsNode) {
        isFuncCall = true;
        this.ident = ident;
        this.funcRParamsNode = funcRParamsNode;
        primaryExpNode = null;
        unaryOp = null;
        unaryExpNode = null;
    }

    public UnaryExpNode(TokenType unaryOp, UnaryExpNode unaryExpNode) {
        isFuncCall = false;
        primaryExpNode = null;
        ident = null;
        funcRParamsNode = null;
        this.unaryOp = unaryOp;
        this.unaryExpNode = unaryExpNode;
    }

    public boolean isFuncCall() {
        return isFuncCall;
    }

    public boolean hasUnaryOp() {
        return unaryOp != null;
    }

    public int getConstVal(SymbolTable symbolTable) {
        if (hasUnaryOp()) {
            if (unaryOp == TokenType.PLUS) {
                return unaryExpNode.getConstVal(symbolTable);
            } else if (unaryOp == TokenType.MINU) {
                return -unaryExpNode.getConstVal(symbolTable);
            }
            return Integer.MIN_VALUE;
        } else {
            return primaryExpNode.getConstVal(symbolTable);
        }
    }

    public FuncRealParamsType getFuncRealParamsType(SymbolTable symbolTable) {
        if (isFuncCall) {
            SymbolTableItem definedFunc =
                SymbolTable.findItemFromAllTable(ident, symbolTable);

            return new FuncRealParamsType(
                definedFunc.getType() == SymbolType.FUNC_INT ? SymbolType.VAR : SymbolType.NONE, 0);
        }
        if (hasUnaryOp()) {
            return unaryExpNode.getFuncRealParamsType(symbolTable);
        }
        return primaryExpNode.getFuncRealParamsType(symbolTable);
    }

    public Operand generateMidCodeAndReturnTempVar(IntermediateVisitor intermediateVisitor) {
        if (isFuncCall()) {
            if (funcRParamsNode != null) {
                ArrayList<Operand> varTs =
                    funcRParamsNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
//                System.err.println(varTs);
                for (int i = 0; i < varTs.size(); i++) {
                    FunctionPushCode functionPushCode = new FunctionPushCode(varTs.get(i), i);
                    intermediateVisitor.addIntermediateCode(functionPushCode);
                }
            }
            Operand target = new Operand(ident.getValue(), Operand.OperandType.ADDRESS);
            FunctionCallCode functionCallCode =
                new FunctionCallCode(target);
            intermediateVisitor.addIntermediateCode(functionCallCode);
            SymbolTableItem item =
                SymbolTable.findItemFromAllTable(ident, RecordSymbolTable);

            if (item == null) {
                System.out.println("error in find item in unaryexpNode");
            }
            if (item.getFuncType() == TokenType.INTTK) {
                Operand temp = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                intermediateVisitor.addIntermediateCode(
                    new AssignCode(temp, new Operand("RET", Operand.OperandType.VAR)));
                return temp;
            } else {
                return null;
            }
        } else if (hasUnaryOp()) {
            Operand src1 = unaryExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operand target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
            Operator operator;
            if (unaryOp == TokenType.PLUS) {
                operator = Operator.PLUS;
            } else if (unaryOp == TokenType.MINU) {
                operator = Operator.NEG;
            } else {
                operator = Operator.NOT;
            }
            SingleCalculateCode singleCalculateCode =
                new SingleCalculateCode(target, src1, operator);
            intermediateVisitor.addIntermediateCode(singleCalculateCode);
            return target;
        } else {
            return primaryExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
        }
    }
}