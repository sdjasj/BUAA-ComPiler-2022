package SyntaxTree;

import Lexer.TokenType;
import MySymbolTable.SymbolTable;

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


}
