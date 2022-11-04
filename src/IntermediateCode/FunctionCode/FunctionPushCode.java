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
        } else if (mipsVisitor.varIsGlobal(target.getName())) {
            if (target.isAddress()) {
                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLA(target.getName(), reg));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else if (target.isVar()) {
                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLW(reg, target.getName(), "$0"));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else {
                System.err.println("error in toMips of funtion push by global");
            }
        } else if (varAddressOffset.isParam(target.getName())) {
            if (target.isAddress()) {
                String tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(
                    MipsCode.generateLW(tempReg,
                        String.valueOf(varAddressOffset.getVarOffset(
                            target.getName())), "$sp"));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else if (target.isVar()) {
                mipsVisitor.addMipsCode(MipsCode.generateSW(
                    registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset,
                        mipsVisitor),
                    String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else {
                System.err.println("error in toMips of funtion push by param");
            }
        } else {
            if (target.isVar()) {
                mipsVisitor.addMipsCode(MipsCode.generateSW(
                    registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset,
                        mipsVisitor), String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else if (target.isAddress()) {
                if (target.getName().startsWith("t@")) {
                    String reg =
                        registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset,
                            mipsVisitor);
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
                } else {
                    int arrayOffset = varAddressOffset.getVarOffset(target.getName());
                    String tempReg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
                    mipsVisitor.addMipsCode(
                        MipsCode.generateADDIU(tempReg, "$sp", String.valueOf(arrayOffset)));
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
                }
            }
        }



//        if (target.isNUMBER()) {
//            String reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(MipsCode.generateLi(reg, target.getName()));
//            mipsVisitor.addMipsCode(
//                MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//        } else if (target.isVar()) {
//            if (mipsVisitor.varIsGlobal(target.getName())) {
//                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                mipsVisitor.addMipsCode(MipsCode.generateLW(reg, target.getName(), "$0"));
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//            } else {
//                mipsVisitor.addMipsCode(MipsCode.generateSW(
//                    registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset, mipsVisitor),
//                    String.valueOf(-(offset * 4 + 4)), "$sp"));
//            }
//        } else if (target.isAddress()) {
//            //数组地址传参
//            if (mipsVisitor.varIsGlobal(target.getName())) {
//                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                mipsVisitor.addMipsCode(MipsCode.generateLA(target.getName(), reg));
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//            } else if (varAddressOffset.isParam(target.getName())) {
//                String tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateADDIU(tempReg, "$sp",
//                        String.valueOf(varAddressOffset.getVarOffset(
//                            target.getName()))));
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateLW(tempReg, "0", tempReg));
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//            } else {
//                if (target.getName().startsWith("t@")) {
//                    String reg =
//                        registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset,
//                            mipsVisitor);
//                    mipsVisitor.addMipsCode(
//                        MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//                } else {
//                    int arrayOffset = varAddressOffset.getVarOffset(target.getName());
//                    String tempReg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//                    mipsVisitor.addMipsCode(
//                        MipsCode.generateADDIU(tempReg, "$sp", String.valueOf(arrayOffset)));
//                    mipsVisitor.addMipsCode(
//                        MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//                }
//            }
//        }

    }

    @Override
    public void output() {
        System.out.println(Operator.PUSH + " " + target);
    }
}
