package SyntaxTree;

import Lexer.Token;
import MySymbolTable.SymbolTable;

import java.io.Serializable;

public class NumberNode extends ParserNode implements Serializable {
    private Token IntConst;


    public NumberNode(Token intConst) {
        IntConst = intConst;
    }

    public int getConstVal() {
        return Integer.parseInt(IntConst.getValue());
    }
}
