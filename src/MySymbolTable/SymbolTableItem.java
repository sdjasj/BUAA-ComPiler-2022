package MySymbolTable;

import Lexer.Token;
import Lexer.TokenType;
import SyntaxTree.FuncTypeNode;
import SyntaxTree.InitValNode;

import java.io.Serializable;
import java.util.ArrayList;

public class SymbolTableItem implements Serializable {
    private String name;
    private SymbolType type;
    private int dimension; //数组维数
    private ArrayList<Integer> constDimensionLength; //定义数组维数对应的长度
    private ArrayList<SymbolTableItem> funcFParams;
    private TokenType funcType;
    private ArrayList<Integer> constArrayInitVal;
    private int constVarInitVal;
    private InitValNode initVal;
    private Token ident;
    private int line;
    private int blockDepth;
    private boolean isConst;

    //非数组变量定义无初值
    public SymbolTableItem(Token ident, SymbolType type) {
        this.ident = ident;
        this.name = ident.getValue();
        this.type = type;
        this.line = ident.getLine();
    }

    //非数组常量定义有初值 || 全局变量
    public SymbolTableItem(Token ident, SymbolType type, int initVal, boolean isConst) {
        this.ident = ident;
        this.name = ident.getValue();
        this.type = type;
        this.line = ident.getLine();
        this.constVarInitVal = initVal;
        this.isConst = isConst;
    }

    //非数组变量定义有初值
    public SymbolTableItem(Token ident, SymbolType type, InitValNode initValNode) {
        this.ident = ident;
        this.name = ident.getValue();
        this.type = type;
        this.line = ident.getLine();
        this.initVal = initValNode;
    }

    //数组变量定义 varDef
    public SymbolTableItem(Token ident, SymbolType type, int dimension,
                           ArrayList<Integer> constDimensionLength, InitValNode initValNode) {

        this.ident = ident;
        this.name = ident.getValue();
        this.type = type;
        this.line = ident.getLine();
        this.dimension = dimension;
        this.constDimensionLength = constDimensionLength;
        this.initVal = initValNode;
    }

    //数组常量定义 ConstDef
    public SymbolTableItem(Token ident, SymbolType type, int dimension,
                           ArrayList<Integer> constDimensionLength,
                           ArrayList<Integer> constArrayInitVal) {
        this.ident = ident;
        this.name = ident.getValue();
        this.type = type;
        this.line = ident.getLine();
        this.dimension = dimension;
        this.constDimensionLength = constDimensionLength;
        this.constArrayInitVal = constArrayInitVal;
        this.isConst = true;
    }

    //函数定义
    public SymbolTableItem(Token ident, SymbolType type, FuncTypeNode funcTypeNode,
                           ArrayList<SymbolTableItem> funcFParams) {
        this.ident = ident;
        this.name = ident.getValue();
        this.type = type;
        this.line = ident.getLine();
        this.funcFParams = funcFParams;
        this.funcType = funcTypeNode.getFuncType();
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return line;
    }

    public SymbolType getType() {
        return type;
    }

    public int getConstVarInitVal() {
        return constVarInitVal;
    }

    public ArrayList<Integer> getConstArrayInitVal() {
        return constArrayInitVal;
    }

    public ArrayList<Integer> getConstDimensionLength() {
        return constDimensionLength;
    }

    public int getFuncParamsLength() {
        return funcFParams == null ? 0 : funcFParams.size();
    }

    public int getDimension() {
        return dimension;
    }

    public ArrayList<SymbolTableItem> getFuncFParams() {
        return funcFParams;
    }

    public int getTwoDimensionLength() {
        return constDimensionLength.get(1);
    }

    //得到数组第n + 1维到最后的长度, n从1开始
    //constDimensionLength -> 如果第一维缺省则constDimensionLength[0] = 0
    public int getDimensionLength(int n) {
        if (n >= constDimensionLength.size()) {
            return 0;
        }
        int ans = 1;
        for (int i = n; i < constDimensionLength.size(); i++) {
            ans *= constDimensionLength.get(i);
        }
        return ans;
    }

    public int getBlockDepth() {
        return blockDepth;
    }

    public void setBlockDepth(int blockDepth) {
        this.blockDepth = blockDepth;
    }

    public TokenType getFuncType() {
        return funcType;
    }

    public boolean isConst() {
        return isConst;
    }
}
