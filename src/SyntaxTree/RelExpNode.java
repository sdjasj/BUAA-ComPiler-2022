package SyntaxTree;

import Lexer.TokenType;
import MySymbolTable.SymbolTable;

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
}
