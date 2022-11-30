package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class OutputCode extends IntermediateCode {
    private boolean isInt;

    public OutputCode(Operand target, boolean isInt) {
        super(null, target, null, Operator.PRINTF);
        this.isInt = isInt;
    }

    public boolean isInt() {
        return isInt;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        if (isInt) {
            registerPool.clearSpecialReg(source1,"$a0", varAddressOffset, mipsVisitor, this);
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$a0", source1.getName()));
            } else if (source1.isGlobal()) {
                mipsVisitor.addMipsCode(MipsCode.generateLW("$a0",
                    String.valueOf(mipsVisitor.getOffsetByVar(source1.getName(), 0)), "$gp"));
            } else {
                String targetReg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
//                System.err.println(source1);
//                System.err.println(targetReg);
                mipsVisitor.addMipsCode(MipsCode.generateMOVE("$a0", targetReg));
                registerPool.unFreeze(targetReg);
            }
            //v0 is need to store?
            mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", "1"));
            mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        } else {
            registerPool.clearSpecialReg(source1,"$a0", varAddressOffset, mipsVisitor, this);
            mipsVisitor.addMipsCode(MipsCode.generateLA(source1.getName(), "$a0"));
            //v0 is need to store?
            mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", "4"));
            mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        }

        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("printf " + source1.getName()));
    }

    @Override
    public void output() {
        if (isInt) {
            System.out.println(op + " INT " + source1);
        } else {
            System.out.println(op + " STR " + source1);
        }
    }
}
