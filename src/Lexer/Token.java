package Lexer;

import java.io.Serializable;

public class Token implements Serializable {
    private TokenType tokenType;
    private String value;
    private int line;

    public Token(TokenType tokenType, String value, int line) {
        this.tokenType = tokenType;
        this.value = value;
        this.line = line;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLine() {
        return line;
    }
}
