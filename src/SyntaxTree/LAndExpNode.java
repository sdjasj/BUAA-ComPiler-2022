package SyntaxTree;

import IntermediateCode.AllCode.LabelCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.TCode;
import Lexer.TokenType;

//LAndExp → EqExp | LAndExp '&&' EqExp
public class LAndExpNode extends ParserNode {
    private EqExpNode eqExpNode;
    private TokenType op;
    private LAndExpNode lAndExpNode;

    public LAndExpNode(EqExpNode eqExpNode) {
        this.eqExpNode = eqExpNode;
    }

    public LAndExpNode(LAndExpNode lAndExpNode, TokenType op, EqExpNode eqExpNode) {
        this.lAndExpNode = lAndExpNode;
        this.op = op;
        this.eqExpNode = eqExpNode;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor, String trueLabel,
                                     String falseLabel) {
        if (lAndExpNode != null) {
            String label;
            if (falseLabel != null) {
                label = falseLabel;
            } else {
                label = TCode.genNewLable();
            }
            lAndExpNode.generateIntermediate(intermediateVisitor, null, label);
            eqExpNode.generateIntermediate(intermediateVisitor, trueLabel, falseLabel);
            if (falseLabel == null) {
                intermediateVisitor.addIntermediateCode(new LabelCode(label));
            }
        } else {
            eqExpNode.generateIntermediate(intermediateVisitor, trueLabel, falseLabel);
        }
    }
}
