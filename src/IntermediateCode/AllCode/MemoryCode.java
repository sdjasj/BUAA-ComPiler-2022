package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.HashSet;

public class MemoryCode extends IntermediateCode {

    public MemoryCode(Operand target, Operand source1, Operand source2, Operator op) {
        super(target, source1, source2, op);
    }

    @Override
    public Operand getLeftVal() {
        return null;
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return new Pair<Operand, Operand>(target, source2);
    }

    @Override
    public HashSet<Operand> getUsedSet() {
        HashSet<Operand> usedSet = new HashSet<>();
        if (target != null && target.isLocal()) {
            usedSet.add(target);
        }
        if (source2 != null && source2.isLocal()) {
            usedSet.add(source2);
        }
        return usedSet;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        String targetReg;
        if (op == Operator.LOAD) {
            targetReg = registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor);
        } else {
            targetReg = getSrcReg(target, varAddressOffset, mipsVisitor, registerPool);
        }

        String source2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
//        if (source2.isNUMBER()) {
//            source2Reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(
//                MipsCode.generateLi(source2Reg, addressMul4(source2.getName())));
//        } else if (mipsVisitor.varIsGlobal(source2.getName())) {
//            source2Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(MipsCode.generateLW(source2Reg, source2.getName(), "$0"));
//        } else {
//            source2Reg =
//                registerPool.allocateRegToVarLoad(source2.getName(), varAddressOffset,
//                    mipsVisitor);
//        }

        if (mipsVisitor.varIsGlobal(source1.getName())) {
            //操作的内存名称为全局变量
            if (op == Operator.LOAD) {
                mipsVisitor.addMipsCode(MipsCode.generateLW(targetReg, source1.getName(), source2Reg));
            } else if (op == Operator.STORE) {
                mipsVisitor.addMipsCode(MipsCode.generateSW(targetReg, source1.getName(), source2Reg));
            }
            return;
        } else {
            //局部变量
            String tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
            if (varAddressOffset.isParam(source1)) {
                //参数
                mipsVisitor.addMipsCode(MipsCode.generateLW(tempReg,
                    String.valueOf(varAddressOffset.getArrayOffset(source1, 0)), "$sp"));
                mipsVisitor.addMipsCode(MipsCode.generateADDU(source2Reg, source2Reg, tempReg));
            } else {
                //普通局部变量
                mipsVisitor.addMipsCode(MipsCode.generateADDIU(tempReg, "$sp",
                    String.valueOf(varAddressOffset.getArrayOffset(source1, 0))));
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
