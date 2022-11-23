package SyntaxTree;

import IntermediateCode.AllCode.BranchCode;
import IntermediateCode.AllCode.CompareCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.TokenType;
import MySymbolTable.SymbolTable;
import Tool.Optimizer;

////RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
public class RelExpNode extends ParserNode {
    private AddExpNode addExpNode;
    private TokenType op;
    private RelExpNode relExpNode;

    public RelExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public RelExpNode(RelExpNode relExpNode, TokenType op, AddExpNode addExpNode) {
        this.relExpNode = relExpNode;
        this.op = op;
        this.addExpNode = addExpNode;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor, String trueLabel,
                                     String falseLabel) {
        if (relExpNode != null) {
            Operand src1 = relExpNode.getRelExpResult(intermediateVisitor);
            Operand src2 = addExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operand target;
            Operator ICop = null;
            if (falseLabel == null) {
                if (op == TokenType.LSS) {
                    ICop = Operator.BLT;
                } else if (op == TokenType.GRE) {
                    ICop = Operator.BGT;
                } else if (op == TokenType.LEQ) {
                    ICop = Operator.BLE;
                } else if (op == TokenType.GEQ) {
                    ICop = Operator.BGE;
                }
                target = Operand.getNewOperand(trueLabel, Operand.OperandType.ADDRESS);
            } else {
                if (op == TokenType.LSS) {
                    ICop = Operator.BGE;
                } else if (op == TokenType.GRE) {
                    ICop = Operator.BLE;
                } else if (op == TokenType.LEQ) {
                    ICop = Operator.BGT;
                } else if (op == TokenType.GEQ) {
                    ICop = Operator.BLT;
                }
                target = Operand.getNewOperand(falseLabel, Operand.OperandType.ADDRESS);
            }
            intermediateVisitor.addIntermediateCode(new BranchCode(target, src1, src2, ICop));
        } else {
            Operand src1 = addExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            if (falseLabel != null) {
                intermediateVisitor.addIntermediateCode(
                    new BranchCode(Operand.getNewOperand(falseLabel, Operand.OperandType.ADDRESS), src1, null,
                        Operator.BEQZ));
            } else {
                intermediateVisitor.addIntermediateCode(
                    new BranchCode(Operand.getNewOperand(trueLabel, Operand.OperandType.ADDRESS), src1, null,
                        Operator.BNEZ));
            }
        }

    }

    public Operand getRelExpResult(IntermediateVisitor intermediateVisitor) {
        if (relExpNode != null) {
            Operand src1 = relExpNode.getRelExpResult(intermediateVisitor);
            Operand src2 = addExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);

            if (Optimizer.ConstOptimizer && src1.isNUMBER() && src2.isNUMBER()) {
                int src1Val = Integer.parseInt(src1.getName());
                int src2Val = Integer.parseInt(src2.getName());
                if (op == TokenType.LSS) {
                    if (src1Val < src2Val) {
                        return Operand.getNewOperand("1", Operand.OperandType.NUMBER);
                    } else {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                } else if (op == TokenType.LEQ) {
                    if (src1Val <= src2Val) {
                        return Operand.getNewOperand("1", Operand.OperandType.NUMBER);
                    } else {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                } else if (op == TokenType.GRE) {
                    if (src1Val > src2Val) {
                        return Operand.getNewOperand("1", Operand.OperandType.NUMBER);
                    } else {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                } else if (op == TokenType.GEQ) {
                    if (src1Val >= src2Val) {
                        return Operand.getNewOperand("1", Operand.OperandType.NUMBER);
                    } else {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                }
            }

            Operand target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            Operator ICop = null;
            if (op == TokenType.LSS) {
                ICop = Operator.LT;
            } else if (op == TokenType.LEQ) {
                ICop = Operator.LTE;
            } else if (op == TokenType.GRE) {
                ICop = Operator.GT;
            } else if (op == TokenType.GEQ) {
                ICop = Operator.GTE;
            }
            intermediateVisitor.addIntermediateCode(new CompareCode(target, src1, src2, ICop));
            return target;
        } else {
            return addExpNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
        }
    }
}
