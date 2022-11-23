package IntermediateCode;

import MipsCode.GlobalArrayInit;
import MySymbolTable.SymbolTable;

import java.util.ArrayList;

public class GlobalArrayDecl extends GlobalDecl {
    private ArrayList<Integer> dimensions;
    private ArrayList<Integer> initVal;

    public GlobalArrayDecl(StoreType storeType, String name, boolean isConst,
                           ArrayList<Integer> dimensions,
                           ArrayList<Integer> initVal) {
        super(storeType, name, isConst);
        this.dimensions = dimensions;
        this.initVal = initVal;
    }

    @Override
    public void output() {
        String constDef = "";
        if (isConst) {
            constDef = "const";
        }
        StringBuilder dimensionShow = new StringBuilder();
        for (Integer dimension : dimensions) {
            dimensionShow.append("[").append(dimension).append("]");
        }
        if (initVal == null) {
            String intermediateCode =
                constDef + " " + storeType + " " + name + " " + dimensionShow.toString() + " = " + "0:" + dimensions;
            System.out.println(intermediateCode);
        } else {
            String intermediateCode =
                constDef + " " + storeType + " " + name + " " + dimensionShow.toString() + " = " +
                    initVal;
            System.out.println(intermediateCode);
        }
    }

    public GlobalArrayInit toMips() {
        int size = 1;
        for (Integer dimension : dimensions) {
            size *= dimension;
        }
        return new GlobalArrayInit(name, size, initVal);
    }
}
