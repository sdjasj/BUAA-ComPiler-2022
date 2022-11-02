package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsCode.MipsCompareCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class CompareCode extends IntermediateCode {
//    GT,
//    GTE,
//    LT,
//    LTE,
//    SEQ,
//    SNE,
    public CompareCode(Operand target, Operand source1, Operand source2, Operator op) {
        super(target, source1, source2, op);
    }

    @Override
    public void output() {
        System.out.println(target + " = " + source1 + " " + op + " " + source2);
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

        String src2Reg;
        if (source2.isNUMBER()) {
            src2Reg = source2.getName();
        } else if (mipsVisitor.varIsGlobal(source2.getName())) {
            src2Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateLW(src2Reg, source2.getName(), "$0"));
        } else {
            src2Reg =
                registerPool.allocateRegToVarLoad(source2.getName(), varAddressOffset, mipsVisitor);
        }

        String target = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);

        String mipsOp = null;
        if (op == Operator.GT) {
            mipsOp = "sgt";
        } else if (op == Operator.GTE) {
            mipsOp = "sge";
        } else if (op == Operator.LT) {
            if (source2.isNUMBER()) {
                mipsOp = "slti";
            } else {
                mipsOp = "slt";
            }
        } else if (op == Operator.LTE) {
            mipsOp = "sle";
        } else if (op == Operator.SEQ) {
            mipsOp = "seq";
        } else if (op == Operator.SNE) {
            mipsOp = "sne";
        }

        mipsVisitor.addMipsCode(new MipsCompareCode(mipsOp, target, src1Reg, src2Reg));
    }
}
