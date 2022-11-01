package ErrorTool;

import MySymbolTable.SymbolType;

import java.util.ArrayList;

//函数实参变量类型
public class FuncRealParamsType {
    private SymbolType funcType;
    private int dimension;
    private ArrayList<Integer> dimensionLength;

    public FuncRealParamsType(SymbolType funcType, int dimension) {
        this.funcType = funcType;
        this.dimension = dimension;
    }

    public FuncRealParamsType(SymbolType funcType, int dimension, ArrayList<Integer> dimensionLength) {
        this(funcType, dimension);
        this.dimensionLength = dimensionLength;
    }

    public SymbolType getFuncType() {
        return funcType;
    }

    public int getDimension() {
        return dimension;
    }

    public int getTwoDimensionLength() {
        return dimensionLength.get(1);
    }
}
