package IntermediateCode;

public class GlobalDecl {
    protected StoreType storeType;
    protected String name;
    protected boolean isConst;

    public enum StoreType {
        int_,
        str_,
    }

    public GlobalDecl(StoreType storeType, String name, boolean isConst) {
        this.storeType = storeType;
        this.name = name;
        this.isConst = isConst;
    }

    public IntermediateCode generateIntermediateCode() {
        return null;
    }

    public void output() {

    }

}
