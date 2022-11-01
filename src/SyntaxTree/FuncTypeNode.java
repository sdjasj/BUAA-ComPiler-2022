package SyntaxTree;
import Lexer.TokenType;
import MySymbolTable.SymbolTable;

public class FuncTypeNode extends ParserNode {
    private TokenType funcType;

    public FuncTypeNode(TokenType funcType) {
        this.funcType = funcType;
    }

    public TokenType getFuncType() {
        return funcType;
    }
}
