package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsBranchCode;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.ArrayList;
import java.util.List;

public class BranchCode extends IntermediateCode {

    public BranchCode(Operand target, Operand source1, Operand source2, Operator op) {
        super(target, source1, source2, op);
    }

    @Override
    public Operand getLeftVal() {
        return target;
    }

    @Override
    public void output() {
        if (op == Operator.BEQZ || op == Operator.BNEZ) {
            System.out.println("IF " + source1 + " " + op + " GOTO " + target);
        } else {
            System.out.println("IF " + source1 + " " + op + " " + source2 + " GOTO " + target);
        }
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
//        System.err.println(this);
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



//        BEQ,
//        BNE,
//        BGE,
//        BLE,
//        BLT,
//        BGT,
//        BEQZ
//        BNEZ
        String mipsOp = null;
        if (op == Operator.BEQ) {
            mipsOp = "beq";
            String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            String src2Reg = null;
            if (source2 != null) {
                src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
            }
            mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
            registerPool.unFreeze(src1Reg);
            registerPool.unFreeze(src2Reg);
        } else if (op == Operator.BNE) {
            mipsOp = "bne";
            String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            String src2Reg = null;
            if (source2 != null) {
                src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
            }
            mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
            registerPool.unFreeze(src1Reg);
            registerPool.unFreeze(src2Reg);
        } else if (op == Operator.BGE) {
            mipsOp = "bge";
            String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            String src2Reg = null;
            if (source2 != null) {
                src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
            }
            mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
            registerPool.unFreeze(src1Reg);
            registerPool.unFreeze(src2Reg);
        } else if (op == Operator.BLE) {
            mipsOp = "ble";
            String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            String src2Reg = null;
            if (source2 != null) {
                src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
            }
            mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
            registerPool.unFreeze(src1Reg);
            registerPool.unFreeze(src2Reg);
        } else if (op == Operator.BLT) {
            mipsOp = "blt";
            if (source2.isNUMBER() && Integer.parseInt(source2.getName()) <= 32767 && Integer.parseInt(source2.getName()) >= -32768) {
                String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
                mipsVisitor.addMipsCode(MipsCode.generateSLTI("$1", src1Reg, source2.getName()));
                mipsVisitor.addMipsCode(new MipsBranchCode("bne", target.getName(), "$1", "$0"));
                registerPool.unFreeze(src1Reg);
            } else {
                String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
                String src2Reg = null;
                if (source2 != null) {
                    src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
                }
                mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
                registerPool.unFreeze(src1Reg);
                registerPool.unFreeze(src2Reg);
            }
        } else if (op == Operator.BGT) {
            mipsOp = "bgt";
                String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
                String src2Reg = null;
                if (source2 != null) {
                    src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
                }
                mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
                registerPool.unFreeze(src1Reg);
                registerPool.unFreeze(src2Reg);
        } else if (op == Operator.BEQZ) {
            mipsOp = "beqz";
            String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            String src2Reg = null;
            if (source2 != null) {
                src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
            }
            mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
            registerPool.unFreeze(src1Reg);
            registerPool.unFreeze(src2Reg);
        } else if (op == Operator.BNEZ) {
            mipsOp = "bnez";
            String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            String src2Reg = null;
            if (source2 != null) {
                src2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
            }
            mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
            registerPool.unFreeze(src1Reg);
            registerPool.unFreeze(src2Reg);
        }
    }
}
