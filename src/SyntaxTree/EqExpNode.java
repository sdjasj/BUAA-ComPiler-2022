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

//EqExp → RelExp | EqExp ('==' | '!=') RelExp
public class EqExpNode extends ParserNode {
    private RelExpNode relExpNode;
    private TokenType op;
    private EqExpNode eqExpNode;

    public EqExpNode(RelExpNode relExpNode) {
        this.relExpNode = relExpNode;
    }

    public EqExpNode(EqExpNode eqExpNode, TokenType op, RelExpNode relExpNode) {
        this.eqExpNode = eqExpNode;
        this.op = op;
        this.relExpNode = relExpNode;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor, String trueLabel,
                                     String falseLabel) {
        if (eqExpNode != null) {
            Operand src1 = eqExpNode.getEqExpResult(intermediateVisitor);
            Operand src2 = relExpNode.getRelExpResult(intermediateVisitor);
            Operator ICOP = null;
            Operand target;
            if (falseLabel == null) {
                if (op == TokenType.EQL) {
                    //==
                    ICOP = Operator.BEQ;
                } else if (op == TokenType.NEQ) {
                    ICOP = Operator.BNE;
                }
                target = Operand.getNewOperand(trueLabel, Operand.OperandType.ADDRESS);
            } else {
                if (op == TokenType.EQL) {
                    //==
                    ICOP = Operator.BNE;
                } else if (op == TokenType.NEQ) {
                    ICOP = Operator.BEQ;
                }
                target = Operand.getNewOperand(falseLabel, Operand.OperandType.ADDRESS);
            }
            intermediateVisitor.addIntermediateCode(new BranchCode(target, src1, src2, ICOP));
        } else {
            relExpNode.generateIntermediate(intermediateVisitor, trueLabel, falseLabel);
        }
    }

    public Operand getEqExpResult(IntermediateVisitor intermediateVisitor) {
        if (eqExpNode != null) {
            Operand src1 = eqExpNode.getEqExpResult(intermediateVisitor);
            Operand src2 = relExpNode.getRelExpResult(intermediateVisitor);

            if (Optimizer.ConstOptimizer && src1.isNUMBER() && src2.isNUMBER()) {
                int src1Val = Integer.parseInt(src1.getName());
                int src2Val = Integer.parseInt(src2.getName());
                if (op == TokenType.EQL) {
                    if (src1Val == src2Val) {
                        return Operand.getNewOperand("1", Operand.OperandType.NUMBER);
                    } else {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                } else if (op == TokenType.NEQ) {
                    if (src1Val != src2Val) {
                        return Operand.getNewOperand("1", Operand.OperandType.NUMBER);
                    } else {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                }
            }

            Operand target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            Operator ICOP = null;
            if (op == TokenType.EQL) {
                //==
                ICOP = Operator.SEQ;
            } else if (op == TokenType.NEQ) {
                ICOP = Operator.SNE;
            }
            intermediateVisitor.addIntermediateCode(new CompareCode(target, src1, src2, ICOP));
            return target;
        } else {
            return relExpNode.getRelExpResult(intermediateVisitor);
        }
    }
}
