package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.ArrayList;
import java.util.List;

public class CalculateCode extends IntermediateCode {

    public CalculateCode(Operand target, Operand source1, Operand source2,
                         Operator op) {
        super(target, source1, source2, op);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("calculate " + target));
        String source1Reg;
        String source2Reg;
        //TODO: $0

        if (source1.isNUMBER()) {
            source1Reg = source1.getName();
        } else {
            source1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
        }

        if (source2.isNUMBER()) {
            source2Reg = source2.getName();
        } else {
            source2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
        }

        String resReg;

        if (target.isGlobal()) {
            resReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
        } else {
            resReg =
                registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor, this);
        }

        if (op == Operator.ADD) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsCode = MipsCode.generateADDIU(resReg, source2Reg, source1Reg);
            } else if (source2.isNUMBER()) {
                mipsCode = MipsCode.generateADDIU(resReg, source1Reg, source2Reg);
            } else {
                mipsCode = MipsCode.generateADDU(resReg, source1Reg, source2Reg);
            }
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.SUB) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateSUBU(resReg, "$1", source2Reg);
            } else if (source2.isNUMBER()) {
                mipsCode = MipsCode.generateSUBIU(resReg, source1Reg, source2Reg);
            } else {
                mipsCode = MipsCode.generateSUBU(resReg, source1Reg, source2Reg);
            }
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.MUL) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateMUL(resReg, source2Reg, "$1");
            } else if (source2.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source2Reg));
                mipsCode = MipsCode.generateMUL(resReg, source1Reg, "$1");
            } else {
                mipsCode = MipsCode.generateMUL(resReg, source1Reg, source2Reg);
            }
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.DIV) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateDIV(source2Reg, "$1");
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            } else if (source2.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source2Reg));
                mipsCode = MipsCode.generateDIV(source1Reg, "$1");
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            } else {
                mipsCode = MipsCode.generateDIV(source1Reg, source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            }
        } else if (op == Operator.MOD) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateDIV(source2Reg, "$1");
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            } else if (source2.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source2Reg));
                mipsCode = MipsCode.generateDIV(source1Reg, "$1");
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            } else {
                mipsCode = MipsCode.generateDIV(source1Reg, source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            }
        }


        //左值为全局变量
        if (target.isGlobal()) {
            MipsCode mipsCode1 = MipsCode.generateSW(resReg, target.getName(), "$0");
            mipsVisitor.addMipsCode(mipsCode1);
        }
        registerPool.unFreeze(source1Reg);
        registerPool.unFreeze(source2Reg);
        registerPool.unFreeze(resReg);
    }

    @Override
    public void output() {
        if (op == Operator.ADD) {
            System.out.println(target + " = " + source1 + " + " + source2);
        } else if (op == Operator.SUB) {
            System.out.println(target + " = " + source1 + " - " + source2);
        } else if (op == Operator.MUL) {
            System.out.println(target + " = " + source1 + " * " + source2);
        } else if (op == Operator.DIV) {
            System.out.println(target + " = " + source1 + " / " + source2);
        } else if (op == Operator.MOD) {
            System.out.println(target + " = " + source1 + " % " + source2);
        }
    }
}
