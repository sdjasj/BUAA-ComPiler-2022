package IntermediateCode;

import MipsCode.GlobalStrInit;

public class GlobalStrDecl extends GlobalDecl {
    private String outputContent;

    public GlobalStrDecl(StoreType storeType, String name, boolean isConst,
                         String outputContent) {
        super(storeType, name, isConst);
        this.outputContent = outputContent;
    }

    @Override
    public void output() {
        System.out.println("const " + storeType + " " + name + " = " + "\"" + outputContent + "\"");
    }

    public GlobalStrInit toMips() {
        return new GlobalStrInit(name, outputContent);
    }
}
