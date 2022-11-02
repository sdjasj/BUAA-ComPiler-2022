package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class MemoryCode extends IntermediateCode {

    public MemoryCode(Operand target, Operand source1, Operand source2, Operator op) {
        super(target, source1, source2, op);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        String targetReg;
        if (target.isNUMBER()) {
            //存储的为数字
            targetReg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLi(targetReg, target.getName()));
        } else {
            //存储的为变量
            targetReg =
                registerPool.allocateRegToVarNotLoad(target.getName(), varAddressOffset,
                    mipsVisitor);

        }

        String source2Reg;
        if (source2.isNUMBER()) {
            source2Reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(
                MipsCode.generateLi(source2Reg, addressMul4(source2.getName())));
        } else if (mipsVisitor.varIsGlobal(source2.getName())) {
            source2Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLW(source2Reg, source2.getName(), "$0"));
        } else {
            source2Reg =
                registerPool.allocateRegToVarLoad(source2.getName(), varAddressOffset,
                    mipsVisitor);
        }

        if (mipsVisitor.varIsGlobal(source1.getName())) {
            //操作的内存名称为全局变量
            if (op == Operator.LOAD) {
                mipsVisitor.addMipsCode(MipsCode.generateLW(targetReg, source1.getName(), source2Reg));
            } else if (op == Operator.STORE) {
                mipsVisitor.addMipsCode(MipsCode.generateSW(targetReg, source1.getName(), source2Reg));
            }
            return;
        } else {
            String tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
            if (varAddressOffset.isParam(source1.getName())) {
                mipsVisitor.addMipsCode(MipsCode.generateLW(tempReg,
                    String.valueOf(varAddressOffset.getArrayOffset(source1.getName(), 0)), "$sp"));
                mipsVisitor.addMipsCode(MipsCode.generateADDU(source2Reg, source2Reg, tempReg));
            } else {
                mipsVisitor.addMipsCode(MipsCode.generateADDIU(tempReg, "$sp",
                    String.valueOf(varAddressOffset.getArrayOffset(source1.getName(), 0))));
                mipsVisitor.addMipsCode(MipsCode.generateADDU(source2Reg, source2Reg, tempReg));
            }






            if (op == Operator.LOAD) {
                mipsVisitor.addMipsCode(MipsCode.generateLW(targetReg, "0", source2Reg));
            } else {
                mipsVisitor.addMipsCode(MipsCode.generateSW(targetReg, "0", source2Reg));
            }
        }

    }

    @Override
    public void output() {
        if (op == Operator.LOAD) {
            System.out.println(
                Operator.LOAD + " " + target + " FROM " + source1 + " OFFSET " + source2);
        } else if (op == Operator.STORE) {
            System.out.println(
                Operator.STORE + " " + target + " TO " + source1 + " OFFSET " + source2);
        }
    }
}
