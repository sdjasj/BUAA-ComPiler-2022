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

//AddExp → MulExp | AddExp ('+' | '−') MulExp
public class AddExpNode extends ParserNode {
    private AddExpNode addExpNode;
    private TokenType op;
    private MulExpNode mulExpNode;

    public AddExpNode(MulExpNode mulExpNode) {
        addExpNode = null;
        op = null;
        this.mulExpNode = mulExpNode;
    }

    public AddExpNode(AddExpNode addExpNode, TokenType op, MulExpNode mulExpNode) {
        this.addExpNode = addExpNode;
        this.op = op;
        this.mulExpNode = mulExpNode;
    }

    public int getConstVal(SymbolTable symbolTable) {
        if (addExpNode == null) {
            return mulExpNode.getConstVal(symbolTable);
        } else {
            if (op == TokenType.PLUS) {
                return addExpNode.getConstVal(symbolTable) + mulExpNode.getConstVal(symbolTable);
            } else {
                return addExpNode.getConstVal(symbolTable) - mulExpNode.getConstVal(symbolTable);
            }
        }
    }

    public FuncRealParamsType getFuncRealParamsType(SymbolTable symbolTable) {
        if (op != null) {
            return new FuncRealParamsType(SymbolType.VAR, 0);
        }
        return mulExpNode.getFuncRealParamsType(symbolTable);
    }

    public Operand generateMidCodeAndReturnTempVar(IntermediateVisitor intermediateVisitor) {
        if (addExpNode == null) {
            return mulExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
        } else {
            Operand src1 = addExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operand src2 = mulExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operator operator = null;
            if (op == TokenType.PLUS) {
                operator = Operator.ADD;
            } else if (op == TokenType.MINU) {
                operator = Operator.SUB;
            }

            if (Optimizer.ConstOptimizer) {
                if (src1.isNUMBER() && src2.isNUMBER()) {
                    if (operator == Operator.ADD) {
                        return Operand.getNewOperand(
                            String.valueOf(Integer.parseInt(src1.getName()) +
                                Integer.parseInt(src2.getName())),
                            Operand.OperandType.NUMBER);
                    } else if (operator == Operator.SUB) {
                        return Operand.getNewOperand(
                            String.valueOf(Integer.parseInt(src1.getName()) -
                                Integer.parseInt(src2.getName())),
                            Operand.OperandType.NUMBER);
                    }
                } else {
                    if (operator == Operator.ADD) {
                        if (src1.isNUMBER() && src1.getName().equals("0")) {
                            return src2;
                        } else if (src2.isNUMBER() && src2.getName().equals("0")) {
                            return src1;
                        }
                    } else if (operator == Operator.SUB) {
                        if (src2.isNUMBER() && src2.getName().equals("0")) {
                            return src1;
                        }
                    }
                }
            }

            if (operator == Operator.SUB && src1.getName().equals(src2.getName())) {
                return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
            }

            Operand target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            CalculateCode calculateCode = new CalculateCode(target, src1, src2, operator);
            intermediateVisitor.addIntermediateCode(calculateCode);
            return target;
        }
    }
}
