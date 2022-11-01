package IntermediateCode;

import MipsCode.GlobalVarInit;

public class GlobalVarDecl extends GlobalDecl {
    private int initVal;

    public GlobalVarDecl(StoreType storeType, String name, boolean isConst, int initVal) {
        super(storeType, name, isConst);
        this.initVal = initVal;
    }

    @Override
    public void output() {
        String constDef = "";
        if (isConst) {
            constDef = "const";
        }
        String intermediateCode = constDef + " " + storeType + " " + name + " = " + initVal;
        System.out.println(intermediateCode);
    }

    public GlobalVarInit toMips() {
        return new GlobalVarInit(name, initVal);
    }
}
