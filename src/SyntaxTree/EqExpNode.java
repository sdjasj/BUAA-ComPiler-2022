package SyntaxTree;

import IntermediateCode.AllCode.BranchCode;
import IntermediateCode.AllCode.CompareCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.TokenType;
import MySymbolTable.SymbolTable;

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
                target = new Operand(trueLabel, Operand.OperandType.ADDRESS);
            } else {
                if (op == TokenType.EQL) {
                    //==
                    ICOP = Operator.BNE;
                } else if (op == TokenType.NEQ) {
                    ICOP = Operator.BEQ;
                }
                target = new Operand(falseLabel, Operand.OperandType.ADDRESS);
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
            Operand target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
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
