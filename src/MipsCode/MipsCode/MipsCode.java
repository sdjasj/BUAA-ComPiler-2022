package MipsCode.MipsCode;


public class MipsCode {
    protected String target;
    protected String source1;
    protected String source2;
    protected String codeName;

    public MipsCode(String codeName, String target, String source1, String source2) {
        this.codeName = codeName;
        this.target = target;
        this.source1 = source1;
        this.source2 = source2;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSource1() {
        return source1;
    }

    public void setSource1(String source1) {
        this.source1 = source1;
    }

    public String getSource2() {
        return source2;
    }

    public void setSource2(String source2) {
        this.source2 = source2;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public static MipsCode generateLW(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("lw", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSW(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("sw", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateLA(String tag, String target) {
        MipsCode mipsCode = new MipsCode("la", tag, target, null);
        return mipsCode;
    }

    public static MipsCode generateADDIU(String target, String source1, String source2) {

        MipsCode mipsCode = new MipsCode("addiu", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateTag(String target) {
        MipsCode mipsCode = new MipsCode("tag", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateLi(String target, String source1) {
        MipsCode mipsCode = new MipsCode("li", target, source1, null);
        return mipsCode;
    }

    public static MipsCode generateMOVE(String target, String source1) {
        MipsCode mipsCode = new MipsCode("move", target, source1, null);
        return mipsCode;
    }

    public static MipsCode generateADDU(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("addu", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSUBU(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("subu", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSUBIU(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("subiu", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateMULT(String source1, String source2) {
        MipsCode mipsCode = new MipsCode("mult", null, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateMFLO(String target) {
        MipsCode mipsCode = new MipsCode("mflo", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateMFHI(String target) {
        MipsCode mipsCode = new MipsCode("mfhi", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateDIV(String source1, String source2) {
        MipsCode mipsCode = new MipsCode("div", null, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateDIV(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("div", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSYSCALL() {
        MipsCode mipsCode = new MipsCode("syscall", null, null, null);
        return mipsCode;
    }

    public static MipsCode generateJAL(String tag) {
        MipsCode mipsCode = new MipsCode("jal", tag, null, null);
        return mipsCode;
    }

    public static MipsCode generateJR(String target) {
        MipsCode mipsCode = new MipsCode("jr", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateComment(String target) {
        MipsCode mipsCode = new MipsCode("comment", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateLable(String target) {
        MipsCode mipsCode = new MipsCode("lable", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateJ(String target) {
        MipsCode mipsCode = new MipsCode("j", target, null, null);
        return mipsCode;
    }

    public static MipsCode generateMUL(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("mul", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSLL(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("sll", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSRA(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("sra", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSRL(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("srl", target, source1, source2);
        return mipsCode;
    }

    public static MipsCode generateSLT(String target, String source1, String source2) {
        MipsCode mipsCode = new MipsCode("slt", target, source1, source2);
        return mipsCode;
    }


    public void output() {
        if (codeName.equals("lw")) {
            System.out.println("lw " + target + ", " + source1 + "(" + source2 + ")");
        } else if (codeName.equals("sw")) {
            System.out.println("sw " + target + ", " + source1 + "(" + source2 + ")");
        } else if (codeName.equals("la")) {
            System.out.println("la " + source1 + ", " + target);
        } else if (codeName.equals("addiu")) {
            System.out.println("addiu " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("tag")) {
            System.out.println(target + ": ");
        } else if (codeName.equals("li")) {
            System.out.println("li " + target + ", " + source1);
        } else if (codeName.equals("move")) {
            System.out.println("move " + target + ", " + source1);
        } else if (codeName.equals("addu")) {
            System.out.println("addu " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("subu")) {
            System.out.println("subu " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("subiu")) {
            System.out.println("subiu " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("mult")) {
            System.out.println("mult " + source1 + ", " + source2);
        } else if (codeName.equals("mflo")) {
            System.out.println("mflo " + target);
        } else if (codeName.equals("mfhi")) {
            System.out.println("mfhi " + target);
        } else if (codeName.equals("div")) {
            if (target == null) {
                System.out.println("div " + source1 + ", " + source2);
            } else {
                System.out.println("div " + target + ", " + source1 + ", " + source2);
            }
        } else if (codeName.equals("syscall")) {
            System.out.println("syscall");
        } else if (codeName.equals("jal")) {
            System.out.println("jal " + target);
        } else if (codeName.equals("jr")) {
            System.out.println("jr " + target);
        } else if (codeName.equals("comment")) {
            System.out.println("# " + target);
        } else if (codeName.equals("lable")) {
            System.out.println(target + ":");
        } else if (codeName.equals("j")) {
            System.out.println("j " + target);
        } else if (codeName.equals("mul")) {
            System.out.println("mul " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("sll")) {
            System.out.println("sll " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("sra")) {
            System.out.println("sra " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("srl")) {
            System.out.println("srl " + target + ", " + source1 + ", " + source2);
        } else if (codeName.equals("slt")) {
            System.out.println("slt " + target + ", " + source1 + ", " + source2);
        }
    }
}
