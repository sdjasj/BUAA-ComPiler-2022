package MipsCode.MipsCode;

public class MipsCompareCode extends MipsCode {

    public MipsCompareCode(String codeName, String target, String source1, String source2) {
        super(codeName, target, source1, source2);
        MipsCode.registerPool.addDirtyRegs(target);
    }

    @Override
    public void output() {
        System.out.println(codeName + " " + target + ", " + source1 + ", " + source2);
    }
}
