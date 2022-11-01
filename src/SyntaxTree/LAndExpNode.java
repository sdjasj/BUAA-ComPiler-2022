package SyntaxTree;

import Lexer.TokenType;
import MySymbolTable.SymbolTable;

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
}
