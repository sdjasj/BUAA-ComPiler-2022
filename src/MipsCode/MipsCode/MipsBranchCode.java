package MipsCode.MipsCode;

public class MipsBranchCode extends MipsCode {

    public MipsBranchCode(String codeName, String target, String source1, String source2) {
        super(codeName, target, source1, source2);
    }

    @Override
    public void output() {
        if (codeName.equals("beqz") || codeName.equals("bnez")) {
            System.out.println(codeName + " " + source1 + " " + target);
        } else {
            System.out.println(codeName + " " + source1 + ", " + source2 + " " + target);
        }
    }
}
