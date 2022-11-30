package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.List;

public class FunctionPushCode extends IntermediateCode {
    int offset;

    public FunctionPushCode(Operand target, int offset) {
        super(null, target, null, Operator.PUSH);
        this.offset = offset;
    }

    @Override
    public Operand getLeftVal() {
        return null;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

        mipsVisitor.addMipsCode(MipsCode.generateComment("push " + source1));
        if (source1.isNUMBER()) {
            String reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor, this);
            mipsVisitor.addMipsCode(MipsCode.generateLi(reg, source1.getName()));
            mipsVisitor.addMipsCode(
                MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            registerPool.unFreeze(reg);
        } else if (source1.isGlobal()) {
            if (source1.isAddress()) {
//                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
//                mipsVisitor.addMipsCode(MipsCode.generateLA(source1.getName(), reg));
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//                registerPool.unFreeze(reg);
                String reg =
                    registerPool.allocateRegToVarNotLoad(source1, varAddressOffset, mipsVisitor,
                        this);
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else if (source1.isVar()) {
//                String reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
//                mipsVisitor.addMipsCode(MipsCode.generateLW(reg, source1.getName(), "$0"));
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
//                registerPool.unFreeze(reg);
                mipsVisitor.addMipsCode(MipsCode.generateLW("$1",
                    String.valueOf(mipsVisitor.getOffsetByVar(source1.getName(), 0)), "$gp"));
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW("$1", String.valueOf(-(offset * 4 + 4)), "$sp"));
                registerPool.unFreeze("$1");
            } else {
                System.err.println("error in toMips of funtion push by global");
            }
        } else if (varAddressOffset.isParam(source1)) {
            if (source1.isAddress()) {
                String tempReg =
                    registerPool.allocateRegToVarNotLoad(source1, varAddressOffset, mipsVisitor,
                        this);
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
                registerPool.unFreeze(tempReg);
            } else if (source1.isVar()) {
                String reg = registerPool.allocateRegToVarLoad(source1, varAddressOffset,
                    mipsVisitor, this);
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else {
                System.err.println("error in toMips of funtion push by param");
            }
        } else {
            if (source1.isVar()) {
                mipsVisitor.addMipsCode(MipsCode.generateSW(
                    registerPool.allocateRegToVarLoad(source1, varAddressOffset,
                        mipsVisitor, this), String.valueOf(-(offset * 4 + 4)), "$sp"));
            } else if (source1.isAddress()) {
                if (source1.getName().startsWith("t@")) {
                    String reg =
                        registerPool.allocateRegToVarLoad(source1, varAddressOffset,
                            mipsVisitor, this);
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSW(reg, String.valueOf(-(offset * 4 + 4)), "$sp"));
                } else {
                    String tempReg =
                        registerPool.allocateRegToVarNotLoad(source1, varAddressOffset, mipsVisitor,
                            this);
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSW(tempReg, String.valueOf(-(offset * 4 + 4)), "$sp"));
                    registerPool.unFreeze(tempReg);
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
        System.out.println(Operator.PUSH + " " + source1);
    }
}
