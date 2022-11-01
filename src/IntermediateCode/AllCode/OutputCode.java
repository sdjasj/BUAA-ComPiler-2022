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
        super(target, null, null, Operator.PRINTF);
        this.isInt = isInt;
    }

    public boolean isInt() {
        return isInt;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        if (isInt) {
            registerPool.clearSpecialReg("$a0", varAddressOffset, mipsVisitor);
            if (target.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$a0", target.getName()));
            } else if (mipsVisitor.varIsGlobal(target.getName())) {
                mipsVisitor.addMipsCode(MipsCode.generateLW("$a0", target.getName(), "$0"));
            } else {
                String targetReg =
                    registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset,
                        mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateMOVE("$a0", targetReg));
            }
            //v0 is need to store?
            mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", "1"));
            mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        } else {
            registerPool.clearSpecialReg("$a0", varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLA(target.getName(), "$a0"));
            //v0 is need to store?
            mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", "4"));
            mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        }

        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("printf " + target.getName()));
    }

    @Override
    public void output() {
        if (isInt) {
            System.out.println(op + " INT " + target);
        } else {
            System.out.println(op + " STR " + target);
        }
    }
}
