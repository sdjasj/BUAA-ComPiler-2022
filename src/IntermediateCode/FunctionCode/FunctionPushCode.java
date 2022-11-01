package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class FunctionPushCode extends IntermediateCode {
    int offset;

    public FunctionPushCode(Operand target, int offset) {
        super(target, null, null, Operator.PUSH);
        this.offset = offset;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        if (target.isNUMBER()) {
            String reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLi(reg, target.getName()));
            mipsVisitor.addMipsCode(
                MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
        } else if (target.isVar()) {
            if (mipsVisitor.varIsGlobal(target.getName())) {
                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLW(reg, target.getName(), "$0"));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else {
                mipsVisitor.addMipsCode(MipsCode.generateSW(
                    registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset, mipsVisitor),
                    String.valueOf(-(offset * 4 + 4)), "$sp"));
            }
        } else if (target.isAddress()) {
            //数组地址传参
            if (mipsVisitor.varIsGlobal(target.getName())) {
                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLA(target.getName(), reg));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else {
                int offset = varAddressOffset.getVarOffset(target.getName());
                String tempReg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(
                    MipsCode.generateADDIU(tempReg, "$sp", addressMul4(String.valueOf(offset))));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            }
        }

    }

    @Override
    public void output() {
        System.out.println(Operator.PUSH + " " + target);
    }
}
