package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsBranchCode;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class BranchCode extends IntermediateCode {

    public BranchCode(Operand target, Operand source1, Operand source2, Operator op) {
        super(target, source1, source2, op);
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
        String src1Reg;
        if (source1.isNUMBER()) {
            src1Reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLi(src1Reg, source1.getName()));
        } else if (mipsVisitor.varIsGlobal(source1.getName())) {
            src1Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLW(src1Reg, source1.getName(), "$0"));
        } else {
            src1Reg =
                registerPool.allocateRegToVarLoad(source1.getName(), varAddressOffset, mipsVisitor);
        }

        String src2Reg = null;
        if (source2 != null) {
            if (source2.isNUMBER()) {
                src2Reg = source2.getName();
            } else if (mipsVisitor.varIsGlobal(source2.getName())) {
                src2Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLW(src2Reg, source2.getName(), "$0"));
            } else {
                src2Reg =
                    registerPool.allocateRegToVarLoad(source2.getName(), varAddressOffset, mipsVisitor);
            }
        }

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
        } else if (op == Operator.BNE) {
            mipsOp = "bne";
        } else if (op == Operator.BGE) {
            mipsOp = "bge";
        } else if (op == Operator.BLE) {
            mipsOp = "ble";
        } else if (op == Operator.BLT) {
            mipsOp = "blt";
        } else if (op == Operator.BGT) {
            mipsOp = "bgt";
        } else if (op == Operator.BEQZ) {
            mipsOp = "beqz";
        } else if (op == Operator.BNEZ) {
            mipsOp = "bnez";
        }
        mipsVisitor.addMipsCode(new MipsBranchCode(mipsOp, target.getName(), src1Reg, src2Reg));
    }
}