package SyntaxTree;

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

}
