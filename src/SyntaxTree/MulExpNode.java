package SyntaxTree;

import ErrorTool.FuncRealParamsType;
import IntermediateCode.AllCode.CalculateCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.TokenType;
import MySymbolTable.SymbolTable;
import MySymbolTable.SymbolType;
import Tool.Optimizer;

//MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
public class MulExpNode extends ParserNode {
    private UnaryExpNode unaryExpNode;
    private TokenType op;
    private MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode) {
        this.unaryExpNode = unaryExpNode;
        this.op = null;
        this.mulExpNode = null;
    }

    public MulExpNode(MulExpNode mulExpNode, TokenType op, UnaryExpNode unaryExpNode) {
        this.mulExpNode = mulExpNode;
        this.op = op;
        this.unaryExpNode = unaryExpNode;
    }

    public int getConstVal(SymbolTable symbolTable) {
        if (mulExpNode == null) {
            return unaryExpNode.getConstVal(symbolTable);
        } else {
            if (op == TokenType.MULT) {
                return mulExpNode.getConstVal(symbolTable) * unaryExpNode.getConstVal(symbolTable);
            } else if (op == TokenType.DIV) {
                return mulExpNode.getConstVal(symbolTable) / unaryExpNode.getConstVal(symbolTable);
            } else {
                return mulExpNode.getConstVal(symbolTable) % unaryExpNode.getConstVal(symbolTable);
            }
        }
    }

    public FuncRealParamsType getFuncRealParamsType(SymbolTable symbolTable) {
        if (op != null) {
            return new FuncRealParamsType(SymbolType.VAR, 0);
        }
        return unaryExpNode.getFuncRealParamsType(symbolTable);
    }

    public Operand generateMidCodeAndReturnTempVar(IntermediateVisitor intermediateVisitor) {
        if (mulExpNode == null) {
            return unaryExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
        } else {
            Operand src1 = mulExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operand src2 = unaryExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operand target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            Operator operator = null;
            if (op == TokenType.MULT) {
                operator = Operator.MUL;
            } else if (op == TokenType.DIV) {
                operator = Operator.DIV;
            } else if (op == TokenType.MOD) {
                operator = Operator.MOD;
            }

            if (Optimizer.ConstOptimizer && src1.isNUMBER() && src2.isNUMBER()) {
                if (operator == Operator.MUL) {
                    return Operand.getNewOperand(
                        String.valueOf(Integer.parseInt(src1.getName()) *
                            Integer.parseInt(src2.getName())),
                        Operand.OperandType.NUMBER);
                } else if (operator == Operator.DIV) {
                    return Operand.getNewOperand(
                        String.valueOf(Integer.parseInt(src1.getName()) /
                            Integer.parseInt(src2.getName())),
                        Operand.OperandType.NUMBER);
                } else if (operator == Operator.MOD) {
                    return Operand.getNewOperand(
                        String.valueOf(Integer.parseInt(src1.getName()) %
                            Integer.parseInt(src2.getName())),
                        Operand.OperandType.NUMBER);
                }
            }

            CalculateCode calculateCode = new CalculateCode(target, src1, src2, operator);
            intermediateVisitor.addIntermediateCode(calculateCode);
            return target;
        }
    }
}
