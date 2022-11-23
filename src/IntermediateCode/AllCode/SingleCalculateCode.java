package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsCode.MipsCompareCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class SingleCalculateCode extends IntermediateCode {

    public SingleCalculateCode(Operand target, Operand source1, Operator op) {
        super(target, source1, null, op);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        String targetReg =
            registerPool.allocateRegToVarNotLoad(target, varAddressOffset,
                mipsVisitor, this);
        String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
//        if (source1.isNUMBER()) {
//            src1Reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(MipsCode.generateLi(src1Reg, source1.getName()));
//        } else if (mipsVisitor.varIsGlobal(source1.getName())) {
//            src1Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(MipsCode.generateLW(src1Reg, source1.getName(), "$0"));
//        } else {
//            src1Reg =
//                registerPool.allocateRegToVarLoad(source1.getName(), varAddressOffset, mipsVisitor);
//        }
        if (op == Operator.PLUS) {
            mipsVisitor.addMipsCode(MipsCode.generateADDU(targetReg, "$0", src1Reg));
        } else if (op == Operator.NEG) {
            mipsVisitor.addMipsCode(MipsCode.generateSUBU(targetReg, "$0", src1Reg));
        } else if (op == Operator.NOT) {
            mipsVisitor.addMipsCode(new MipsCompareCode("seq", targetReg, src1Reg, "$0"));
        }
    }

    @Override
    public void output() {
        System.out.println(target + " = " + op + " " + source1);
    }
}
