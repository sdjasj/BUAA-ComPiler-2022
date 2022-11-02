package SyntaxTree;

import IntermediateCode.AllCode.LabelCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.TCode;
import Lexer.TokenType;

public class LOrExpNode extends ParserNode {
    private LAndExpNode lAndExpNode;
    private LOrExpNode lOrExpNode;
    private TokenType op;

    public LOrExpNode(LAndExpNode lAndExpNode) {
        this.lAndExpNode = lAndExpNode;
    }

    public LOrExpNode(LOrExpNode lOrExpNode, TokenType op, LAndExpNode lAndExpNode) {
        this.lOrExpNode = lOrExpNode;
        this.op = op;
        this.lAndExpNode = lAndExpNode;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor, String trueLabel,
                                     String falseLabel) {
        if (lOrExpNode != null) {
            String label;
            if (trueLabel != null) {
                label = trueLabel;
            } else {
                label = TCode.genNewLable();
            }
            lOrExpNode.generateIntermediate(intermediateVisitor, label, null);
            lAndExpNode.generateIntermediate(intermediateVisitor, trueLabel, falseLabel);
            if (trueLabel == null) {
                intermediateVisitor.addIntermediateCode(new LabelCode(label));
            }
        } else {
            lAndExpNode.generateIntermediate(intermediateVisitor, trueLabel, falseLabel);
        }
    }
}
