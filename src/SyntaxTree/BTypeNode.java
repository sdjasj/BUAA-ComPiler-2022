package SyntaxTree;

import Lexer.TokenType;
import MySymbolTable.SymbolTable;

public class BTypeNode extends ParserNode {
    private TokenType tokenType;

    public BTypeNode(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public boolean isInt() {
        return tokenType == TokenType.INTTK;
    }
}
