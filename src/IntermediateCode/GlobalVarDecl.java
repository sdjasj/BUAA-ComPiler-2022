package IntermediateCode;

import MipsCode.GlobalVarInit;

public class GlobalVarDecl extends GlobalDecl {
    private int initVal;
    private boolean isAssigned;

    public GlobalVarDecl(StoreType storeType, String name, boolean isConst, int initVal) {
        super(storeType, name, isConst);
        this.initVal = initVal;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public void setAssigned(boolean assigned) {
        isAssigned = assigned;
    }

    public int getInitVal() {
        return initVal;
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
